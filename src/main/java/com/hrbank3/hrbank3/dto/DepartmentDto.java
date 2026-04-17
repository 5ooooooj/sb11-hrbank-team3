package com.hrbank3.hrbank3.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DepartmentDto(
        Long id,
        String name,
        String description,
        LocalDate establishedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long employeeCount  // 부서 소속 직원 수
) {}