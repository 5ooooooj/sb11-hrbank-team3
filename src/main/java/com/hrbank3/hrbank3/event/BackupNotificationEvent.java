package com.hrbank3.hrbank3.event;

import java.time.Instant;

public record BackupNotificationEvent(
    String eventType, // BACKUP_SUCCESS, BACKUP_FAILED
    Instant startedAt,
    Instant endedAt,  // 완료(실패) 시각
    String eroorMessage // 성공시 null
) {

}
