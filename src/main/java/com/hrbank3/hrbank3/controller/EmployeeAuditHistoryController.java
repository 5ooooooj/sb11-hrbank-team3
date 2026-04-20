package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.common.exception.ErrorResponse;
import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDetailDto;
import com.hrbank3.hrbank3.service.EmployeeAuditHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "직원 정보 수정 이력 관리", description = "직원 정보 수정 이력 관리 API")
@RestController
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
public class EmployeeAuditHistoryController {

  private final EmployeeAuditHistoryService auditHistoryService;

  @Operation(summary = "직원 정보 수정 이력 상세 조회", description = "특정 수정 이력의 상세 변경 내역을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(
          responseCode = "404",
          description = "이력을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      ),
      @ApiResponse(
          responseCode = "500",
          description = "서버 오류",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class))
      )
  })
  @GetMapping("/{id}")
  public ResponseEntity<ChangeLogDetailDto> getChangeLogDetail(@PathVariable("id") Long id) {
    ChangeLogDetailDto detailDto = auditHistoryService.getAuditDetail(id);
    return ResponseEntity.ok(detailDto);
  }
}