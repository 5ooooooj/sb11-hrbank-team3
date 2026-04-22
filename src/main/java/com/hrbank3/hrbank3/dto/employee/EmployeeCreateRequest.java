package com.hrbank3.hrbank3.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EmployeeCreateRequest(

    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @NotNull(message = "부서는 필수입니다.")
    Long departmentId,

    @NotBlank(message = "직함은 필수입니다.")
    String position,

    @NotNull(message = "입사일은 필수입니다.")
    LocalDate hireDate,

    String memo
) {

}
