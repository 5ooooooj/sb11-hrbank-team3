package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.dto.dashboard.DashboardSummaryDto;
import com.hrbank3.hrbank3.dto.dashboard.DepartmentDistributionDto;
import com.hrbank3.hrbank3.dto.dashboard.EmployeeTrendDto;
import com.hrbank3.hrbank3.dto.dashboard.PositionDistributionDto;
import com.hrbank3.hrbank3.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "대시보드 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 카드형 요약 정보 조회
    @Operation(summary = "대시보드 요약 정보 조회")
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    // 직원 수 추이 조회
    @Operation(summary = "직원 수 추이 조회")
    @GetMapping("/employees/stats/trend")
    public ResponseEntity<List<EmployeeTrendDto>> getEmployeeTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "month") String unit
    ) {
        return ResponseEntity.ok(dashboardService.getEmployeeTrend(from, to, unit));
    }

    // 부서별 직원 분포
    @Operation(summary = "부서별 직원 분포 조회")
    @GetMapping("/dashboard/department-distribution")
    public ResponseEntity<List<DepartmentDistributionDto>> getDepartmentDistribution() {
        return ResponseEntity.ok(dashboardService.getDepartmentDistribution());
    }

    // 직무별 직원 분포
    @Operation(summary = "직무별 직원 분포 조회")
    @GetMapping("/dashboard/position-distribution")
    public ResponseEntity<List<PositionDistributionDto>> getPositionDistribution() {
        return ResponseEntity.ok(dashboardService.getPositionDistribution());
    }
}