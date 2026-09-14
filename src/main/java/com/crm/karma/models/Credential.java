package com.crm.karma.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "credentials", uniqueConstraints = {
  @UniqueConstraint(name = "unique_credentials_user_id", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Credential extends Model {

  @Column(nullable = false)
  private String hash;

  @Column(nullable = false, name = "user_id")
  private UUID userId;
}