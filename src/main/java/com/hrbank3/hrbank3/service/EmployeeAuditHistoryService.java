package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.common.util.IpExtractUtil;
import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDetailDto;
import com.hrbank3.hrbank3.dto.audit_history.ChangeLogDto;
import com.hrbank3.hrbank3.dto.audit_history.DiffDto;
import com.hrbank3.hrbank3.entity.EmployeeAuditHistory;
import com.hrbank3.hrbank3.entity.enums.AuditType;
import com.hrbank3.hrbank3.event.EmployeeAuditEvent;
import com.hrbank3.hrbank3.repository.EmployeeAuditHistoryRepository;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import com.hrbank3.hrbank3.repository.condition.ChangeLogSearchCondition;
import com.hrbank3.hrbank3.repository.custom.EmployeeAuditHistoryRepositoryCustom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmployeeAuditHistoryService {

  private final EmployeeAuditHistoryRepository auditRepository;
  private final EmployeeAuditHistoryRepositoryCustom customAuditRepository;
  private final EmployeeRepository employeeRepository;

  // 직원 정보 수정 시 발생하는 핸들링
  @Transactional
  @TransactionalEventListener
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

  @Transactional(readOnly = true)
  public CursorPageResponseDto<ChangeLogDto> findAll(ChangeLogSearchCondition condition) {
    validatePaginationParams(condition.getCursor(), condition.getIdAfter());

    return customAuditRepository.findAllWithCursor(condition);
  }

  // 상세 데이터 읽기
  @Transactional(readOnly = true)
  public ChangeLogDetailDto find(Long id) {
    EmployeeAuditHistory audit = auditRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("이력을 찾을 수 없습니다."));

    List<DiffDto> diffs = audit.getChangedContent().entrySet().stream()
        .map(entry -> {
          if (!(entry.getValue() instanceof Map<?, ?> values)) {
            // 만약 Map 형태가 아니라면 안전하게 기본값("-") 반환
            return new DiffDto(entry.getKey(), "-", "-");
          }

          return new DiffDto(
              entry.getKey(),
              String.valueOf(values.get("before")),
              String.valueOf(values.get("after"))
          );
        })
        .toList();

    EmployeeInfo info = resolveEmployeeInfo(audit, diffs);

    return new ChangeLogDetailDto(
        audit.getId(),
        audit.getAuditType().name(),
        audit.getTargetEmployeeNo(),
        audit.getMemo(),
        audit.getIpAddress(),
        audit.getCreatedAt(),
        info.name(),
        info.profileImageId(),
        diffs
    );
  }

  // 사원 이름 및 프로필 이미지 추출
  private EmployeeInfo resolveEmployeeInfo(EmployeeAuditHistory audit, List<DiffDto> diffs) {
    return employeeRepository.findByEmployeeNumber(audit.getTargetEmployeeNo())
        .map(e -> new EmployeeInfo(
            e.getName(),
            e.getProfileImage() != null ? e.getProfileImage().getId() : null
        ))
        .orElseGet(() -> {
          String deletedName = diffs.stream()
              .filter(d -> "name".equals(d.propertyName()))
              .map(DiffDto::before)
              .findFirst()
              .orElse("알 수 없음");

          Long deletedProfileId = diffs.stream()
              .filter(d -> "profileImageId".equals(d.propertyName()))
              .map(DiffDto::before)
              .filter(val -> val != null && !"-".equals(val) && !"null".equals(val)) // 파싱 에러 방지
              .map(val -> {
                try {
                  return Long.parseLong(val);
                } catch (NumberFormatException ex) {
                  return null;
                }
              })
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
          return new EmployeeInfo(deletedName, deletedProfileId);
        });
  }

  private void validatePaginationParams(String cursor, Long idAfter) {
    if ((StringUtils.hasText(cursor) && idAfter == null) ||
        (!StringUtils.hasText(cursor) && idAfter != null)) {
      throw new IllegalArgumentException("cursor와 idAfter는 반드시 함께 사용되어야 합니다.");
    }
  }

  // 데이터 전달용 임시 레코드
  private record EmployeeInfo(String name, Long profileImageId) {

  }
}