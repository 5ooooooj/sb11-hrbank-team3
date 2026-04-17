package com.hrbank.hrbank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DepartmentRequest(

        @NotBlank(message = "부서 이름은 필수입니다") // 빈 문자열, null 불가
        String name,

        String description, // 선택값이라 검증 없음

        @NotNull(message = "설립일은 필수입니다") // null 불가
        LocalDate establishedDate
) {}