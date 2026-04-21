package com.hrbank3.hrbank3.repository.condition;

import com.hrbank3.hrbank3.entity.enums.AuditType;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangeLogSearchCondition {

  // 검색 필터
  @Parameter(description = "대상 직원 사번")
  private String employeeNumber;

  @Parameter(description = "이력 유형 (CREATED, UPDATED, DELETED)")
  private AuditType type;

  @Parameter(description = "내용")
  private String memo;

  @Parameter(description = "IP 주소")
  private String ipAddress;

  @Parameter(description = "수정 일시(부터)")
  private ZonedDateTime atFrom;

  @Parameter(description = "수정 일시(까지)")
  private ZonedDateTime atTo;


  // 페이징 및 정렬
  @Parameter(description = "이전 페이지 마지막 요소 ID")
  private Long idAfter;

  @Parameter(description = "커서 (이전 페이지의 마지막 ID)")
  private String cursor;

  @Parameter(description = "페이지 크기")
  private int size = 10;

  @Parameter(description = "정렬 필드 (ipAddress, at")
  private String sortField = "at";

  @Parameter(description = "정렬 방향 (asc, desc)")
  private String sortDirection = "desc";
}