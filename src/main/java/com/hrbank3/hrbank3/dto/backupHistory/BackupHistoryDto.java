package com.hrbank3.hrbank3.dto.backupHistory;

import com.hrbank3.hrbank3.entity.BackupHistory;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record BackupHistoryDto(
    Long id,
    String worker,
    ZonedDateTime startedAt, // Instant에서 ZonedDateTime 변환
    ZonedDateTime endedAt,
    String status,
    Long fileId // FileMetaData에서 id만 추출
) {
}
