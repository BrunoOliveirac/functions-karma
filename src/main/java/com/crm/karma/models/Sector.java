package com.crm.karma.models;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Sector extends Model {
  @Schema(description = "Sector's name")
  @Column(nullable = false)
  private String name;

  @Schema(description = "ID of user who created it")
  @Column(nullable = false, name = "user_id")
  private UUID userId;

  @Schema(description = "Sector's deletion date")
  @Column(name = "deleted_at")
  private Instant deletedAt;
}
