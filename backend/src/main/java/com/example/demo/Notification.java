package com.example.demo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "notification")
public class Notification {
  @Id
  @Column(name = "notification_id", nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long notificationId;

  private String title;

  private String content;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @Builder
  public Notification(String title, String content) {
    this.title = title;
    this.content = content;
  }

}
