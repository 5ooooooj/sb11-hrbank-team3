package com.hrbank.hrbank.service;

import com.hrbank.hrbank.dto.CursorPageResponseDto;
import com.hrbank.hrbank.dto.DepartmentDto;
import com.hrbank.hrbank.dto.DepartmentRequest;
import com.hrbank.hrbank.entity.Department;
import com.hrbank.hrbank.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // 부서 등록
    public DepartmentDto create(DepartmentRequest request) {
        // 이름 중복 체크
        if (departmentRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 부서 이름입니다: " + request.name());
        }

        Department department = new Department();
        department.setName(request.name());
        department.setDescription(request.description());
        department.setEstablishedDate(request.establishedDate());

        Department saved = departmentRepository.save(department);
        return toDto(saved);
    }

    // 부서 수정
    public DepartmentDto update(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다: " + id));

        // 자기 자신 제외하고 이름 중복 체크
        if (departmentRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new IllegalArgumentException("이미 존재하는 부서 이름입니다: " + request.name());
        }

        department.setName(request.name());
        department.setDescription(request.description());
        department.setEstablishedDate(request.establishedDate());

        return toDto(department);
    }

    // 부서 삭제
    public void delete(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 부서입니다: " + id));

        departmentRepository.delete(department);
    }

    // 부서 목록 조회 (커서 페이지네이션)
    @Transactional(readOnly = true)
    public CursorPageResponseDto<DepartmentDto> findAll(
            String nameKeyword,
            String descriptionKeyword,
            String sortBy,
            String sortDirection,
            Long lastId,
            int size) {

        // 전체 목록 조회
        List<Department> all = departmentRepository.findAll();

        // 이름 키워드 필터링
        if (nameKeyword != null && !nameKeyword.isBlank()) {
            all = all.stream()
                    .filter(d -> d.getName().contains(nameKeyword))
                    .collect(Collectors.toList());
        }

        // 설명 키워드 필터링
        if (descriptionKeyword != null && !descriptionKeyword.isBlank()) {
            all = all.stream()
                    .filter(d -> d.getDescription() != null &&
                            d.getDescription().contains(descriptionKeyword))
                    .collect(Collectors.toList());
        }

        // 정렬
        if ("establishedDate".equals(sortBy)) {
            all.sort((a, b) -> "desc".equals(sortDirection)
                    ? b.getEstablishedDate().compareTo(a.getEstablishedDate())
                    : a.getEstablishedDate().compareTo(b.getEstablishedDate()));
        } else {
            all.sort((a, b) -> "desc".equals(sortDirection)
                    ? b.getName().compareTo(a.getName())
                    : a.getName().compareTo(b.getName()));
        }

        // 커서 페이지네이션 - lastId 이후 데이터만 가져오기
        if (lastId != null) {
            int idx = -1;
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId().equals(lastId)) {
                    idx = i;
                    break;
                }
            }
            if (idx >= 0) {
                all = all.subList(idx + 1, all.size());
            }
        }

        long totalElements = all.size();

        // size+1개 가져와서 다음 페이지 있는지 확인
        boolean hasNext = all.size() > size;
        if (hasNext) {
            all = all.subList(0, size);
        }

        List<DepartmentDto> content = all.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        Long nextCursor = hasNext ? all.get(all.size() - 1).getId() : null;

        return new CursorPageResponseDto<>(
                content,
                nextCursor,
                nextCursor,
                content.size(),
                totalElements,
                hasNext
        );
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
                department.getUpdatedAt()
        );
    }
}