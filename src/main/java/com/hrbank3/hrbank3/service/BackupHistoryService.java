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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackupHistoryService {

  private static final int PAGE_SIZE_OFFSET = 1;

  private final BackupHistoryRepository backupHistoryRepository;
  private final EmployeeRepository employeeRepository;
  private final BackupHistoryMapper backupHistoryMapper;
  private final DepartmentRepository departmentRepository;
  private final BackupHistoryTransaction backupHistoryTransaction;

  @Value("${backup.dir}")
  private String backupDir;

  @Value("${backup.chunk-size:500}")
  private int chunkSize;

  // 백업 이력 조회
  @Transactional(readOnly = true)
  public CursorPageResponseBackupDto getBackupHistories(BackupHistorySearchCondition condition) {
    // hasNext 판단을 위해 pageSize + 1개 조회
    List<BackupHistory> histories = backupHistoryRepository.findAllByCondition(condition);

    boolean hasNext = histories.size() > condition.getPageSize();
    if (hasNext) {
      histories = histories.subList(0, condition.getPageSize());
    }

    List<BackupHistoryDto> content = histories.stream()
        .map(backupHistoryMapper::toDto)
        .toList();

    long totalElements = backupHistoryRepository.countByCondition(condition);

    return CursorPageResponseBackupDto.of(content, hasNext, totalElements, condition.getSortType());
  }

  // 최신 백업 조회 메서드
  @Transactional(readOnly = true)
  public Optional<BackupHistoryDto> getLatestBackup(String status) {
    BackupStatus backupStatus;
    try {
      backupStatus = BackupStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 상태값입니다: " + status);
    }
    return backupHistoryRepository.findTopByStatusOrderByStartedAtDesc(backupStatus)
        .map(backupHistoryMapper::toDto);
  }

  // 백업 생성 메서드
  public BackupHistoryDto backup(String worker) {

    BackupHistory history = backupHistoryTransaction.initiate(worker); // 내부에 별도 트랜잭션

    // history가 SKIPPED면 바로 반환
    if (history.getStatus() == BackupStatus.SKIPPED) {
      return backupHistoryMapper.toDto(history);
    }

    // 아니면 저장 로직 실행
    return execute(history);
  }

  // 실제 백업 수행 및 성공/실패 처리, 성공이면 COMPLETED, 실패면 CSV 삭제 후 에러 로그 저장 후 FAILED
  private BackupHistoryDto execute(BackupHistory history) {
    Path csvPath = null;
    try {
      csvPath = writeCsv();
      FileMetadata file = backupHistoryTransaction.saveFileMetadata(csvPath, "text/csv");
      backupHistoryTransaction.complete(history, file);
    } catch (Exception e) {
      deleteSilently(csvPath);
      try {
        Path logPath = writeErrorLog(e);
        FileMetadata logFile = backupHistoryTransaction.saveFileMetadata(logPath, "text/plain");
        backupHistoryTransaction.fail(history, logFile);
      } catch (IOException logException) {
        // 로그 저장도 실패 시 파일 없이 FAILED 처리
        backupHistoryTransaction.fail(history, null);
      }
    }
    return backupHistoryMapper.toDto(history);
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



}
