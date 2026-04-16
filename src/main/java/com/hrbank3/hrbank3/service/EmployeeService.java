package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.employee.EmployeeCreateRequest;
import com.hrbank3.hrbank3.dto.employee.EmployeeDto;
import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;

  @Transactional
  public EmployeeDto create(EmployeeCreateRequest request) {

    if (employeeRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
    }

    Employee employee = Employee.create(
        request.name(),
        request.email(),
        request.departmentId(),
        request.position(),
        request.hireDate(),
        null // 프로필 이미지는 추후 파일 관리와 연동
    );

    Employee savedEmployee = employeeRepository.save(employee);

    return toDto(savedEmployee);
  }

  @Transactional(readOnly = true)
  public EmployeeDto findById(Long id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수  없습니다."));
    return toDto(employee);
  }

  private EmployeeDto toDto(Employee employee) {
    return new EmployeeDto(
        employee.getId(),
        employee.getName(),
        employee.getEmail(),
        employee.getEmployeeNumber(),
        employee.getDepartmentId(),
        null, // 부서이름은 부서 엔티티 연동 후 추가
        employee.getPosition(),
        employee.getHireDate(),
        employee.getStatus(),
        employee.getProfileImageId()
    );
  }


}
