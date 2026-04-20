package com.hrbank3.hrbank3.entity.enums;

// 정렬 타입을 enum으로 만들어서 유효하지 않은 값 입력 받지 않게
public enum BackupHistorySortType {
  STARTED_AT_ASC,
  STARTED_AT_DESC,
  ENDED_AT_ASC,
  ENDED_AT_DESC,
  STATUS_ASC,
  STATUS_DESC;

  public static BackupHistorySortType from(String sortField, String sortDirection) {
    try {
      String converted = sortField
          .replaceAll("([a-z])([A-Z])", "$1_$2")
          .toUpperCase();
      return BackupHistorySortType.valueOf(
          converted + "_" + sortDirection.toUpperCase()
      );
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("지원하지 않는 정렬 필드입니다: " + sortField);
    }
  }
}
