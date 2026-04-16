package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.repository.condition.BackupHistorySearchCondition;
import java.util.List;

public interface BackupHistoryRepositoryCustom {
  List<BackupHistory> findAllByCondition(BackupHistorySearchCondition condition);
}
