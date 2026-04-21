package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.common.exception.ErrorResponse;
import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDetailDto;
import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDto;
import com.hrbank3.hrbank3.repository.condition.ChangeLogSearchCondition;
import com.hrbank3.hrbank3.service.EmployeeAuditHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "직원 정보 수정 이력 관리", description = "직원 정보 수정 이력 관리 API")
@RestController
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
public class EmployeeAuditHistoryController {

  private final EmployeeAuditHistoryService auditHistoryService;

  @Operation(summary = "직원 정보 수정 이력 상세 조회", description = "직원 정보 수정 이력의 상세 정보를 조회합니다. 변경 상세 내용이 포함됩니다.")
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
    ChangeLogDetailDto detailDto = auditHistoryService.find(id);
    return ResponseEntity.ok(detailDto);
  }

  @Operation(summary = "직원 정보 수정 이력 목록 조회", description = "직원 정보 수정 이력 목록을 조회합니다. 상세 변경 내용은 포함되지 않습니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 지원하지 않는 정렬 필드"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @GetMapping
  public ResponseEntity<CursorPageResponseDto<ChangeLogDto>> findAll(
      @ParameterObject @ModelAttribute ChangeLogSearchCondition condition) {
    return ResponseEntity.ok(auditHistoryService.findAll(condition));
  }

  @Operation(summary = "수정 이력 건수 조회", description = "직원 정보 수정 이력 건수를 조회합니다. 파라미터를 제공하지 않으면 최근 일주일 데이터를 반환합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "조회 성공"),

  })
  @GetMapping("/count")
  public ResponseEntity<Long> getCount(
          @Parameter(description = "시작 일시 (기본값: 7일 전)")
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
          @Parameter(description = "종료 일시 (기본값: 현재)")
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate
  ) {
    return ResponseEntity.ok(auditHistoryService.getCount(fromDate, toDate));
  }
}