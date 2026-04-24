package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDto;
import com.hrbank3.hrbank3.repository.condition.ChangeLogSearchCondition;
import java.util.List;

public interface EmployeeAuditHistoryRepositoryCustom {

//  // 다건 목록 조회 (커서 페이징 + 동적 필터링)
//  CursorPageResponseDto<ChangeLogDto> findAllWithCursor(ChangeLogSearchCondition condition);

  // 1. 순수 데이터 목록 조회
  List<ChangeLogDto> findChangeLogs(ChangeLogSearchCondition condition);

  // 2. 전체 개수 카운트 조회
  long countChangeLogs(ChangeLogSearchCondition condition);
}