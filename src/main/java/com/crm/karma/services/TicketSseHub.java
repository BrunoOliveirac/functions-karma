package com.crm.karma.services;

import com.crm.karma.responses.TicketStreamEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TicketSseHub {

  static final long EMITTER_TIMEOUT_MS = 30 * 60 * 1000L;

  private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emitters =
    new ConcurrentHashMap<>();

  /**
   * Register a new SSE connection for the given user.
   *
   * @param userId Recipient user ID
   * @return Open emitter bound to that user
   */
  public SseEmitter subscribe(UUID userId) {
    SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
    emitters.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

    emitter.onCompletion(() -> remove(userId, emitter));
    emitter.onTimeout(() -> completeAndRemove(userId, emitter));
    emitter.onError(error -> remove(userId, emitter));

    try {
      emitter.send(SseEmitter.event().comment("connected"));
    } catch (IOException e) {
      completeAndRemove(userId, emitter);
    }

    return emitter;
  }

  /**
   * Notify every open tab of the recipient that a ticket changed.
   * The payload is already a plain object, so it stays valid after commit.
   *
   * @param userId Recipient user ID
   * @param event  Ticket plus the status it had before the change
   */
  public void publish(UUID userId, TicketStreamEvent event) {
    if (userId == null || event == null) return;

    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            emit(userId, event);
          }
        }
      );

      return;
    }

    emit(userId, event);
  }

  private void emit(UUID userId, TicketStreamEvent event) {
    List<SseEmitter> userEmitters = emitters.get(userId);
    if (userEmitters == null || userEmitters.isEmpty()) return;

    for (SseEmitter emitter : userEmitters) {
      try {
        emitter.send(
          SseEmitter
            .event()
            .id(event.ticket().id().toString())
            .data(event)
        );
      } catch (IOException e) {
        completeAndRemove(userId, emitter);
      }
    }
  }

  /**
   * Keep idle SSE connections alive through proxies.
   */
  @Scheduled(fixedRate = 15_000)
  public void heartbeat() {
    emitters.forEach((userId, userEmitters) -> {
      for (SseEmitter emitter : userEmitters) {
        try {
          emitter.send(SseEmitter.event().comment("keepalive"));
        } catch (IOException e) {
          completeAndRemove(userId, emitter);
        }
      }
    });
  }

  private void completeAndRemove(UUID userId, SseEmitter emitter) {
    try {
      remove(userId, emitter);
      emitter.complete();
    } catch (Exception ignored) {
      // Already completed or the client already dropped the connection.
    }
  }

  private void remove(UUID userId, SseEmitter emitter) {
    emitters.computeIfPresent(userId, (id, userEmitters) -> {
      userEmitters.remove(emitter);
      return userEmitters.isEmpty() ? null : userEmitters;
    });
  }
}
