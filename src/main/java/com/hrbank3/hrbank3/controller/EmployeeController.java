package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.dto.employee.EmployeeCreateRequest;
import com.hrbank3.hrbank3.dto.employee.EmployeeDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeUpdateRequest;
import com.hrbank3.hrbank3.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "직원 관리", description = "직원 관리 API ")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeService employeeService;

  @Operation(summary = "직원 등록")
  @PostMapping(consumes = "multipart/form-data")
  //employee 객체와 profile 파일 같이 보내야하는데, 파일이 포함된 요청은 multipart/form-data 써야함
  public ResponseEntity<EmployeeDto> create(
      @RequestPart("employee")
      @Valid EmployeeCreateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    EmployeeDto response = employeeService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(summary = "직원 상세 조회")
  @GetMapping
  public ResponseEntity<EmployeeDto> findById(@PathVariable Long id) {
    EmployeeDto response = employeeService.findById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "직원 수정")
  @PatchMapping(value = "/{id}", consumes = "multipart/form-data")
  public ResponseEntity<EmployeeDto> update(
      @PathVariable Long id,
      @RequestPart("employee")
      @Valid EmployeeUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  ) {
    EmployeeDto response = employeeService.update(id, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "직원 삭제")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    employeeService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
