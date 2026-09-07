package com.crm.karma.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
  @Index(name = "idx_notifications_user_created_at", columnList = "user_id, created_at"),
  @Index(name = "idx_notifications_actor_id", columnList = "actor_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends Model {

  @Schema(description = "Internalization's code (used to identify the message)")
  @Column(nullable = false)
  private String code;

  @Schema(description = "Notification's type/origin (project, system, user")
  @Column(nullable = false)
  private String type;

  @Schema(description = "Document's ID")
  @Column(name = "reference_id")
  private UUID referenceId;

  @Schema(description = "Document's Label (name/content")
  @Column(name = "reference_label")
  private String referenceLabel;

  @Schema(description = "Indicate if the user reads the notification")
  @Builder.Default
  @Column(nullable = false)
  private Boolean read = false;

  @Schema(description = "Notification's deletion date")
  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Schema(description = "User who will receive the notification")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @Schema(description = "User of the member is linked")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User actor;
}
