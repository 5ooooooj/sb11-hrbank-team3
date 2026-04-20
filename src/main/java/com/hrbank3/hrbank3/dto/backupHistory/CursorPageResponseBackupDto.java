package com.hrbank3.hrbank3.dto.backupHistory;

import com.hrbank3.hrbank3.entity.enums.BackupHistorySortType;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

public record CursorPageResponseBackupDto(
    List<BackupHistoryDto> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {
  public static CursorPageResponseBackupDto of(
      List<BackupHistoryDto> content,
      boolean hasNext,
      long totalElements,
      BackupHistorySortType sortType
  ) {
    Long nextIdAfter = hasNext ? content.get(content.size() - 1).id() : null;
    String nextCursor = null;

    if (hasNext && nextIdAfter != null) {
      // 정렬 기준에 따라 커서에 인코딩할 값 결정
      Instant cursorBase = switch (sortType) {
        case ENDED_AT_ASC, ENDED_AT_DESC -> content.get(content.size() - 1).endedAt()
            .toInstant();
        default -> content.get(content.size() - 1).startedAt().toInstant();
      };
      nextCursor = Base64.getEncoder().encodeToString(cursorBase.toString().getBytes());
    }

    return new CursorPageResponseBackupDto(
        content,
        nextCursor,
        nextIdAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

}
