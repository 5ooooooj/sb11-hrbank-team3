package com.hrbank3.hrbank3.dto.department;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DepartmentUpdateRequest(

    @NotBlank(message = "부서 이름은 필수입니다")
    String name,

    String description,

    @NotNull(message = "설립일은 필수입니다")
    LocalDate establishedDate,

    @NotBlank(message = "부서 메일은 필수입니다")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    String departmentEmail
) {

}