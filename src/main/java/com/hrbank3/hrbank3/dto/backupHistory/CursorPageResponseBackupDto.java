package com.hrbank3.hrbank3.dto.backupHistory;

import com.hrbank3.hrbank3.entity.enums.BackupHistorySortType;
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
      String cursorBase = switch (sortType) {
        case ENDED_AT_ASC, ENDED_AT_DESC ->
            content.get(content.size() - 1).endedAt().toInstant().toString();
            .toInstant().toString();
        case STATUS_ASC, STATUS_DESC ->
            content.get(content.size() - 1).status();
        default ->
            content.get(content.size() - 1).startedAt().toInstant().toString();
      };
      nextCursor = Base64.getEncoder().encodeToString(cursorBase.getBytes());
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
