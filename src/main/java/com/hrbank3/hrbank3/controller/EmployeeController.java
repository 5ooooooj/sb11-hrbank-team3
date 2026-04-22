package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.dto.dashboard.EmployeeDistributionDto;
import com.hrbank3.hrbank3.dto.dashboard.EmployeeTrendDto;
import com.hrbank3.hrbank3.dto.employee.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeCreateRequest;
import com.hrbank3.hrbank3.dto.employee.EmployeeDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeUpdateRequest;
import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import com.hrbank3.hrbank3.entity.enums.TrendUnit;
import com.hrbank3.hrbank3.repository.condition.EmployeeSearchCondition;
import com.hrbank3.hrbank3.service.DashboardService;
import com.hrbank3.hrbank3.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "직원 관리", description = "직원 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeService employeeService;
  private final DashboardService dashboardService;

  @Operation(summary = "직원 등록")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EmployeeDto> create(
      @RequestPart(value = "employee")
      @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      @Valid EmployeeCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request, profile));
  }

  @Operation(summary = "직원 목록 조회")
  @GetMapping
  public ResponseEntity<CursorPageResponseDto<EmployeeDto>> findAll(
      @RequestParam(required = false) String nameOrEmail,
      @RequestParam(required = false) String employeeNumber,
      @RequestParam(required = false) String departmentName,
      @RequestParam(required = false) String position,
      @RequestParam(required = false) LocalDate hireDateFrom,
      @RequestParam(required = false) LocalDate hireDateTo,
      @RequestParam(required = false) EmployeeStatus status,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sortField,
      @RequestParam(defaultValue = "asc") String sortDirection
  ) {
    EmployeeSearchCondition condition = new EmployeeSearchCondition(
        nameOrEmail, employeeNumber, departmentName, position,
        hireDateFrom, hireDateTo, status, idAfter, cursor,
        size, sortField, sortDirection
    );
    return ResponseEntity.ok(employeeService.findAll(condition));
  }

  @Operation(summary = "직원 수 조회", description = "지정된 조건에 맞는 직원 수를 조회합니다. 상태 필터링 및 입사일 기간 필터링이 가능합니다.")
  @GetMapping("/count")
  public ResponseEntity<Long> getEmployeeCount(
      @Parameter(description = "직원 상태 (재직중, 휴직중, 퇴사)", schema = @Schema(allowableValues = {"ACTIVE",
          "ON_LEAVE", "RESIGNED"}))
      @RequestParam(required = false) EmployeeStatus status,
      @Parameter(description = "입사일 시작 (지정 시 해당 기간 내 입사한 직원 수 조회, 미지정 시 전체 직원 수 조회)")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @Parameter(description = "입사일 종료 (fromDate와 함께 사용, 기본값: 현재 일시)")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
  ) {
    return ResponseEntity.ok(dashboardService.getEmployeeCount(status, fromDate, toDate));
  }

  @Operation(summary = "직원 상세 조회")
  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDto> findById(@PathVariable Long id) {
    return ResponseEntity.ok(employeeService.findById(id));
  }

  @Operation(summary = "직원 수정")
  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EmployeeDto> update(
      @PathVariable Long id,
      @RequestPart("employee") @Valid EmployeeUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    return ResponseEntity.ok(employeeService.update(id, request, profile));
  }

  @Operation(summary = "직원 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    employeeService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "직원 수 추이 조회", description = "지정된 기간 및 시간 단위로 그룹화된 직원 수 추이를 조회합니다. 파라미터를 제공하지 않으면 최근 12개월 데이터를 월 단위로 반환합니다.")
  @GetMapping("/stats/trend")
  public ResponseEntity<List<EmployeeTrendDto>> getEmployeeTrend(
      @Parameter(description = "시작 일시 (기본값: 현재로부터 unit 기준 12개 이전)")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @Parameter(description = "종료 일시 (기본값: 현재)")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @Parameter(description = "시간 단위 (day, week, month, quarter, year, 기본값: month)")
      @RequestParam(required = false, defaultValue = "month") TrendUnit unit
  ) {
    return ResponseEntity.ok(dashboardService.getEmployeeTrend(from, to, unit));
  }

  @Operation(summary = "직원 분포 조회", description = "지정된 기준으로 그룹화된 직원 분포를 조회합니다.")
  @GetMapping("/stats/distribution")
  public ResponseEntity<List<EmployeeDistributionDto>> getDistribution(
      @Parameter(description = "그룹화 기준 (department: 부서별, position: 직무별, 기본값: department)")
      @RequestParam(defaultValue = "department") String groupBy,
      @Parameter(description = "직원 상태 (재직중, 휴직중, 퇴사, 기본값: 재직중)", schema = @Schema(allowableValues = {
          "ACTIVE", "ON_LEAVE", "RESIGNED"}))
      @RequestParam(required = false) EmployeeStatus status
  ) {
    return ResponseEntity.ok(dashboardService.getDistribution(groupBy, status));
  }
}