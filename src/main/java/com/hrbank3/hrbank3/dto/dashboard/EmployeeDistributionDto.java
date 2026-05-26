package com.hrbank3.hrbank3.dto.dashboard;

public record EmployeeDistributionDto(
    String groupKey,
    long count,
    double percentage
) {

}