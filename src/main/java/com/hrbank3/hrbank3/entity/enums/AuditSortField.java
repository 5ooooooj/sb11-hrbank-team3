package com.hrbank3.hrbank3.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditSortField {
  AT("at"),
  IP_ADDRESS("ipAddress");

  private final String value;

  public static AuditSortField from(String source) {
    if (source == null || source.isBlank() || "ALL".equalsIgnoreCase(source)) {
      return AT;
    }

    for (AuditSortField field : AuditSortField.values()) {
      if (field.value.equalsIgnoreCase(source) || field.name().equalsIgnoreCase(source)) {
        return field;
      }
    }
    return AT; // 지원하지 않는 필드 시 기본값 반환
  }
}