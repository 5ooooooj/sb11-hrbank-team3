package com.hrbank3.hrbank3.entity;

import com.hrbank3.hrbank3.entity.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String eventType;

  @Column(name = "department_id")
  private Long departmentId;

  // TODO: 하빈님 department_mail 컬럼 추가 후 연동 필요
  // 현재는 하드코딩, 추후 department.mail로 교체 예정
  @Column(nullable = false)
  private String recipientEmail;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationStatus status;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant updatedAt;

  @Builder
  public static Notification create(String eventType, Long departmentId, String recipientEmail,
      String content, NotificationStatus status) {
    Notification notification = new Notification();
    notification.eventType = eventType;
    notification.departmentId = departmentId;
    notification.recipientEmail = recipientEmail;
    notification.content = content;
    notification.status = status;
    return notification;
  }
}
