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

@Entity
@Table(name = "user_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserMember extends Model {

  @Schema(description = "User of the member is linked")
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User user;

  @Schema(description = "member linked to the user")
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "member_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User member;
}
