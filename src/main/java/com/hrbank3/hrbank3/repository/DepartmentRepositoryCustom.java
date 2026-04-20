package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.department.DepartmentDto;

public interface DepartmentRepositoryCustom {

    CursorPageResponseDto<DepartmentDto> findAllWithCursor(
            String nameOrDescription,
            String sortField,
            String sortDirection,
            Long idAfter,
            int size
    );
}