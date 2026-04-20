package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDto;
import com.hrbank3.hrbank3.repository.condition.ChangeLogSearchCondition;

public interface EmployeeAuditHistoryRepositoryCustom {

  // 다건 목록 조회 (커서 페이징 + 동적 필터링)
  CursorPageResponseDto<ChangeLogDto> findAllWithCursor(ChangeLogSearchCondition condition);
}