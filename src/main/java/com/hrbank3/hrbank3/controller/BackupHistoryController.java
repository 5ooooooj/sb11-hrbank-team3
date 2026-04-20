package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.dto.backupHistory.BackupHistoryDto;
import com.hrbank3.hrbank3.service.BackupHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @Operation(summary = "데이터 백업 생성", description = "데이터 백업을 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "백업 생성 성공"),
      @ApiResponse(responseCode = "409", description = "이미 진행 중인 백업이 있음"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping
  public ResponseEntity<BackupHistoryDto> backup(HttpServletRequest request) {
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
