package com.crm.karma.services;

import com.crm.karma.models.Notification;
import com.crm.karma.models.User;
import com.crm.karma.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public NotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
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
  }

  public void saveAll(List<Notification> notifications) {
    notificationRepository.saveAll(notifications);
  }
}
