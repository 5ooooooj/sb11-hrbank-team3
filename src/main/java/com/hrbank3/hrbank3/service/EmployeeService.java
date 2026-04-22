package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.employee.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeCreateRequest;
import com.hrbank3.hrbank3.dto.employee.EmployeeDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeUpdateRequest;
import com.hrbank3.hrbank3.entity.Department;
import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.entity.FileMetadata;
import com.hrbank3.hrbank3.entity.enums.AuditType;
import com.hrbank3.hrbank3.event.EmployeeAuditEvent;
import com.hrbank3.hrbank3.event.EmployeeNotificationEvent;
import com.hrbank3.hrbank3.repository.DepartmentRepository;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import com.hrbank3.hrbank3.repository.condition.EmployeeSearchCondition;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final FileService fileService;

  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public EmployeeDto create(EmployeeCreateRequest request, MultipartFile profileImage) {

    if (employeeRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
    }

    Department department = departmentRepository.findById(request.departmentId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 부서입니다."));

    // 파일 존재 여부에 따른 조건부 처리(기본값은 null)
    FileMetadata savedProfileImage = null;

    // 사진을 업로드한 경우에만 FileService를 호출하여 DB에 메타데이터를 생성
    if (profileImage != null && !profileImage.isEmpty()) {
      savedProfileImage = fileService.uploadFile(profileImage);
    }

    try {
      Employee employee = Employee.create(
          request.name(),
          request.email(),
          department,
          request.position(),
          request.hireDate(),
          savedProfileImage
      );

      Employee savedEmployee = employeeRepository.save(employee);

      // 알림 이벤트 발행
      eventPublisher.publishEvent(new EmployeeNotificationEvent("EMPLOYEE_CREATE", savedEmployee));

      // 수정 이력 저장 이벤트 발행
      eventPublisher.publishEvent(new EmployeeAuditEvent(
          AuditType.CREATED,
          savedEmployee.getEmployeeNumber(),
          new HashMap<>(), // Before: 없음
          extractEmployeeData(savedEmployee), // After: 변경 내역
          "신규 직원 등록"
      ));

      return toDto(savedEmployee);
    } catch (Exception e) {
      if (savedProfileImage != null) {
        fileService.deletePhysicalFile(savedProfileImage.getStoragePath());
      }
      throw new RuntimeException("직원 등록 중 오류가 발생하여 업로드된 파일을 삭제하고 작업을 취소합니다", e);
    }

  }

  @Transactional(readOnly = true)
  public CursorPageResponseDto<EmployeeDto> findAll(EmployeeSearchCondition condition) {
    List<Employee> employees = employeeRepository.findAllByCondition(condition);

    boolean hasNext = employees.size() > condition.size();
    if (hasNext) {
      employees = employees.subList(0, condition.size());
    }

    long totalElements = employeeRepository.countByCondition(condition);

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

  @Transactional
  public EmployeeDto update(Long id, EmployeeUpdateRequest request, MultipartFile newProfileImage) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수 없습니다."));

    if (!employee.getEmail().equals(request.email()) &&
        employeeRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
    }

    Department department = departmentRepository.findById(request.departmentId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 부서입니다."));

    // 수정 이력 저장을 위한 수정 전 스냅샷
    Map<String, Object> beforeData = extractEmployeeData(employee);

    employee.update(
        request.name(),
        request.email(),
        department,
        request.position(),
        request.hireDate(),
        request.status()
    );

    // 프로필 이미지가 새로 업로드된 경우
    if (newProfileImage != null && !newProfileImage.isEmpty()) {
      // 기존 이미지가 존재 할 경우, 디스크에서 실제 물리파일 먼저 삭제
      FileMetadata oldImage = employee.getProfileImage();
      if (oldImage != null) {
        fileService.deletePhysicalFile(oldImage.getStoragePath());
      }
      // 새로운 파일 로컬 저장 및 DB 메타데이터 생성
      FileMetadata savedNewImage = fileService.uploadFile(newProfileImage);

      employee.updateProfileImage(savedNewImage);
    }

    // 수정 이력 저장 이벤트 발행
    eventPublisher.publishEvent(new EmployeeAuditEvent(
        AuditType.UPDATED,
        employee.getEmployeeNumber(),
        beforeData,                     // Before: 미리 찍어둔 스냅샷
        extractEmployeeData(employee),  // After: 수정이 완료된 상태
        "직원 정보 수정"
    ));

    return toDto(employee);
  }

  @Transactional
  public void delete(Long id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수 없습니다."));

    // 수정 이력 저장을 위한 삭제 전 스냅샷
    Map<String, Object> beforeData = extractEmployeeData(employee);

    // 알림 이벤트 발행
    eventPublisher.publishEvent(new EmployeeNotificationEvent("EMPLOYEE_DELETE", employee));

    if (employee.getProfileImage() != null) {
      fileService.deletePhysicalFile(employee.getProfileImage().getStoragePath());
    }

    employeeRepository.delete(employee);

    // 수정 이력 저장 이벤트 발행
    eventPublisher.publishEvent(new EmployeeAuditEvent(
        AuditType.DELETED,
        employee.getEmployeeNumber(),
        beforeData,       // Before: 삭제 전 데이터
        new HashMap<>(),  // After: 없음
        "직원 영구 삭제"
    ));
  }

  // 엔티티의 현재 상태 스냅샷 저장
  private Map<String, Object> extractEmployeeData(Employee employee) {
    if (employee == null) {
      return new HashMap<>();
    }
    Map<String, Object> data = new HashMap<>();

    data.put("hireDate", employee.getHireDate() != null ? employee.getHireDate().toString() : null);
    data.put("name", employee.getName());
    data.put("position", employee.getPosition());
    data.put("department",
        employee.getDepartment() != null ? employee.getDepartment().getName() : null);
    data.put("email", employee.getEmail());
    data.put("employeeNumber", employee.getEmployeeNumber());
    data.put("status", employee.getStatus() != null ? employee.getStatus().name() : null);
    data.put("profileImageId",
        employee.getProfileImage() != null ? String.valueOf(employee.getProfileImage().getId())
            : null);
    return data;

  }

  private EmployeeDto toDto(Employee employee) {
    String profileImageUrl = null;
    if(employee.getProfileImage() != null) {
      profileImageUrl = "/uploads/" + employee.getProfileImage().getStoragePath();
    }

    return new EmployeeDto(
        employee.getId(),
        employee.getName(),
        employee.getEmail(),
        employee.getEmployeeNumber(),
        employee.getDepartment().getId(), // departmentId
        employee.getDepartment().getName(), // 부서이름은 부서 엔티티 연동 후 추가
        employee.getPosition(),
        employee.getHireDate(),
        employee.getStatus(),
        employee.getProfileImage() != null ? employee.getProfileImage().getId() : null,
        profileImageUrl
    );
  }

}
