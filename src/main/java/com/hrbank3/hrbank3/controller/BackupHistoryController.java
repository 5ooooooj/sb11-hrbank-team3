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

  @Operation(summary = "데이터 백업 생성", description = "데이터 백업을 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "백업 생성 성공"),
      @ApiResponse(responseCode = "409", description = "이미 진행 중인 백업이 있음"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping
  public ResponseEntity<BackupHistoryDto> create(HttpServletRequest request) {
    String worker = request.getRemoteAddr(); // IP 주소
    return ResponseEntity.ok(backupHistoryService.create(worker));
  }

}
