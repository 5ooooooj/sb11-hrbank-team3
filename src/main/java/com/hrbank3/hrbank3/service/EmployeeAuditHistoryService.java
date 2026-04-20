package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.common.util.IpExtractUtil;
import com.hrbank3.hrbank3.entity.EmployeeAuditHistory;
import com.hrbank3.hrbank3.entity.enums.AuditType;
import com.hrbank3.hrbank3.event.EmployeeAuditEvent;
import com.hrbank3.hrbank3.repository.EmployeeAuditHistoryRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeAuditHistoryService {

  private final EmployeeAuditHistoryRepository auditRepository;

  // 직원 정보 수정 시 발생하는 핸들링
  @Transactional
  @EventListener
  public void recordAuditHistory(EmployeeAuditEvent event) {
    Map<String, Object> changedContent = extractDiff(event.beforeData(), event.afterData());

    // 바뀐 내용이 없으면 이력 생성 생략
    if (changedContent.isEmpty() && event.auditType() == AuditType.UPDATED) {
      return;
    }

    EmployeeAuditHistory auditHistory = EmployeeAuditHistory.builder()
        .auditType(event.auditType())
        .targetEmployeeNo(event.targetEmployeeNo())
        .changedContent(changedContent)
        .memo(event.memo())
        .ipAddress(IpExtractUtil.getClientIpFromContext())
        .build();

    auditRepository.save(auditHistory);
  }

  private Map<String, Object> extractDiff(Map<String, Object> beforeData,
      Map<String, Object> afterData) {
    Map<String, Object> diffMap = new HashMap<>();
    Set<String> allKeys = new HashSet<>();

    if (beforeData != null) {
      allKeys.addAll(beforeData.keySet());
    }
    if (afterData != null) {
      allKeys.addAll(afterData.keySet());
    }

    for (String key : allKeys) {
      Object before = beforeData != null ? beforeData.get(key) : "-";
      Object after = afterData != null ? afterData.get(key) : "-";

      if (!Objects.equals(before, after)) {
        diffMap.put(key, Map.of("before", before, "after", after));
      }
    }
    return diffMap;
  }
}