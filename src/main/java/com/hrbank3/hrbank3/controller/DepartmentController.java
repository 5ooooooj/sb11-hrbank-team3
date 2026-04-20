package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.dto.CursorPageResponseDto;
import com.hrbank3.hrbank3.dto.department.DepartmentDto;
import com.hrbank3.hrbank3.dto.department.DepartmentCreateRequest;
import com.hrbank3.hrbank3.dto.department.DepartmentUpdateRequest;
import com.hrbank3.hrbank3.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "부서 관리")  // Swagger 태그명 변경
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "부서 등록")
    @PostMapping
    public ResponseEntity<DepartmentDto> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(departmentService.create(request));
    }

    @Operation(summary = "부서 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<DepartmentDto> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    @Operation(summary = "부서 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "부서 목록 조회")
    @GetMapping
    public ResponseEntity<CursorPageResponseDto<DepartmentDto>> findAll(
            @RequestParam(required = false) String nameOrDescription,
            @RequestParam(defaultValue = "name") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                departmentService.findAll(nameOrDescription, sortField, sortDirection, idAfter, size));
    }

    @Operation(summary = "부서 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.findById(id));
    }
}