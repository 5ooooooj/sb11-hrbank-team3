package com.hrbank3.hrbank3.dto.employee;

import java.util.List;

public record CursorPageResponseDto<EmployeeDto>(
    List<EmployeeDto> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
