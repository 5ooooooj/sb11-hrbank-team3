package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.entity.EmployeeStatus;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  // 기본적인 CRUD는 JpaRepository가 기본 제공함
  boolean existsByEmail(String email);

  // 특정 시간 이후 변경된 직원 존재 여부 - BackupHistoryService에서 사용함
  boolean existsByUpdatedAtAfter(Instant lastBackupTime);

  // 퇴사자 제외한 총 직원 수 - 대시보드에서 사용
  long countByStatusNot(EmployeeStatus status);

  // 이번달 입사자 수 - 대시보드에서 사용
  long countByHireDateGreaterThanEqualAndStatus(LocalDate hireDate, EmployeeStatus status);
}