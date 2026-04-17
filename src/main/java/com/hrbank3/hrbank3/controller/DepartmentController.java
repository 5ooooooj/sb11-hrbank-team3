package com.hrbank.hrbank.controller;

import com.hrbank.hrbank.dto.CursorPageResponseDto;
import com.hrbank.hrbank.dto.DepartmentDto;
import com.hrbank.hrbank.dto.DepartmentCreateRequest;
import com.hrbank.hrbank.dto.DepartmentUpdateRequest;
import com.hrbank.hrbank.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments") // 모든 부서 API의 기본 경로
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    // 부서 등록
    @PostMapping
    public ResponseEntity<DepartmentDto> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.create(request));
    }

    // 부서 수정
    @PatchMapping("/{id}")
    public ResponseEntity<DepartmentDto> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    // 부서 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 부서 목록 조회
    @GetMapping
    public ResponseEntity<CursorPageResponseDto<DepartmentDto>> findAll(
            @RequestParam(required = false) String nameOrDescription, // 변경
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                departmentService.findAll(
                        nameOrDescription, // 변경
                        sortBy, sortDirection, lastId, size));
    }
    // 부서 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }
}
