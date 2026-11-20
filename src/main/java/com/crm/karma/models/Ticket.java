package com.crm.karma.models;

import com.crm.karma.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Ticket extends Model {

  @Schema(description = "Ticket's title")
  @Column(nullable = false)
  private String title;

  @Schema(description = "Ticket's description")
  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Schema(description = "Support acceptance date")
  @Column(name = "accepted_at")
  private Instant acceptedAt;

  @Schema(description = "Support acceptance date")
  @Column(name = "finished_at")
  private Instant finishedAt;

  @Schema(description = "Ticket's status")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private TicketStatus status;

  @ColumnDefault("nextval('ticket_code_seq')")
  @Column(insertable = false, updatable = false, nullable = false, unique = true)
  private Long code;

  @Schema(description = "User who generated the ticket")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.RESTRICT)
  private User user;

  @Schema(description = "Support who accepted the ticket")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "support_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User support;
}
