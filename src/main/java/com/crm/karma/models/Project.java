package com.crm.karma.models;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Project extends Model {
  @Schema(description = "Project's name")
  @Column(nullable = false)
  private String name;

  @Schema(description = "ID of user who created it")
  @Column(nullable = false, name = "user_id")
  private UUID userId;

  @Schema(description = "Project's deletion date")
  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Schema(description = "Client linked to the project")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_id", nullable = false)
  @OnDelete(action = OnDeleteAction.RESTRICT)
  private Client client;
}
