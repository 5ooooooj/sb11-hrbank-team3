package com.hrbank3.hrbank3.dto.backupHistory;

import com.hrbank3.hrbank3.entity.enums.BackupHistorySortType;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.repository.condition.BackupHistorySearchCondition;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class BackupHistorySearchRequest {
    private String worker;
    private BackupStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime startedAtFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime startedAtTo;

    private Long idAfter;
    private String cursor;
    private int size = 10;
    private String sortField = "startedAt";
    private String sortDirection = "DESC";

    public BackupHistorySearchCondition toCondition() {
      return BackupHistorySearchCondition.builder()
          .worker(worker)
          .status(status)
          .startedAtFrom(startedAtFrom)
          .startedAtTo(startedAtTo)
          .cursor(cursor)
          .lastId(idAfter)
          .sortType(BackupHistorySortType.from(sortField, sortDirection))
          .pageSize(size)
          .build();
  }
}
