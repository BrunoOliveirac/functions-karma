package com.crm.karma.controllers;

import com.crm.karma.enums.TicketStatus;
import com.crm.karma.models.User;
import com.crm.karma.requests.AcceptTicketRequest;
import com.crm.karma.requests.AddTicketRequest;
import com.crm.karma.requests.ListTicketsQueryRequest;
import com.crm.karma.responses.StatusResponse;
import com.crm.karma.responses.TicketCounts;
import com.crm.karma.responses.TicketListResponse;
import com.crm.karma.services.TicketService;
import com.crm.karma.services.TicketSseHub;
import com.crm.karma.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets")
public class TicketController {

  private final UserService userService;
  private final TicketSseHub ticketSseHub;
  private final TicketService ticketService;

  public TicketController(
    UserService userService,
    TicketSseHub ticketSseHub,
    TicketService ticketService
  ) {
    this.userService = userService;
    this.ticketSseHub = ticketSseHub;
    this.ticketService = ticketService;
  }

  /**
   * List paginated tickets.
   * Users and members pass userId. Support passes supportId.
   * Search, status, and dates are optional. Page size must be 5.
   *
   * @param listTicketsQueryRequest Page, page size, and optional filters
   * @return The page plus the totals used by the paginator
   */
  @Operation(summary = "List paginated tickets")
  @GetMapping("/list")
  @PreAuthorize("hasAnyRole('USER', 'MEMBER', 'ADMIN', 'SUPPORT')")
  public TicketListResponse listTickets(ListTicketsQueryRequest listTicketsQueryRequest) {
    return ticketService.listTickets(listTicketsQueryRequest);
  }

  /**
   * Count tickets for the home cards.
   * Pass userId for a user or member. Pass supportId for support.
   * User and member counts use userId for every status.
   * Support open count is every open ticket. The other three support counts use supportId.
   * all is the sum of those four counts.
   *
   * @param userId    Owner id
   * @param supportId Support id
   * @return Open, in progress, resolved, cancelled, and the sum of those four
   */
  @Operation(summary = "Count tickets for the home cards")
  @GetMapping("/counts")
  @PreAuthorize("hasAnyRole('USER', 'MEMBER', 'ADMIN', 'SUPPORT')")
  public TicketCounts countTickets(
    @RequestParam(required = false) UUID userId,
    @RequestParam(required = false) UUID supportId
  ) {
    return ticketService.countTickets(userId, supportId);
  }

  /**
   * Open an SSE stream for the authenticated user.
   *
   * @return Long-lived emitter scoped to the JWT subject
   */
  @Operation(summary = "Subscribe to ticket events of the logged user")
  @GetMapping(value = "stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamTickets() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || authentication.getPrincipal() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    String email = authentication.getPrincipal().toString();
    User user = userService.getByEmail(email);

    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    return ticketSseHub.subscribe(user.getId());
  }

  /**
   * Add a new ticket
   *
   * @param addTicketRequest Object with user ID, title, and description.
   */
  @Operation(summary = "Add a new ticket")
  @PostMapping("/create")
  @PreAuthorize("hasAnyRole('USER', 'MEMBER')")
  public StatusResponse create(@RequestBody AddTicketRequest addTicketRequest) {
    ticketService.add(addTicketRequest);
    return new StatusResponse("success");
  }

  /**
   * Support accepts a user ticket
   *
   * @param acceptTicketRequest Object with ticket ID and support ID
   */
  @Operation(summary = "Accept a user/member ticket")
  @PostMapping("/accept")
  @PreAuthorize("hasAnyRole('SUPPORT')")
  public StatusResponse accept(@RequestBody AcceptTicketRequest acceptTicketRequest) {
    ticketService.acceptTicket(acceptTicketRequest);
    return new StatusResponse("success");
  }

  /**
   * * User, member, or support resolves the ticket
   *
   * @param ticketId Ticket ID
   */
  @Operation(summary = "Resolve a user/member ticket")
  @PostMapping("/resolve/{ticketId}")
  @PreAuthorize("hasAnyRole('USER', 'MEMBER', 'SUPPORT')")
  public StatusResponse resolve(@PathVariable UUID ticketId) {
    ticketService.finalizeTicket(ticketId, TicketStatus.RESOLVED);
    return new StatusResponse("success");
  }

  /**
   * User, member, or support cancels the ticket
   *
   * @param ticketId Ticket ID
   */
  @Operation(summary = "Cancel a user/member ticket")
  @PostMapping("/cancel/{ticketId}")
  @PreAuthorize("hasAnyRole('USER', 'MEMBER', 'SUPPORT')")
  public StatusResponse cancel(@PathVariable UUID ticketId) {
    ticketService.finalizeTicket(ticketId, TicketStatus.CANCELLED);
    return new StatusResponse("success");
  }
}
