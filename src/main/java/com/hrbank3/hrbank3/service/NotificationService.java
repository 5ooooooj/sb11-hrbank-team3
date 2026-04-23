package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.entity.Department;
import com.hrbank3.hrbank3.entity.Notification;
import com.hrbank3.hrbank3.entity.enums.NotificationStatus;
import com.hrbank3.hrbank3.event.BackupNotificationEvent;
import com.hrbank3.hrbank3.event.EmployeeNotificationEvent;
import com.hrbank3.hrbank3.repository.NotificationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final RestTemplate restTemplate;

  @Value("${notification.admin-email}")
  private String adminEmail;

  @Value("${mailtrap.api-token:}")
  private String mailtrapApiToken;

  private static final String MAILTRAP_API_URL = "https://sandbox.api.mailtrap.io/api/send/4562077";

  // 직원 알림 이벤트
  @Async
  @EventListener
  @Transactional
  public void handleEmployeeNotification(EmployeeNotificationEvent event) {
    Department department = event.employee().getDepartment();
    // null 체크 추가
    String recipientEmail = department.getDepartmentMail() != null
        ? department.getDepartmentMail()
        : adminEmail;
    String subject = event.eventType();
    String content = buildEmployeeContent(event);

    sendAndSave(event.eventType(), department, recipientEmail, subject, content);
  }

  // 백업 알림 이벤트
  @Async
  @EventListener
  @Transactional
  public void handleBackupNotification(BackupNotificationEvent event) {
    String subject = event.eventType();
    String content = buildBackupContent(event);

    sendAndSave(event.eventType(), null, adminEmail, subject, content);
  }

  // 메일 발송 + DB 저장
  private void sendAndSave(String eventType, Department department,
      String recipientEmail, String subject, String content) {
    NotificationStatus status;
    try {
      sendMail(recipientEmail, subject, content);
      status = NotificationStatus.SUCCESS;
    } catch (Exception e) {
      log.error("메일 발송 실패 : {}", e.getMessage());
      status = NotificationStatus.FAILED;
    }

    Notification notification = Notification.create(
        eventType,
        department,
        recipientEmail,
        content,
        status
    );
    notificationRepository.save(notification);
  }

  // Mailtrap API로 메일 발송
  private void sendMail(String to, String subject, String content) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(mailtrapApiToken);

    Map<String, Object> body = new HashMap<>();
    body.put("from", Map.of("email", "sb11_team3@hrbank.com", "name", "HRBANK"));
    body.put("to", List.of(Map.of("email", to)));
    body.put("subject", subject);
    body.put("html", content);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
    restTemplate.postForObject(MAILTRAP_API_URL, request, String.class);
  }

  // 직원 알림 메일 내용
  private String buildEmployeeContent(EmployeeNotificationEvent event) {
    String action = "EMPLOYEE_CREATE".equals(event.eventType())
        ? "직원 정보가 생성되었습니다."
        : "퇴사로 변경되었습니다.";
    return String.format(
        "%s님의 %s\n\n" +
            "사원번호: %s\n" +
            "부서: %s\n" +
            "직함: %s\n" +
            "입사일: %s",
        event.employee().getName(),
        action,
        event.employee().getEmployeeNumber(),
        event.employee().getDepartment().getName(),
        event.employee().getPosition(),
        event.employee().getHireDate()
    );
  }

  // 백업 알림 메일 내용
  private String buildBackupContent(BackupNotificationEvent event) {
    String result = "BACKUP_SUCCESS".equals(event.eventType())
        ? "백업이 성공적으로 완료되었습니다."
        : "백업이 실패하였습니다.";

    StringBuilder sb = new StringBuilder();
    sb.append(result).append("\n\n");
    sb.append("시작 시각: ").append(event.startedAt()).append("\n");
    sb.append("완료(실패) 시각: ").append(event.endedAt()).append("\n");

    if (event.errorMessage() != null) {
      sb.append("원인: ").append(event.errorMessage());
    }

    return sb.toString();
  }
}
