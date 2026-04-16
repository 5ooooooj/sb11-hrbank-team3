package com.hrbank3.hrbank3.dto.employee;

import com.hrbank3.hrbank3.entity.EmployeeStatus;
import java.time.LocalDate;

public record EmployeeDto(
    Long id,
    String name,
    String email,
    String employeeNumber,
    Long departmentId,
    String departmentName,
    String position,
    LocalDate hireDate,
    EmployeeStatus status,
    Long profileImageId
) {

}
