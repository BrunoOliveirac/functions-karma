package com.crm.karma.models;


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

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;


  @Column(nullable = false)
  private String phone;

  @Column
  private String notes;

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private Integer budget;

  @Column(nullable = false)
  private Boolean favorite;
}
