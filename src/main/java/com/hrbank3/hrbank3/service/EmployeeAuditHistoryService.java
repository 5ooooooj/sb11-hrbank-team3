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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeAuditHistoryService {

  private final EmployeeAuditHistoryRepository auditRepository;
  private final EmployeeAuditHistoryRepositoryCustom customAuditRepository;
  private final EmployeeRepository employeeRepository;


  /*
   * 직원 정보 수정 시 발생하는 이벤트 핸들링
   * 메인 비즈니스 로직의 응답 속도 저하를 막고,
   * 이력 저장이 실패하더라도 메인 트랜잭션이 롤백되지 않도록
   * 의도적으로 비동기 처리(@Async) 및 트랜잭션을 분리(AFTER_COMMIT)하였습니다.
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
      Object before = (beforeData != null) ? beforeData.get(key) : null;
      Object after = (afterData != null) ? afterData.get(key) : null;

      if (!Objects.equals(before, after)) {
        Map<String, Object> diffEntry = new HashMap<>();
        diffEntry.put("before", before);
        diffEntry.put("after", after);

        diffMap.put(key, diffEntry);
      }
    }
    return diffMap;
  }

  @Transactional(readOnly = true)
  public CursorPageResponseDto<ChangeLogDto> findAll(ChangeLogSearchCondition condition) {
    validatePaginationParams(condition.cursor(), condition.idAfter());

    return customAuditRepository.findAllWithCursor(condition);
  }

  // 상세 데이터 읽기
  @Transactional(readOnly = true)
  public ChangeLogDetailDto find(Long id) {
    EmployeeAuditHistory audit = auditRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("이력을 찾을 수 없습니다."));

    List<DiffDto> diffs = audit.getChangedContent().entrySet().stream()
        .map(entry -> {
          if (!(entry.getValue() instanceof Map<?, ?> values)) {
            // 만약 Map 형태가 아니라면 안전하게 기본값("-") 반환
            return new DiffDto(entry.getKey(), "-", "-");
          }

          Object beforeRaw = values.get("before");
          Object afterRaw = values.get("after");

          String beforeStr = beforeRaw == null ? null : String.valueOf(beforeRaw);
          String afterStr = afterRaw == null ? null : String.valueOf(afterRaw);

          return new DiffDto(entry.getKey(), beforeStr, afterStr);
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
          if (audit.getAuditType() == AuditType.DELETED) {
            String recoveredName = diffs.stream()
                .filter(d -> "name".equals(d.propertyName()))
                .map(DiffDto::before)
                .filter(val -> val != null && !"-".equals(val))
                .findFirst()
                .orElseGet(() -> {
                  log.warn("직원 사번 스냅샷 복원 실패: {}", audit.getId());
                  return null;
                });

            Long recoveredProfileId = diffs.stream()
                .filter(d -> "profileImageId".equals(d.propertyName()))
                .map(DiffDto::before)
                .filter(val -> val != null && !"-".equals(val))
                .findFirst()
                .map(val -> {
                  try {
                    return Long.parseLong(val);
                  } catch (NumberFormatException ex) {
                    return null;
                  }
                })
                .orElseGet(() -> {
                  log.info("직원 프로필 아이디 스냅샷 복원 실패: {}", audit.getId());
                  return null;
                });
            return new EmployeeInfo(recoveredName, recoveredProfileId);
          }
          return new EmployeeInfo(null, null);
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

  @Transactional(readOnly = true)
  public long getCount(Instant fromDate, Instant toDate) {
    if (fromDate == null) {
      fromDate = Instant.now().minus(7, ChronoUnit.DAYS);
    }
    if (toDate == null) {
      toDate = Instant.now();
    }
    return auditRepository.countByCreatedAtBetween(fromDate, toDate);
  }
}