package com.crm.karma.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Model {

  @Schema(description = "Document ID")
  @Id
  @EqualsAndHashCode.Include
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Schema(description = "Active status of the document")
  @Builder.Default
  @Column(nullable = false)
  private Boolean active = true;

  @Schema(description = "Document creation date")
  @CreatedDate
  @Column(nullable = false, updatable = false, name = "created_at")
  private Instant createdAt;

  @Schema(description = "Document update date")
  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  public void toggleStatus() {
    this.active = !this.active;
  }
}