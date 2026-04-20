package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.entity.FileMetadata;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.repository.BackupHistoryRepository;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import com.hrbank3.hrbank3.repository.FileMetadataRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackupHistoryTransaction {
    private final BackupHistoryRepository backupHistoryRepository;
    private final EmployeeRepository employeeRepository;
    private final FileMetadataRepository fileMetadataRepository;

  // 백업 필요 여부 반한 후 IN_PROGRESS 또는 SKIPPED 이력 저장 메서드
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public BackupHistory initiate(String worker) {
    // IN_PROGRESS 중복 체크
    boolean isInProgress = backupHistoryRepository.existsByStatus(BackupStatus.IN_PROGRESS);

    if (isInProgress) {
      throw new IllegalStateException("이미 진행 중인 백업이 있습니다.");
    }

    // worker null 체크
    if (worker == null || worker.isBlank()) {
      throw new IllegalArgumentException("작업자 정보가 없습니다.");
    }

    // 백업 필요 없으면 skipped 상태로 저장
    BackupHistory history = isBackupNeeded()
        ? BackupHistory.ofInProgress(worker)
        : BackupHistory.ofSkipped(worker);

    return backupHistoryRepository.save(history);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(BackupHistory history, FileMetadata file) {
      history.updateComplete(file);
      backupHistoryRepository.save(history);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(BackupHistory history, FileMetadata logFile) {
      history.updateFail(logFile);
      backupHistoryRepository.save(history);
    }

  // Path를 받아 FileMetadata 엔티티 생성 후 db 저장
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public FileMetadata saveFileMetadata(Path filePath, String contentType) throws IOException {
    String originalName = filePath.getFileName().toString();
    String storedName = UUID.randomUUID().toString();
    long fileSize = Files.size(filePath);

    FileMetadata fileMetadata = new FileMetadata(
        originalName,
        storedName,
        contentType,
        fileSize,
        filePath.toString()
    );

    return fileMetadataRepository.save(fileMetadata);
  }
}