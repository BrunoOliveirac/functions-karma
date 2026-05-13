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

import java.util.UUID;

@Entity
@Table(name = "clients")
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
  @Column
  private String notes;

  @Schema(description = "ID of user who created it")
  @Column(nullable = false)
  private UUID userId;

  @Schema(description = "Client's budget")
  @Column(nullable = false)
  private Integer budget;

  @Schema(description = "Favorite customer indicator")
  @Column(nullable = false)
  private Boolean favorite;
}
