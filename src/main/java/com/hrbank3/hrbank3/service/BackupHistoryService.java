package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.backupHistory.BackupHistoryDto;
import com.hrbank3.hrbank3.dto.backupHistory.CursorPageResponseBackupDto;
import com.hrbank3.hrbank3.entity.Department;
import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.entity.FileMetadata;
import com.hrbank3.hrbank3.repository.DepartmentRepository;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import com.hrbank3.hrbank3.entity.BackupHistory;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.mapper.BackupHistoryMapper;
import com.hrbank3.hrbank3.repository.BackupHistoryRepository;
import com.hrbank3.hrbank3.repository.FileMetadataRepository;
import com.hrbank3.hrbank3.repository.condition.BackupHistorySearchCondition;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BackupHistoryService {

  private static final int PAGE_SIZE_OFFSET = 1;

  private final BackupHistoryRepository backupHistoryRepository;
  private final EmployeeRepository employeeRepository;
  private final BackupHistoryMapper backupHistoryMapper;
  private final DepartmentRepository departmentRepository;
  private final FileMetadataRepository fileMetadataRepository;

  @Value("${backup.dir}")
  private String backupDir;

  @Value("${backup.chunk-size:500}")
  private int chunkSize;

  // 백업 이력 조회
  public CursorPageResponseBackupDto getBackupHistories(BackupHistorySearchCondition condition) {
    // hasNext 판단을 위해 pageSize + 1개 조회
    condition.setPageSize(condition.getPageSize() + PAGE_SIZE_OFFSET);

    List<BackupHistory> histories = backupHistoryRepository.findAllByCondition(condition);

    boolean hasNext = histories.size() == condition.getPageSize();
    if (hasNext) {
      histories = histories.subList(0, histories.size() - PAGE_SIZE_OFFSET);
    }

    List<BackupHistoryDto> content = histories.stream()
        .map(backupHistoryMapper::toDto)
        .toList();

    long totalElements = backupHistoryRepository.countByCondition(condition);

    return CursorPageResponseBackupDto.of(content, hasNext, totalElements, condition.getSortType());
  }

  // 백업 생성 메서드
  @Transactional
  public BackupHistoryDto backup(String worker) {

    BackupHistory history = initiate(worker);

    // history가 SKIPPED면 바로 반환
    if (history.getStatus() == BackupStatus.SKIPPED) {
      return backupHistoryMapper.toDto(history);
    }

    // 아니면 저장 로직 실행
    return execute(history);
  }

  // 백업 필요 여부 반한 후 IN_PROGRESS 또는 SKIPPED 이력 저장 메서드
  private BackupHistory initiate(String worker) {
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

  // 실제 백업 수행 및 성공/실패 처리, 성공이면 COMPLETED, 실패면 CSV 삭제 후 에러 로그 저장 후 FAILED
  private BackupHistoryDto execute(BackupHistory history) {
    Path csvPath = null;
    try {
      csvPath = writeCsv();
      FileMetadata file = saveFileMetadata(csvPath, "text/csv");
      history.updateComplete(file);
    } catch (Exception e) {
      deleteSilently(csvPath);
      try {
        Path logPath = writeErrorLog(e);
        FileMetadata logFile = saveFileMetadata(logPath, "text/plain");
        history.updateFail(logFile);
      } catch (IOException logException) {
        // 로그 저장도 실패 시 파일 없이 FAILED 처리
        history.updateFail(null);
      }
    }
    return backupHistoryMapper.toDto(backupHistoryRepository.save(history));
  }

  // 청크(500건) 단위로 직원 조회 후 CSV 파일 생성
  private Path writeCsv() throws IOException {
    String fileName = "backup-" + Instant.now()
        .atZone(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        + ".csv";
    Path csvPath = Paths.get(backupDir, fileName);
    Files.createDirectories(csvPath.getParent());

    try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardCharsets.UTF_8)) {
      writer.write(
          "name,email,employeeNumber,departmentName,position,hireDate,status,profileImageId");
      writer.newLine();

      // page = 0 이면 0~499번 조회 후 hasNext가 true면 page++1
      // false일때까지 조회 후 반복문 종료
      int page = 0;
      boolean hasNext = true;

      while (hasNext) {
        Slice<Employee> slice = employeeRepository.findAllWithProfileImage(
            PageRequest.of(page, chunkSize, Sort.by("id").ascending())
        );

        // 청크 내 departmentId 모아서 한번에 조회
        List<Long> departmentIds = slice.getContent().stream()
            .map(e -> e.getDepartment().getId())
            .distinct()
            .toList();

        Map<Long, String> departmentNameMap = departmentRepository.findAllById(departmentIds)
            .stream()
            .collect(Collectors.toMap(Department::getId, Department::getName));

        // csv 작성
        for (Employee emp : slice.getContent()) {
          writer.write(toCsvRow(emp, departmentNameMap));
          writer.newLine();
        }

        // 다음 청크 있는지 확인
        hasNext = slice.hasNext();
        page++;
      }
    }
    return csvPath;
  }

  // 에외 메시지와 스택트레이스를 .log 파일로 저장
  private Path writeErrorLog(Exception e) {
    String fileName = "backup-error-" + Instant.now()
        .atZone(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        + ".log";
    Path logPath = Paths.get(backupDir, fileName);

    try {
      Files.createDirectories(logPath.getParent());
      try (BufferedWriter writer = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8);
          PrintWriter printWriter = new PrintWriter(writer)) {
        e.printStackTrace(printWriter); // 전체 스택트레이스 출력
      }
    } catch (IOException ioException) {
      throw new RuntimeException("에러 로그 파일 저장 실패: ", ioException);
    }
    return logPath;
  }

  // Employee 한 명을 csv 한 줄로 변환
  private String toCsvRow(Employee emp, Map<Long, String> departmentNameMap) {
    return String.join(",",
        escapeCsv(emp.getName()),
        escapeCsv(emp.getEmail()),
        escapeCsv(emp.getEmployeeNumber()),
        escapeCsv(departmentNameMap.getOrDefault(emp.getDepartment().getId(), "")),
        escapeCsv(emp.getPosition()),
        emp.getHireDate().toString(),
        emp.getStatus().name(),
        emp.getProfileImage() != null ? String.valueOf(emp.getProfileImage().getId()) : ""
    );
  }

  // csv 형식에서 특수 문자가 포함된 값을 처리하기 위한 메서드
  private String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  // 실패 처리 시 저장중이던 csv 파일 삭제 메서드. 파일 삭제가 실패해도 예외 던지지 않고 무시
  private void deleteSilently(Path path) {
    if (path != null) {
      try {
        Files.deleteIfExists(path);
      } catch (IOException ignored) {
      }
    }
  }

  // Path를 받아 FileMetadata 엔티티 생성 후 db 저장
  private FileMetadata saveFileMetadata(Path filePath, String contentType) throws IOException {
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
