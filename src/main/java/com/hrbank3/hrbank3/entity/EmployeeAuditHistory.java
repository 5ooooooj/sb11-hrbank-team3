package com.hrbank3.hrbank3.entity;

import com.hrbank3.hrbank3.entity.enums.AuditType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "employee_audit_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class EmployeeAuditHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "audit_type", nullable = false, length = 20)
  private AuditType auditType;

  @Column(name = "target_employee_no", nullable = false, length = 50)
  private String targetEmployeeNo;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "changed_content", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> changedContent;

  @Column(name = "memo", length = 255)
  private String memo;

  @Column(name = "ip_address", nullable = false, length = 50)
  private String ipAddress;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Builder
  public EmployeeAuditHistory(AuditType auditType, String targetEmployeeNo,
      Map<String, Object> changedContent, String memo, String ipAddress) {
    this.auditType = auditType;
    this.targetEmployeeNo = targetEmployeeNo;
    this.changedContent = changedContent;
    this.memo = memo;
    this.ipAddress = ipAddress;
  }
}