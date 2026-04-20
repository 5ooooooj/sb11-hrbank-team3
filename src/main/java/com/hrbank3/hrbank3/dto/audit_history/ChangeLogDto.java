package com.hrbank3.hrbank3.dto.audit_history;

import com.hrbank3.hrbank3.entity.enums.AuditType;
import java.time.Instant;

public record ChangeLogDto(
    Long id,
    AuditType type,
    String employeeNumber,
    String memo,
    String ipAddress,
    Instant at
) {

}