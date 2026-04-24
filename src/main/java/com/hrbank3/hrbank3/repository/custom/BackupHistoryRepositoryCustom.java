package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.dto.backup_history.BackupHistorySearchCondition;
import com.hrbank3.hrbank3.entity.BackupHistory;
import java.util.List;

public interface BackupHistoryRepositoryCustom {

  List<BackupHistory> findAllByCondition(BackupHistorySearchCondition condition);

  long countByCondition(BackupHistorySearchCondition condition);
}
