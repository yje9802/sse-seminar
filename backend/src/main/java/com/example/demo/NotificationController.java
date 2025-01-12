package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notify")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> subscribe(@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

    SseEmitter emitter = notificationService.createEmitter(lastEventId);
    return ResponseEntity.ok(emitter);
  }

  // 테스트를 위해 임의로 알림 생성 엔드포인트 구현
  @PostMapping("/send-notification")
  public ResponseEntity<String> sendNotification() {
    notificationService.send("Alarm Title", "Alarm Message");

    return ResponseEntity.ok("Notification Sent");
  }
}
