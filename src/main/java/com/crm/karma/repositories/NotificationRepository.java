package com.crm.karma.repositories;

import com.crm.karma.models.Notification;
import com.crm.karma.responses.NotificationCounts;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<@NonNull Notification, @NonNull UUID> {

  List<Notification> findByUserId(UUID userId);

  List<Notification> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
    UUID userId,
    Pageable pageable
  );

  boolean existsByUserIdAndDeletedAtIsNullAndReadIsFalse(UUID userId);

  @Query(
    """
      SELECT n FROM Notification n
      LEFT JOIN n.actor a
      WHERE n.user.id = :userId
        AND n.deletedAt IS NULL
        AND (
          :status = 'all'
          OR (:status = 'unread' AND n.read = false)
          OR (:status = 'read' AND n.read = true)
        )
        AND (
          :query = ''
          OR LOWER(n.referenceLabel) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
        )
      """
  )
  Page<@NonNull Notification> searchByUserId(
    @Param("userId") UUID userId,
    @Param("query") String query,
    @Param("status") String status,
    Pageable pageable
  );

  @Query(
    """
      SELECT new com.crm.karma.responses.NotificationCounts(
        COUNT(n),
        COALESCE(SUM(CASE WHEN n.read = false THEN 1 ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN n.read = true THEN 1 ELSE 0 END), 0)
      )
      FROM Notification n
      LEFT JOIN n.actor a
      WHERE n.user.id = :userId
        AND n.deletedAt IS NULL
        AND (
          :query = ''
          OR LOWER(n.referenceLabel) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
        )
      """
  )
  NotificationCounts countByUserIdAndQuery(
    @Param("userId") UUID userId,
    @Param("query") String query
  );
}
