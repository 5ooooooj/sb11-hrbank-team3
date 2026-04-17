package com.hrbank.hrbank.repository;

import com.hrbank.hrbank.dto.CursorPageResponseDto;
import com.hrbank.hrbank.dto.DepartmentDto;

public interface DepartmentRepositoryCustom {

    CursorPageResponseDto<DepartmentDto> findAllWithCursor(
            String nameOrDescription,
            String sortBy,
            String sortDirection,
            Long lastId,
            int size
    );
}