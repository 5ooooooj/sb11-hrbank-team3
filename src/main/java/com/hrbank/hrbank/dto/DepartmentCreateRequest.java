package com.hrbank.hrbank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DepartmentCreateRequest(

        @NotBlank(message = "부서 이름은 필수입니다")
        String name,

        String description,

        @NotNull(message = "설립일은 필수입니다")
        LocalDate establishedDate
) {}