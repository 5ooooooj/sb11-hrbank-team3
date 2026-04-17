package com.hrbank3.hrbank3.dto.backupHistory;

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
      long totalElements
  ) {
    Long nextIdAfter = hasNext ? content.get(content.size() - 1).id() : null;
    String nextCursor = nextIdAfter != null ? String.valueOf(nextIdAfter) : null;

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
