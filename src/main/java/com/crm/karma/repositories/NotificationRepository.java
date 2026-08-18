package com.crm.karma.repositories;

import com.crm.karma.models.Notification;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<@NonNull Notification, @NonNull UUID> {

  List<Notification> findTop5ByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
    UUID userId,
    Pageable pageable
  );
}
