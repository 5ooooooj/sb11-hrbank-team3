package com.hrbank3.hrbank3.event;

import com.hrbank3.hrbank3.entity.Employee;

public record EmployeeNotificationEvent(
    String eventType,
    Employee employee
) {

}
