package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.dashboard.DashboardSummaryDto;
import com.hrbank3.hrbank3.dto.dashboard.DepartmentDistributionDto;
import com.hrbank3.hrbank3.dto.dashboard.EmployeeTrendDto;
import com.hrbank3.hrbank3.dto.dashboard.PositionDistributionDto;
import com.hrbank3.hrbank3.entity.enums.BackupStatus;
import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import com.hrbank3.hrbank3.entity.enums.TrendUnit;
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

  // 직원 수 추이 조회 (from, to, unit 파라미터)
  @Transactional(readOnly = true)
  public List<EmployeeTrendDto> getEmployeeTrend(LocalDate from, LocalDate to, TrendUnit unit) {
    // 기본값 설정
    if (unit == null) {
      unit = TrendUnit.month;
    }
    if (to == null) {
      to = LocalDate.now();
    }
    if (from == null) {
      from = getDefaultFrom(to, unit);
    }

    List<EmployeeTrendDto> trend = new ArrayList<>();
    LocalDate cursor = from;

    while (!cursor.isAfter(to)) {
      LocalDate periodEnd = getPeriodEnd(cursor, unit, to);

      // 해당 기간 말일 기준 재직/휴직 직원 수
      long count = employeeRepository.countByHireDateLessThanEqualAndStatusNot(
          periodEnd, EmployeeStatus.RESIGNED);

      // 이전 기간 직원 수
      LocalDate prevPeriodEnd = getPeriodEnd(getPrevPeriodStart(cursor, unit), unit,
          cursor.minusDays(1));
      long prevCount = employeeRepository.countByHireDateLessThanEqualAndStatusNot(
          prevPeriodEnd, EmployeeStatus.RESIGNED);

      // 증감 수, 증감률 계산
      long change = count - prevCount;
      double changeRate = prevCount == 0 ? 0.0
          : Math.round((double) change / prevCount * 1000) / 10.0;

      trend.add(new EmployeeTrendDto(
          cursor.format(getFormatter(unit)),
          count,
          change,
          changeRate
      ));

      cursor = getNextPeriodStart(cursor, unit);
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

  // unit 기준 기본 시작일 계산
  private LocalDate getDefaultFrom(LocalDate to, TrendUnit unit) {
    return switch (unit) {
      case day -> to.minusDays(11);
      case week -> to.minusWeeks(11);
      case quarter -> to.minusMonths(33);
      case year -> to.minusYears(11);
      default -> to.minusMonths(11);
    };
  }

  // 해당 기간의 마지막 날 계산
  private LocalDate getPeriodEnd(LocalDate start, TrendUnit unit, LocalDate max) {
    LocalDate end = switch (unit) {
      case day -> start;
      case week -> start.plusWeeks(1).minusDays(1);
      case quarter -> start.plusMonths(3).minusDays(1);
      case year -> start.plusYears(1).minusDays(1);
      default -> start.withDayOfMonth(start.lengthOfMonth());
    };
    return end.isAfter(max) ? max : end;
  }

  // 다음 기간 시작일 계산
  private LocalDate getNextPeriodStart(LocalDate current, TrendUnit unit) {
    return switch (unit) {
      case day -> current.plusDays(1);
      case week -> current.plusWeeks(1);
      case quarter -> current.plusMonths(3);
      case year -> current.plusYears(1);
      default -> current.plusMonths(1);
    };
  }

  // 이전 기간 시작일 계산
  private LocalDate getPrevPeriodStart(LocalDate current, TrendUnit unit) {
    return switch (unit) {
      case day -> current.minusDays(1);
      case week -> current.minusWeeks(1);
      case quarter -> current.minusMonths(3);
      case year -> current.minusYears(1);
      default -> current.minusMonths(1);
    };
  }

  // unit별 날짜 포맷
  private DateTimeFormatter getFormatter(TrendUnit unit) {
    return switch (unit) {
      case day, week -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
      case year -> DateTimeFormatter.ofPattern("yyyy");
      default -> DateTimeFormatter.ofPattern("yyyy-MM");
    };
  }
}