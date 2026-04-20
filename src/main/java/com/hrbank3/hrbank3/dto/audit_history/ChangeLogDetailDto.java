package com.hrbank3.hrbank3.dto.audit_history;

import java.time.Instant;
import java.util.List;

public record ChangeLogDetailDto(
    Long id,
    String type,
    String employeeNumber,
    String memo,
    String ipAddress,
    Instant at,
    String employeeName,
    Long profileImageId,
    List<DiffDto> diffs
) {

}