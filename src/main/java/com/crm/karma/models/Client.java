package com.crm.karma.models;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
@Table(name = "clients", indexes = {
  @Index(name = "idx_clients_user_email_active", columnList = "user_id, email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Client extends Model {

  @Schema(description = "Client's name")
  @Column(nullable = false)
  private String name;

  @Schema(description = "Client's e-mail address")
  @Column(nullable = false)
  private String email;


  @Schema(description = "Client's phone")
  @Column(nullable = false)
  private String phone;

  @Schema(description = "Notes (description or observation) about the client")
  @Column(length = 500)
  @Size(max = 500, message = "Notes should have a maximum of 500 characters")
  private String notes;

  @Schema(description = "ID of user who created it")
  @Column(nullable = false, name = "user_id")
  private UUID userId;

  @Schema(description = "Client's budget")
  @Column(nullable = false)
  private Integer budget;

  @Schema(description = "Favorite customer indicator")
  @Column(nullable = false)
  private Boolean favorite = false;

  @Schema(description = "Client's deletion date")
  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Schema(description = "ID sector linked to Client")
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "sector_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private Sector sector;
}
