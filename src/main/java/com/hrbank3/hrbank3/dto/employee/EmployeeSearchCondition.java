package com.hrbank3.hrbank3.dto.employee;

import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import java.time.LocalDate;

public record EmployeeSearchCondition(
    String nameOrEmail,
    String employeeNumber,
    String departmentName,
    String position,
    LocalDate hireDateFrom,
    LocalDate hireDateTo,
    EmployeeStatus status,
    Long idAfter,
    String cursor,
    int size,
    String sortField,
    String sortDirection
) {

}
