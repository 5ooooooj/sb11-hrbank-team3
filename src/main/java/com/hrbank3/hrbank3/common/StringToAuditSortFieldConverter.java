package com.hrbank3.hrbank3.common;

import com.hrbank3.hrbank3.entity.enums.AuditSortField;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToAuditSortFieldConverter implements Converter<String, AuditSortField> {

  @Override
  public AuditSortField convert(String source) {
    return AuditSortField.from(source);
  }
}