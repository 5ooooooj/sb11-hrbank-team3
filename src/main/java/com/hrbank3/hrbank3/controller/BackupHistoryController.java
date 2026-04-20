package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.dto.backupHistory.BackupHistoryDto;
import com.hrbank3.hrbank3.dto.backupHistory.CursorPageResponseBackupDto;
import com.hrbank3.hrbank3.entity.enums.BackupHistorySortType;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.repository.condition.BackupHistorySearchCondition;
import com.hrbank3.hrbank3.service.BackupHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "데이터 백업 관리", description = "데이터 백업 관리 API")
@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
public class BackupHistoryController {

  private final BackupHistoryService backupHistoryService;

  private static final String[] IP_HEADERS = {
      "X-Forwarded-For",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_CLIENT_IP",
      "HTTP_X_FORWARDED_FOR"
  };

  @Operation(summary = "최근 백업 정보 조회", description = "지정된 상태의 가장 최근 백업 정보를 조회합니다. 상태를 지정하지 않으면 성공적으로 완료된(COMPLETED) 백업을 반환합니다.")
  @GetMapping("/latest")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효하지 않은 상태값"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<BackupHistoryDto> getLatestBackup(
      @RequestParam(defaultValue = "COMPLETED") String status) {
    return backupHistoryService.getLatestBackup(status)
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new NoSuchElementException("백업 이력이 없습니다"));
  }

  // 데이터 백업 목록 조회 api (GET)
  @Operation(summary = "데이터 백업 목록 조회", description = "조건에 따른 데이터 백업 목록을 조회합니다.")
  @GetMapping
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 정렬 필드"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  public ResponseEntity<CursorPageResponseBackupDto> getAllBackups(
      @RequestParam(required = false) String worker,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startedAtFrom,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startedAtTo,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "startedAt") String sortField,
      @RequestParam(defaultValue = "DESC") String sortDirection
  ) {
    BackupHistorySortType sortType = BackupHistorySortType.from(sortField, sortDirection);
    // from()에서 지원하지 않는 sortField면 400 예외 던지도록 구현

    BackupHistorySearchCondition condition = new BackupHistorySearchCondition();
    condition.setWorker(worker);
    condition.setStatus(status != null ? BackupStatus.valueOf(status) : null);
    condition.setStartedAtFrom(startedAtFrom);
    condition.setStartedAtTo(startedAtTo);
    condition.setCursor(cursor);
    condition.setLastId(idAfter);
    condition.setSortType(sortType);
    condition.setPageSize(size);

    return ResponseEntity.ok(backupHistoryService.getBackupHistories(condition));

  }

  // 수동 백업 api (POST)
  @Operation(summary = "데이터 백업 생성", description = "데이터 백업을 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "백업 생성 성공"),
      @ApiResponse(responseCode = "409", description = "이미 진행 중인 백업이 있음"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping
  public ResponseEntity<BackupHistoryDto> createBackup(HttpServletRequest request) {
    String worker = extractClientIp(request);
    return ResponseEntity.ok(backupHistoryService.backup(worker));
  }

  // IP 추출 메서드
  private String extractClientIp(HttpServletRequest request) {
    String ip = null;
    for (String header : IP_HEADERS) {
      ip = request.getHeader(header);
      if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
        break;
      }
    }

    if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }

    // x-Forwarded-For 헤더는 프록시를 거칠 때마다 IP를 뒤에 추가하는 방식
    // 첫번째 IP가 실제 클라이언트일 가능성이 높음
    if (ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }

    return ip;
  }
}
