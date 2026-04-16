package com.hrbank3.hrbank3.controller;

import com.hrbank3.hrbank3.dto.employee.EmployeeCreateRequest;
import com.hrbank3.hrbank3.dto.employee.EmployeeDto;
import com.hrbank3.hrbank3.dto.employee.EmployeeUpdateRequest;
import com.hrbank3.hrbank3.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeService employeeService;

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

  @GetMapping
  public ResponseEntity<EmployeeDto> findById(@PathVariable Long id) {
    EmployeeDto response = employeeService.findById(id);
    return ResponseEntity.ok(response);
  }

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
}
