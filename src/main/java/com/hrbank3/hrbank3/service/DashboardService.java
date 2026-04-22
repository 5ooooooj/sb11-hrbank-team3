package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.dashboard.DepartmentDistributionDto;
import com.hrbank3.hrbank3.dto.dashboard.EmployeeDistributionDto;
import com.hrbank3.hrbank3.dto.dashboard.EmployeeTrendDto;
import com.hrbank3.hrbank3.dto.dashboard.PositionDistributionDto;
import com.hrbank3.hrbank3.entity.enums.EmployeeStatus;
import com.hrbank3.hrbank3.entity.enums.TrendUnit;
import com.hrbank3.hrbank3.repository.DepartmentRepository;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
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
  private final DepartmentRepository departmentRepository;

  // 직원 수 추이 조회
  @Transactional(readOnly = true)
  public List<EmployeeTrendDto> getEmployeeTrend(LocalDate from, LocalDate to, TrendUnit unit) {
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
      long count = employeeRepository.countByHireDateLessThanEqualAndStatusNot(periodEnd,
          EmployeeStatus.RESIGNED);

      LocalDate prevPeriodEnd = getPeriodEnd(getPrevPeriodStart(cursor, unit), unit,
          cursor.minusDays(1));
      long prevCount = employeeRepository.countByHireDateLessThanEqualAndStatusNot(prevPeriodEnd,
          EmployeeStatus.RESIGNED);

      long change = count - prevCount;
      double changeRate =
          prevCount == 0 ? 0.0 : Math.round((double) change / prevCount * 1000) / 10.0;

      trend.add(new EmployeeTrendDto(cursor.format(getFormatter(unit)), count, change, changeRate));
      cursor = getNextPeriodStart(cursor, unit);
    }
    return trend;
  }

  // 직원 분포 조회 (groupBy: department 또는 position)
  @Transactional(readOnly = true)
  public List<EmployeeDistributionDto> getDistribution(String groupBy, EmployeeStatus status) {
    if (status == null) {
      status = EmployeeStatus.ACTIVE;
    }

    if ("position".equals(groupBy)) {
      List<PositionDistributionDto> raw = employeeRepository.findPositionDistribution(
          EmployeeStatus.RESIGNED);
      long total = raw.stream().mapToLong(PositionDistributionDto::employeeCount).sum();
      return raw.stream()
          .map(d -> new EmployeeDistributionDto(
              d.positionName(),
              d.employeeCount(),
              total == 0 ? 0.0 : Math.round((double) d.employeeCount() / total * 1000) / 10.0
          ))
          .toList();
    }

    List<DepartmentDistributionDto> raw = departmentRepository.findAllWithEmployeeCount(
        EmployeeStatus.RESIGNED);
    long total = raw.stream().mapToLong(DepartmentDistributionDto::employeeCount).sum();
    return raw.stream()
        .map(d -> new EmployeeDistributionDto(
            d.departmentName(),
            d.employeeCount(),
            total == 0 ? 0.0 : Math.round((double) d.employeeCount() / total * 1000) / 10.0
        ))
        .toList();
  }

  // 직원 수 조회 (status, fromDate, toDate 필터)
  @Transactional(readOnly = true)
  public long getEmployeeCount(EmployeeStatus status, LocalDate fromDate, LocalDate toDate) {
    if (fromDate != null) {
      LocalDate to = toDate != null ? toDate : LocalDate.now();
      return employeeRepository.countByHireDateBetweenAndStatus(fromDate, to, status);
    }
    if (status != null) {
      return employeeRepository.countByStatus(status);
    }
    return employeeRepository.countByStatusNot(EmployeeStatus.RESIGNED);
  }

  private LocalDate getDefaultFrom(LocalDate to, TrendUnit unit) {
    return switch (unit) {
      case day -> to.minusDays(11);
      case week -> to.minusWeeks(11);
      case quarter -> to.minusMonths(33);
      case year -> to.minusYears(11);
      default -> to.minusMonths(11);
    };
  }

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

  private LocalDate getNextPeriodStart(LocalDate current, TrendUnit unit) {
    return switch (unit) {
      case day -> current.plusDays(1);
      case week -> current.plusWeeks(1);
      case quarter -> current.plusMonths(3);
      case year -> current.plusYears(1);
      default -> current.plusMonths(1);
    };
  }

  private LocalDate getPrevPeriodStart(LocalDate current, TrendUnit unit) {
    return switch (unit) {
      case day -> current.minusDays(1);
      case week -> current.minusWeeks(1);
      case quarter -> current.minusMonths(3);
      case year -> current.minusYears(1);
      default -> current.minusMonths(1);
    };
  }

  private DateTimeFormatter getFormatter(TrendUnit unit) {
    return switch (unit) {
      case day, week -> DateTimeFormatter.ofPattern("yyyy-MM-dd");
      case year -> DateTimeFormatter.ofPattern("yyyy");
      default -> DateTimeFormatter.ofPattern("yyyy-MM");
    };
  }
}