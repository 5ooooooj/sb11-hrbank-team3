package com.hrbank3.hrbank3.event;

import com.hrbank3.hrbank3.entity.enums.AuditType;
import java.util.Map;

// 직원 정보 변경 발생시 발행되는 이벤트 객체
public record EmployeeAuditEvent(
    AuditType auditType,
    String targetEmployeeNo,
    Map<String, Object> beforeData,
    Map<String, Object> afterData,
    String memo
) {

}