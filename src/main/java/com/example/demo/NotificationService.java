package com.example.demo;

import java.io.IOException;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

  private final EmitterRepository emitterRepository;
  private final NotificationRepository notificationRepository;

  public SseEmitter createEmitter(String lastEventId) {
    String emitterId = makeTimeIncludeId();
    SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

    // SseEmitter가 완료되거나 타임아웃될 때 해당 SseEmitter를 emitterRepository에서 삭제
    emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
    emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

    // 첫 접속 시 503 에러를 방지하기 위한 더미 이벤트 전송
    String eventId = makeTimeIncludeId();
    sendNotification(emitter, eventId, emitterId, "EventStream Created.");

    // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
    if (!lastEventId.isEmpty()) {
      sendLostData(lastEventId, emitterId, emitter);
    }

    return emitter;
  }

  @Transactional
  public void send (String title, String content) {
    Notification notification = Notification.builder().title(title).content(content).build();
    notificationRepository.save(notification);

    String eventId = "Event_" + System.currentTimeMillis();

    // 원래는 memberId로 찾아야 하나, 예제 프로젝트에서는 따로 유저가 존재하지 않기 때문에 임의로 "Unique"로 찾도록 함.
    Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByMemberId("Unique");
    emitters.forEach(
        (key, emitter) -> {
          emitterRepository.saveEventCache(key, notification);

          sendNotification(emitter, eventId, key, NotificationDTO.builder().id(notification.getNotificationId()).title(title).content(content).createdAt(notification.getCreatedAt()).build());
        }
    );
  }

  private String makeTimeIncludeId() {
    /**
     * 원래는 Unique 대신 유저의 이메일 주소 등 유저를 식별할 수 있는 값을 PREFIX로 추가
     */

    return "Unique_" + System.currentTimeMillis();
  }

  private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
    try {
      System.out.println("Sending data: " + data.toString());

      emitter.send(SseEmitter.event()
          .id(eventId)
          .name("sse-notification")
          .data(data)
          .reconnectTime(3000L)  // 클라이언트에서 연결이 끊기고 3초마다 재연결을 시도
      );
    } catch (IOException exception) {
      emitterRepository.deleteById(emitterId);
    }
  }

  private void sendLostData(String lastEventId, String emitterId, SseEmitter emitter) {
    Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByMemberId("Unique");
    eventCaches.entrySet().stream()
        .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
        .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
  }
}
