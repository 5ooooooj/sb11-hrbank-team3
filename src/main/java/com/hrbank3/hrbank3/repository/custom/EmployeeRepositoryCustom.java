package com.hrbank3.hrbank3.repository.custom;

import com.hrbank3.hrbank3.dto.employee.EmployeeSearchCondition;
import com.hrbank3.hrbank3.entity.Employee;
import java.util.List;

public interface EmployeeRepositoryCustom {

  List<Employee> findAllByCondition(EmployeeSearchCondition condition);
  // 조건에 맞는 직원 목록 조회시 사용

  long countByCondition(EmployeeSearchCondition condition);
  // 전체 직원 수 조회에 사용
}
