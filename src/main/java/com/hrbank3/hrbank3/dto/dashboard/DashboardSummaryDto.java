package com.hrbank3.hrbank3.dto.dashboard;

import java.time.Instant;

// 대시보드 카드형 요약 정보 응답 DTO
public record DashboardSummaryDto(
        long totalEmployeeCount,
        long recentAuditCount,
        long thisMonthHireCount,
        Instant lastBackupAt
) {}