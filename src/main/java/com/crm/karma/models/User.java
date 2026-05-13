package com.crm.karma.models;

import com.crm.karma.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

  @Schema(description = "User's type")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserType type;

}