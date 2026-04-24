package com.hrbank3.hrbank3.dto.dashboard;

// 부서별 직원 분포 응답 DTO
public record DepartmentDistributionDto(
    Long departmentId,
    String departmentName,
    long employeeCount
) {

}