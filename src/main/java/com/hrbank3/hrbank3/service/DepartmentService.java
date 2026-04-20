package com.hrbank3.hrbank3.service;

import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.department.DepartmentDto;
import com.hrbank3.hrbank3.dto.department.DepartmentCreateRequest;
import com.hrbank3.hrbank3.dto.department.DepartmentUpdateRequest;
import com.hrbank3.hrbank3.entity.Department;
import com.hrbank3.hrbank3.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hrbank3.hrbank3.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    // 부서 등록
    public DepartmentDto create(DepartmentCreateRequest request) {
        // 이름 중복 체크
        if (departmentRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 부서 이름입니다: " + request.name());
        }

        Department department = new Department(
                request.name(),
                request.description(),
                request.establishedDate()
        );

        Department saved = departmentRepository.save(department);
        return toDto(saved);
    }

    // 부서 수정
    public DepartmentDto update(Long id, DepartmentUpdateRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다: " + id));

        // 자기 자신 제외하고 이름 중복 체크
        if (departmentRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new IllegalArgumentException("이미 존재하는 부서 이름입니다: " + request.name());
        }

        department.update(request.name(), request.description(), request.establishedDate());

        return toDto(department);
    }

    // 부서 삭제
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다: " + id));

        // 소속 직원 유무 체크 추가
        boolean hasEmployees = employeeRepository.existsByDepartmentId(id);
        if (hasEmployees) {
            throw new IllegalStateException("소속 직원이 있는 부서는 삭제할 수 없습니다.");
        }

        departmentRepository.delete(department);
    }

    // 부서 목록 조회 (커서 페이지네이션)
    @Transactional(readOnly = true)
    public CursorPageResponseDto<DepartmentDto> findAll(
            String nameOrDescription,
            String sortBy,
            String sortDirection,
            Long lastId,
            int size) {
        return departmentRepository.findAllWithCursor(
                nameOrDescription, sortBy, sortDirection, lastId, size);
    }
    // 부서 단건 조회
    @Transactional(readOnly = true)
    public DepartmentDto findById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다: " + id));
        return toDto(department);
    }

    // Entity -> DTO 변환
    private DepartmentDto toDto(Department department) {
        return new DepartmentDto(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getEstablishedDate(),
                department.getCreatedAt(),
                department.getUpdatedAt(),
                0L  // 직원 연동 전까지 임시로 0
        );
    }
}