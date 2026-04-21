package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.entity.Department;
import com.hrbank3.hrbank3.entity.Notification;
import com.hrbank3.hrbank3.entity.enums.NotificationStatus;
import com.hrbank3.hrbank3.event.BackupNotificationEvent;
import com.hrbank3.hrbank3.event.EmployeeNotificationEvent;
import com.hrbank3.hrbank3.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final JavaMailSender mailSender;
  private final NotificationRepository notificationRepository;

  @Value("${notification.admin-email}")
  private String adminEmail;

  // 직원 알림 이벤트
  @EventListener
  @Transactional
  public void handleEmployeeNotification(EmployeeNotificationEvent event) {
    Department department = event.employee().getDepartment();
    String recipientEmail = adminEmail;
    String subject = event.eventType();
    String content = buildEmployeeContent(event);

    sendAndSave(event.eventType(), department, recipientEmail, subject, content);
  }

  // 백업 알림 이벤트
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

  // 실제 메일 발송
  private void sendMail(String to, String subject, String content) throws MessagingException {
    // Multipurpose Internet Mail Extension : 한글, 첨부파일, HTML 지원
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setTo(to);
    helper.setSubject(subject);
    // 현재는 단순 텍스트라서 true로 변경시 HTML 형식으로 바꾸고 build*Content() 내용 수정해야 함
    helper.setText(content, false);
    mailSender.send(message);
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

    if (event.eroorMessage() != null) {
      sb.append("원인: ").append(event.eroorMessage());
    }

    return sb.toString();
  }
}
