package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeCreateRequest;
import com.hrbank3.hrbank3.dto.employee.EmployeeDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeUpdateRequest;
import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import com.hrbank3.hrbank3.repository.condition.EmployeeSearchCondition;
import java.util.List;
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

    // TODO
    // EmployeeAuditHistory 머지 후 이력 저장 연동 필요
    // AuditType.CREATED, targetEmployeeNo = savedEmployee.getEmployeeNumber()
    Employee savedEmployee = employeeRepository.save(employee);

    return toDto(savedEmployee);
  }

  @Transactional(readOnly = true)
  public CursorPageResponseDto<EmployeeDto> findAll(EmployeeSearchCondition condition) {
    List<Employee> employees = employeeRepository.findAllByCondition(condition);

    // QueryDSL로 목록 조회시 size+1개 가져온 후에
    // 실제로 size+1개가 있다면 다음 페이지가 있다는 뜻이기에 마지막 요소 제거
    boolean hasNext = employees.size() > condition.size();
    if (hasNext) {
      employees = employees.subList(0, condition.size());
    }

    long totalElements = employeeRepository.countByCondition(condition);

    // 다음 페이지가 있다면, 마지막 직원의 Id를 nextIdAfter로 설정 후 Id를 String 변환
    Long nextIdAfter = hasNext ? employees.get(employees.size() - 1).getId() : null;
    String nextCursor = nextIdAfter != null ? String.valueOf(nextIdAfter) : null;

    List<EmployeeDto> content = employees.stream()
        .map(this::toDto)
        .toList();

    return new CursorPageResponseDto<>(
        content,
        nextCursor,
        nextIdAfter,
        content.size(),
        totalElements,
        hasNext
    );
  }

  @Transactional(readOnly = true)
  public EmployeeDto findById(Long id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수  없습니다."));
    return toDto(employee);
  }

  // TODO
  @Transactional
  public EmployeeDto update(Long id, EmployeeUpdateRequest request) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수 없습니다."));

    if (!employee.getName().equals(request.email()) &&
        employeeRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
    }

    employee.update(
        request.name(),
        request.email(),
        request.departmentId(),
        request.position(),
        request.hireDate(),
        request.status(),
        null // 파일업로드 서비스 로직 머지후에 연동
    );

    return toDto(employee);
  }

  // TODO
  @Transactional
  public void delete(Long id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수 없습니다."));
    employeeRepository.delete(employee);
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
