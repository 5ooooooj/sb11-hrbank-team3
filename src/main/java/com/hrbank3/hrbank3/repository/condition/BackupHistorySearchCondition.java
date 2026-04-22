package com.hrbank3.hrbank3.repository.condition;

import com.hrbank3.hrbank3.entity.enums.BackupHistorySortType;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackupHistorySearchCondition {
  private String worker;
  // 프론트에선 한국 기준으로 보여주므로 ZonedDateTime 사용
  private ZonedDateTime startedAtFrom;
  private ZonedDateTime startedAtTo;
  private BackupStatus status;
  private BackupHistorySortType sortType; // 정렬 조건
  private Long lastId; // 이전 페이지 마지막 요소 ID
  private String cursor;
  private int pageSize;
}
