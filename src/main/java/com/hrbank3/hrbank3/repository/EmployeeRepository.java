package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.repository.custom.EmployeeRepositoryCustom;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long>,
    EmployeeRepositoryCustom {

  // 기본적인 CRUD는 JpaRepository가 기본 제공함
  boolean existsByEmail(String email);

  boolean existsByUpdatedAtAfter(Instant lastBackupTime);
}
