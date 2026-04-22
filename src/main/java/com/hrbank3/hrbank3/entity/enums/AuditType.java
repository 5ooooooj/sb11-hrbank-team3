package com.hrbank3.hrbank3.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "이력 유형 Enum")
public enum AuditType {
  CREATED("직원 추가"),
  UPDATED("직원 정보 수정"),
  DELETED("직원 삭제");

  private final String description;
}