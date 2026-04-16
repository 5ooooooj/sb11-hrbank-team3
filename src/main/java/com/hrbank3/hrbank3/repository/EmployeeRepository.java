package com.hrbank3.hrbank3.repository;

import com.hrbank3.hrbank3.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

  // 기본적인 CRUD는 JpaRepository가 기본 제공함
  boolean existsByEmail(String email);
}
