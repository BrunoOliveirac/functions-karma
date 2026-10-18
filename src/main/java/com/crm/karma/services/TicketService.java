package com.crm.karma.services;

import com.crm.karma.enums.TicketStatus;
import com.crm.karma.enums.UserType;
import com.crm.karma.models.Ticket;
import com.crm.karma.models.User;
import com.crm.karma.repositories.TicketRepository;
import com.crm.karma.repositories.UserRepository;
import com.crm.karma.requests.AcceptTicketRequest;
import com.crm.karma.requests.AddTicketRequest;
import com.crm.karma.requests.ListTicketsQueryRequest;
import com.crm.karma.responses.TicketCounts;
import com.crm.karma.responses.TicketListResponse;
import com.crm.karma.responses.TicketResponse;
import com.crm.karma.responses.TicketStreamEvent;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.Set;
import java.util.UUID;

@Service
public class TicketService {

  private static final Set<Integer> PAGE_SIZES = Set.of(3, 4, 5);
  private static final UUID EMPTY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

  private final TicketSseHub ticketSseHub;
  private final UserRepository userRepository;
  private final TicketRepository ticketRepository;

  public TicketService(
    TicketSseHub ticketSseHub,
    UserRepository userRepository,
    TicketRepository ticketRepository
  ) {
    this.ticketSseHub = ticketSseHub;
    this.userRepository = userRepository;
    this.ticketRepository = ticketRepository;
  }

  /**
   * A search made of digits, optionally prefixed with #, is also the sequential code.
   * Leading zeros match the padded value shown in the panel, so 08 finds code 8.
   */
  private static Long codeQuery(String query) {
    String value = query.startsWith("#") ? query.substring(1).trim() : query;
    if (!value.matches("\\d+")) return null;

    try {
      return Long.valueOf(value);
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  /**
   * Calendar dates are inclusive. The query uses the start of the next day as an exclusive bound.
   * A missing zone stays on UTC so older clients keep the previous behavior.
   */
  private static ZoneId zone(String timeZone) {
    if (timeZone == null || timeZone.isBlank()) return ZoneOffset.UTC;

    try {
      return ZoneId.of(timeZone.trim());
    } catch (DateTimeException exception) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid time zone");
    }
  }

  private static Instant startOfDay(LocalDate date, ZoneId zone) {
    return date.atStartOfDay(zone).toInstant();
  }

  /**
   * List tickets for a user, member, or support.
   * Users and members see their own tickets. Support sees every open ticket,
   * and their own in-progress, resolved, and cancelled tickets.
   *
   * @param request Page, page size, and optional search, status, and date filters
   * @return The page plus totals used by the paginator
   */
  public TicketListResponse listTickets(ListTicketsQueryRequest request) {
    if (!PAGE_SIZES.contains(request.pageSize())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be 3, 4 or 5");
    }

    if (request.userId() != null && request.supportId() != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inform only userId or supportId");
    }

    int page = Math.max(request.page(), 1);
    String query = request.query() == null ? "" : request.query().trim();
    Long code = codeQuery(query);
    Scope scope = scope(request.userId(), request.supportId(), request.status());
    ZoneId zone = zone(request.timeZone());
    LocalDate from = request.startDate();
    LocalDate to = request.endDate();
    if (from != null && to != null && to.isBefore(from)) {
      LocalDate swap = from;
      from = to;
      to = swap;
    }

    Instant startDate = from == null ? Instant.EPOCH : startOfDay(from, zone);
    Instant endDate = to == null ? Instant.EPOCH : startOfDay(to.plusDays(1), zone);

    Page<@NonNull TicketResponse> result = ticketRepository.search(
      scope.userId() == null ? 0 : 1,
      scope.userId() == null ? EMPTY_ID : scope.userId(),
      scope.supportId() == null ? 0 : 1,
      scope.supportId() == null ? EMPTY_ID : scope.supportId(),
      scope.status() == null ? 0 : 1,
      scope.status() == null ? TicketStatus.OPEN : scope.status(),
      query,
      code == null ? 0 : 1,
      code == null ? 0L : code,
      from == null ? 0 : 1,
      startDate,
      to == null ? 0 : 1,
      endDate,
      PageRequest.of(page - 1, request.pageSize())
    );

    return new TicketListResponse(result.getContent(), result.getTotalPages(), result.getTotalElements());
  }

  /**
   * Card totals. Each status is counted on its own, and all is their sum.
   * A user id counts that user's tickets in every status.
   * A support id counts every open ticket, plus that support's other statuses.
   *
   * @param userId    Owner id
   * @param supportId Support id
   * @return Open, in progress, resolved, cancelled, and the sum of those four
   */
  public TicketCounts countTickets(UUID userId, UUID supportId) {
    if (userId != null && supportId != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inform only userId or supportId");
    }

    if (userId == null && supportId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User or support identifier is required");
    }

    TicketCounts counts = userId != null
      ? ticketRepository.countForUser(userId)
      : ticketRepository.countForSupport(supportId);

    return counts == null ? TicketCounts.empty() : counts;
  }

  public void add(AddTicketRequest addTicketRequest) {
    User user = userRepository.findById(addTicketRequest.userId())
      .orElseThrow(() -> new RuntimeException("User not found!"));

    Ticket ticket = Ticket
      .builder()
      .title(addTicketRequest.title())
      .description(addTicketRequest.description())
      .status(TicketStatus.OPEN)
      .user(user)
      .build();

    ticketRepository.save(ticket);
    publish(ticket, null);
  }

  public void acceptTicket(AcceptTicketRequest acceptTicketRequest) {
    Ticket ticket = ticketRepository.findById(acceptTicketRequest.ticketId())
      .orElseThrow(() -> new RuntimeException("Ticket not found!"));

    User support = userRepository.findById(acceptTicketRequest.supportId())
      .orElseThrow(() -> new RuntimeException("Support not found!"));

    TicketStatus previousStatus = ticket.getStatus();
    ticket.setSupport(support);
    ticket.setAcceptedAt(Instant.now());
    ticket.setStatus(TicketStatus.IN_PROGRESS);

    ticketRepository.save(ticket);
    publish(ticket, previousStatus);
  }

  public void finalizeTicket(
    UUID ticketId,
    TicketStatus status
  ) {
    Ticket ticket = ticketRepository.findById(ticketId)
      .orElseThrow(() -> new RuntimeException("Ticket not found!"));

    TicketStatus previousStatus = ticket.getStatus();
    ticket.setFinishedAt(Instant.now());
    ticket.setStatus(status);

    ticketRepository.save(ticket);
    publish(ticket, previousStatus);
  }

  /**
   * Users and members are scoped by user id.
   * Support without a status, or with OPEN, sees the shared open queue.
   * Any other support status is limited to tickets assigned to that support.
   */
  private Scope scope(UUID userId, UUID supportId, TicketStatus status) {
    if (userId == null && supportId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User or support identifier is required");
    }

    if (userId != null) {
      return new Scope(userId, null, status);
    }

    if (status == null || status == TicketStatus.OPEN) {
      return new Scope(null, null, TicketStatus.OPEN);
    }

    return new Scope(null, supportId, status);
  }

  private void publish(Ticket ticket, TicketStatus previousStatus) {
    TicketStreamEvent event = new TicketStreamEvent(TicketResponse.from(ticket), previousStatus);
    UUID ownerId = ticket.getUser().getId();
    ticketSseHub.publish(ownerId, event);

    boolean broadcastSupports = previousStatus == null
      || previousStatus == TicketStatus.OPEN
      || ticket.getStatus() == TicketStatus.OPEN;

    if (broadcastSupports) {
      for (User support : userRepository.findAllByTypeAndDeletedAtIsNullOrderByNameAsc(UserType.SUPPORT)) {
        if (support.getId().equals(ownerId)) continue;
        ticketSseHub.publish(support.getId(), event);
      }
      return;
    }

    if (ticket.getSupport() != null && !ticket.getSupport().getId().equals(ownerId)) {
      ticketSseHub.publish(ticket.getSupport().getId(), event);
    }
  }

  private record Scope(UUID userId, UUID supportId, TicketStatus status) {
  }
}
