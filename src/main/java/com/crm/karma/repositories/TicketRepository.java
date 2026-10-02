package com.crm.karma.repositories;

import com.crm.karma.enums.TicketStatus;
import com.crm.karma.models.Ticket;
import com.crm.karma.responses.TicketCounts;
import com.crm.karma.responses.TicketResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<@NonNull Ticket, @NonNull UUID> {

  @Query(
    value = """
      SELECT new com.crm.karma.responses.TicketResponse(
        t.id,
        t.title,
        t.description,
        t.acceptedAt,
        t.status,
        t.code,
        t.user.id,
        s.id,
        t.active,
        t.createdAt,
        t.updatedAt
      )
      FROM Ticket t
      LEFT JOIN t.support s
      WHERE t.active = TRUE
        AND (:hasUser = 0 OR t.user.id = :userId)
        AND (:hasSupport = 0 OR s.id = :supportId)
        AND (:hasStatus = 0 OR t.status = :status)
        AND (
          :query = ''
          OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))
          OR STR(t.code) LIKE CONCAT('%', :query, '%')
          OR (:hasCode = 1 AND t.code = :code)
        )
        AND (:hasStart = 0 OR t.createdAt >= :startDate)
        AND (:hasEnd = 0 OR t.createdAt < :endDate)
      ORDER BY t.createdAt DESC
      """,
    countQuery = """
      SELECT COUNT(t)
      FROM Ticket t
      LEFT JOIN t.support s
      WHERE t.active = TRUE
        AND (:hasUser = 0 OR t.user.id = :userId)
        AND (:hasSupport = 0 OR s.id = :supportId)
        AND (:hasStatus = 0 OR t.status = :status)
        AND (
          :query = ''
          OR LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))
          OR STR(t.code) LIKE CONCAT('%', :query, '%')
          OR (:hasCode = 1 AND t.code = :code)
        )
        AND (:hasStart = 0 OR t.createdAt >= :startDate)
        AND (:hasEnd = 0 OR t.createdAt < :endDate)
      """
  )
  Page<@NonNull  TicketResponse> search(
    @Param("hasUser") int hasUser,
    @Param("userId") UUID userId,
    @Param("hasSupport") int hasSupport,
    @Param("supportId") UUID supportId,
    @Param("hasStatus") int hasStatus,
    @Param("status") TicketStatus status,
    @Param("query") String query,
    @Param("hasCode") int hasCode,
    @Param("code") Long code,
    @Param("hasStart") int hasStart,
    @Param("startDate") Instant startDate,
    @Param("hasEnd") int hasEnd,
    @Param("endDate") Instant endDate,
    Pageable pageable
  );

  @Query(
    """
      SELECT new com.crm.karma.responses.TicketCounts(
        COALESCE(SUM(CASE WHEN t.status = com.crm.karma.enums.TicketStatus.OPEN THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.status = com.crm.karma.enums.TicketStatus.IN_PROGRESS THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.status = com.crm.karma.enums.TicketStatus.RESOLVED THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.status = com.crm.karma.enums.TicketStatus.CANCELLED THEN 1 ELSE 0 END), 0)
      )
      FROM Ticket t
      WHERE t.active = TRUE
        AND t.user.id = :userId
      """
  )
  TicketCounts countForUser(@Param("userId") UUID userId);

  @Query(
    """
      SELECT new com.crm.karma.responses.TicketCounts(
        COALESCE(SUM(CASE WHEN t.status = com.crm.karma.enums.TicketStatus.OPEN THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.status = com.crm.karma.enums.TicketStatus.IN_PROGRESS AND s.id = :supportId THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.status = com.crm.karma.enums.TicketStatus.RESOLVED AND s.id = :supportId THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN t.status = com.crm.karma.enums.TicketStatus.CANCELLED AND s.id = :supportId THEN 1 ELSE 0 END), 0)
      )
      FROM Ticket t
      LEFT JOIN t.support s
      WHERE t.active = TRUE
        AND (t.status = com.crm.karma.enums.TicketStatus.OPEN OR s.id = :supportId)
      """
  )
  TicketCounts countForSupport(@Param("supportId") UUID supportId);
}
