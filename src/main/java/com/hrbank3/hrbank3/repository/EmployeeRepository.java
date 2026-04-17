package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.Employee;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  // 기본적인 CRUD는 JpaRepository가 기본 제공함
  boolean existsByEmail(String email);

  // 특정 시간 이후 변경된 직원 존재 여부 - BackupHistoryService에서 사용함
  boolean existsByUpdatedAtAfter(Instant lastBackupTime);
}
