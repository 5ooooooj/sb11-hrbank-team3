package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.employee.EmployeeCreateRequest;
import com.hrbank3.hrbank3.dto.employee.EmployeeDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeUpdateRequest;
import com.hrbank3.hrbank3.entity.Employee;
import com.hrbank3.hrbank3.entity.FileMetadata;
import com.hrbank3.hrbank3.repository.EmployeeRepository;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final FileService fileService;

  @Transactional
  public EmployeeDto create(EmployeeCreateRequest request, MultipartFile profileImage) {

    if (employeeRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
    }

    // 파일 존재 여부에 따른 조건부 처리(기본값은 null)
    FileMetadata savedProfileImage = null;

    // 사진을 업로드한 경우에만 FileService를 호출하여 DB에 메타데이터를 생성
    if(profileImage != null && !profileImage.isEmpty()) {
      savedProfileImage = fileService.uploadFile(profileImage);
    }

    try {
      Employee employee = Employee.create(
          request.name(),
          request.email(),
          request.departmentId(),
          request.position(),
          request.hireDate(),
          savedProfileImage
      );

      Employee savedEmployee = employeeRepository.save(employee);

      return toDto(savedEmployee);
    } catch (Exception e) {
      if(savedProfileImage != null) {
        fileService.deletePhysicalFile(savedProfileImage.getStoragePath());
      }
      throw new RuntimeException("직원 등록 중 오류가 발생하여 업로드된 파일을 삭제하고 작업을 취소합니다", e);
    }

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

    employee.update(
        request.name(),
        request.email(),
        request.departmentId(),
        request.position(),
        request.hireDate(),
        request.status()
    );

    // 프로필 이미지가 새로 업로드된 경우
    if(newProfileImage != null && !newProfileImage.isEmpty()) {
      // 기존 이미지가 존재 할 경우, 디스크에서 실제 물리파일 먼저 삭제
      FileMetadata oldImage = employee.getProfileImage();
      if(oldImage != null) {
        fileService.deletePhysicalFile(oldImage.getStoragePath());
      }
      // 새로운 파일 로컬 저장 및 DB 메타데이터 생성
      FileMetadata savedNewImage = fileService.uploadFile(newProfileImage);

      employee.updateProfileImage(savedNewImage);
    }

    return toDto(employee);
  }

  @Transactional
  public void delete(Long id) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("직원을 찾을 수 없습니다."));

    if(employee.getProfileImage() != null) {
      fileService.deletePhysicalFile(employee.getProfileImage().getStoragePath());
    }

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
        employee.getProfileImage() != null ? employee.getProfileImage().getId(): null
    );
  }


}
