package com.crm.karma.services;

import com.crm.karma.models.Notification;
import com.crm.karma.models.User;
import com.crm.karma.repositories.NotificationRepository;
import com.crm.karma.responses.LatestNotificationsResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationSseHub notificationSseHub;

  public NotificationService(
    NotificationRepository notificationRepository,
    NotificationSseHub notificationSseHub
  ) {
    this.notificationRepository = notificationRepository;
    this.notificationSseHub = notificationSseHub;
  }

  public LatestNotificationsResponse listLatest(UUID userId) {
    List<Notification> latest = notificationRepository
      .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
        userId,
        PageRequest.of(0, 3)
      );

    boolean hasUnreadInLatest = latest.stream().anyMatch(notification -> !notification.getRead());

    boolean hasUnreadNotifications = hasUnreadInLatest
      || (
      latest.size() == 3
        && notificationRepository.existsByUserIdAndDeletedAtIsNullAndReadIsFalse(userId)
    );

    return new LatestNotificationsResponse(latest, hasUnreadNotifications);
  }

  public void add(
    String code,
    String type,
    User actor,
    User user,
    UUID referenceId,
    String referenceLabel
  ) {
    Notification notification = Notification
      .builder()
      .code(code)
      .type(type)
      .actor(actor)
      .user(user)
      .referenceId(referenceId)
      .referenceLabel(referenceLabel)
      .build();

    notificationRepository.save(notification);
    notificationSseHub.publishCreated(user.getId());
  }

  public void saveAll(List<Notification> notifications) {
    notificationRepository.saveAll(notifications);

    notifications.stream()
      .map(notification -> notification.getUser().getId())
      .distinct()
      .forEach(notificationSseHub::publishCreated);
  }

  public void markNotificationAsRead(UUID notificationId) {
    Notification notification = notificationRepository
      .findById(notificationId)
      .orElseThrow(() -> new RuntimeException("Notification not found!"));

    notification.setRead(true);
    notificationRepository.save(notification);
  }

  public void markAllNotificationAsRead(UUID userId) {
    List<Notification> notifications = notificationRepository.findByUserId(userId);

    if (notifications.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Notifications not found!");
    }

    for (Notification notification : notifications) {
      notification.setRead(true);
    }

    notificationRepository.saveAll(notifications);
  }

  public void delete(UUID notificationId) {
    Notification notification = notificationRepository
      .findById(notificationId)
      .orElseThrow(() -> new RuntimeException("Notification not found!"));

    notification.setDeletedAt(Instant.now());
    notificationRepository.save(notification);
  }
}
