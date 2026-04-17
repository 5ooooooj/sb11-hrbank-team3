package com.hrbank3.hrbank3.dto.dashboard;

// 직무별 직원 분포 응답 DTO
public record PositionDistributionDto(
        String positionName,
        long employeeCount
) {}