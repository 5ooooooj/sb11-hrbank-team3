package com.hrbank3.hrbank3.service.basic;

import com.hrbank3.hrbank3.dto.backupHistory.BackupHistoryDto;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.entity.BackupStatus;
import com.hrbank3.hrbank3.mapper.BackupHistoryMapper;
import com.hrbank3.hrbank3.repository.BackupHistoryRepository;
import com.hrbank3.hrbank3.service.BackupHistoryService;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicBackupHistoryService implements BackupHistoryService {

  private final BackupHistoryRepository backupHistoryRepository;
  private final EmployeeRepository employeeRepository;
  private final BackupHistoryMapper backupHistoryMapper;

  // create
  @Transactional
  public BackupHistoryDto create(String worker) {
    // 백업 필요 여부 판단
    boolean backupNeeded = isBackupNeeded();

    // 백업 필요 없으면 skipped 상태로 저장 후 종료
    BackupHistory history = backupNeeded
        ? BackupHistory.ofInProgress(worker)
        : BackupHistory.ofSkipped(worker);

    BackupHistory savedHistory = backupHistoryRepository.save(history);
    return backupHistoryMapper.toDto(savedHistory);
  }

  // 백업 필요한지 판단 로직
  private boolean isBackupNeeded() {
    Optional<BackupHistory> lastCompleted =
        backupHistoryRepository.findTopByStatusOrderByStartedAtDesc(BackupStatus.COMPLETED);

    if (lastCompleted.isEmpty()) {
      return true; // 완료된 백업 이력 자체가 없으면 백업 필요
    }

    Instant lastBackupTime = lastCompleted.get().getEndedAt();
    // 마지막 백업 완료된 시간 이후 직원 데이터가 변경됐는지 확인하는 메소드
    return employeeRepository.existsByUpdatedAtAfter(lastBackupTime);
  }

  // TODO : 이후 메서드 구현
}
