package com.hrbank3.hrbank3.dto.dashboard;

// 직원 수 추이 응답 DTO
public record EmployeeTrendDto(
    String date,
    long count,
    long change,
    double changeRate
) {

}