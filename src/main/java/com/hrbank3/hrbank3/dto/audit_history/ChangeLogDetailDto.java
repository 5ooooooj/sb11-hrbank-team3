package com.hrbank3.hrbank3.dto.audit_history;

import com.hrbank3.hrbank3.entity.enums.AuditType;
import java.time.Instant;
import java.util.List;

public record ChangeLogDetailDto(
    Long id,
    AuditType type,
    String employeeNumber,
    String memo,
    String ipAddress,
    Instant at,
    String employeeName,
    Long profileImageId,
    List<DiffDto> diffs
) {

}