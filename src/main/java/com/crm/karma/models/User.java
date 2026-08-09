package com.crm.karma.models;

import com.crm.karma.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends Model {

  @Schema(description = "User's name")
  @Column(nullable = false)
  private String name;

  @Schema(description = "User's e-mail")
  @Column(nullable = false, unique = true)
  private String email;

  @Schema(description = "User's avatar as a data URL")
  @Column(columnDefinition = "TEXT")
  private String avatar;

  @Schema(description = "User's type")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserType type;

  @Schema(description = "User's deletion date")
  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Schema(description = "Consecutive failed login attempts")
  @Builder.Default
  @Column(name = "failed_login_attempts", nullable = false)
  private Integer failedLoginAttempts = 0;

  @Schema(description = "Moment until which the account is locked after too many failed logins")
  @Column(name = "locked_until")
  private Instant lockedUntil;
}