package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.dashboard.DashboardSummaryDto;
import com.hrbank3.hrbank3.dto.dashboard.DepartmentDistributionDto;
import com.hrbank3.hrbank3.dto.dashboard.EmployeeTrendDto;
import com.hrbank3.hrbank3.dto.dashboard.PositionDistributionDto;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.entity.EmployeeStatus;
import com.hrbank3.hrbank3.repository.BackupHistoryRepository;
import com.hrbank3.hrbank3.repository.DepartmentRepository;
import com.hrbank3.hrbank3.repository.EmployeeAuditHistoryRepository;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private final DepartmentRepository departmentRepository;

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

    // 최근 1년 월별 직원수 변동 추이
    @Transactional(readOnly = true)
    public List<EmployeeTrendDto> getEmployeeTrend() {
        List<EmployeeTrendDto> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            // 해당 월의 마지막 날 기준으로 직원 수 계산
            LocalDate targetMonth = now.minusMonths(i);
            LocalDate lastDayOfMonth = targetMonth.withDayOfMonth(targetMonth.lengthOfMonth());

            // 해당 월 말일 기준으로 입사한 재직/휴직 직원 수
            long count = employeeRepository.countByHireDateLessThanEqualAndStatusNot(
                    lastDayOfMonth, EmployeeStatus.RESIGNED);

            trend.add(new EmployeeTrendDto(targetMonth.format(formatter), count));
        }
        return trend;
    }

    // 부서별 직원 분포
    @Transactional(readOnly = true)
    public List<DepartmentDistributionDto> getDepartmentDistribution() {
        return departmentRepository.findAllWithEmployeeCount();
    }

    // 직무별 직원 분포
    @Transactional(readOnly = true)
    public List<PositionDistributionDto> getPositionDistribution() {
        return employeeRepository.findPositionDistribution();
    }

    // 총 직원 수 (퇴사자 제외)
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