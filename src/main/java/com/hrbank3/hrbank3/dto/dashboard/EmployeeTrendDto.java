package com.hrbank3.hrbank3.dto.dashboard;

// 최근 1년 월별 직원수 변동 추이 응답 DTO
public record EmployeeTrendDto(
        String yearMonth,
        long employeeCount
) {}