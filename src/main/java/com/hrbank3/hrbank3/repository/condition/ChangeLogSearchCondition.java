package com.hrbank3.hrbank3.repository.condition;

import com.hrbank3.hrbank3.entity.enums.AuditType;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.Instant;

public record ChangeLogSearchCondition(

    // 검색필터
    @Parameter(description = "대상 직원 사번")
    String employeeNumber,

    @Parameter(description = "이력 유형 (CREATED, UPDATED, DELETED)")
    AuditType type,

    @Parameter(description = "내용")
    String memo,

    @Parameter(description = "IP 주소")
    String ipAddress,

    @Parameter(description = "수정 일시(부터)")
    Instant atFrom,

    @Parameter(description = "수정 일시(까지)")
    Instant atTo,

    // 페이징 및 정렬
    @Parameter(description = "이전 페이지 마지막 요소 ID")
    Long idAfter,

    @Parameter(description = "커서 (이전 페이지의 마지막 ID)")
    String cursor,

    @Parameter(description = "페이지 크기")
    Integer size,

    @Parameter(description = "정렬 필드 (ipAddress, at")
    String sortField,

    @Parameter(description = "정렬 방향 (asc, desc)")
    String sortDirection

) {

  public ChangeLogSearchCondition {
    if (size == null) {
      size = 1;
    }
    if (sortField == null) {
      sortField = "at";
    }
    if (sortDirection == null) {
      sortDirection = "desc";
    }
  }

}

