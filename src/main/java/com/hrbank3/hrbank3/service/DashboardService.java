package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.dashboard.DashboardSummaryDto;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.entity.EmployeeStatus;
import com.hrbank3.hrbank3.repository.BackupHistoryRepository;
import com.hrbank3.hrbank3.repository.EmployeeAuditHistoryRepository;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeAuditHistoryRepository employeeAuditHistoryRepository;
    private final BackupHistoryRepository backupHistoryRepository;

    // 카드형 요약 정보 4개를 한번에 반환
    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary() {
        return new DashboardSummaryDto(
                getTotalEmployeeCount(),
                getRecentAuditCount(),
                getThisMonthHireCount(),
                getLastBackupAt()
        );
    }

    // 총 직원 수
    private long getTotalEmployeeCount() {
        return employeeRepository.countByStatusNot(EmployeeStatus.RESIGNED);
    }

    // 최근 일주일 수정 이력 건수
    private long getRecentAuditCount() {
        Instant oneWeekAgo = Instant.now().minusSeconds(60 * 60 * 24 * 7);
        return employeeAuditHistoryRepository.countByCreatedAtAfter(oneWeekAgo);
    }

    // 이번 달 신규 입사자 수
    private long getThisMonthHireCount() {
        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        return employeeRepository.countByHireDateGreaterThanEqualAndStatus(
                firstDayOfMonth, EmployeeStatus.ACTIVE);
    }

    // 마지막 백업 시간
    private Instant getLastBackupAt() {
        return backupHistoryRepository
                .findTopByStatusOrderByStartedAtDesc(BackupStatus.COMPLETED)
                .map(backup -> backup.getEndedAt())
                .orElse(null);
    }
}