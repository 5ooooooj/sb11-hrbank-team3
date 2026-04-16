package com.hrbank3.hrbank3.repository.condition;

// 정렬 타입을 enum으로 만들어서 유효하지 않은 값 입력 받지 않게
public enum BackupHistorySortType {
  STARTED_AT_ASC,
  STARTED_AT_DESC,
  ENDED_AT_ASC,
  ENDED_AT_DESC,
  STATUS_ASC,
  STATUS_DESC
}
