package com.hrbank3.hrbank3.common.converter;

import com.hrbank3.hrbank3.entity.enums.AuditType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToAuditTypeConverter implements Converter<String, AuditType> {

  @Override
  public AuditType convert(String source) {
    // 비어있거나 프론트에서 ALL을 보낸 경우
    if (source.trim().isEmpty() || "ALL".equalsIgnoreCase(source)) {
      return null;
    }

    for (AuditType type : AuditType.values()) {
      if (type.name().equalsIgnoreCase(source)) {
        return type;
      }
    }

    throw new IllegalArgumentException("지원하지 않는 이력 유형입니다: " + source);
  }
}