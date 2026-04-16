package com.hrbank.hrbank.domain.department.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DepartmentDto(
        Long departmentId,
        String name,
        String description,
        LocalDate establishedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}