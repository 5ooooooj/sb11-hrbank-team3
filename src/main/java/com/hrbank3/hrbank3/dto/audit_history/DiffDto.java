package com.hrbank3.hrbank3.dto.audit_history;

public record DiffDto(
    String propertyName,
    String before,
    String after
) {

}