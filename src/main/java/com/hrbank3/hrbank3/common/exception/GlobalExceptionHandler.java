package com.hrbank3.hrbank3.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /// 개발 중 발생하는 에러 핸들링 추가

  // 400 BadRequest
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException e, HttpServletRequest request) {
    log.warn("IllegalArgumentException 발생: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), e.getMessage(),
            request.getRequestURI()));
  }

  // 404 Not Found
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(
      NoSuchElementException e, HttpServletRequest request) {
    log.warn("NoSuchElementException 발생: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), e.getMessage(),
            request.getRequestURI()));
  }

  // 409 Conflict - 중복 백업 시도 등
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e,
      HttpServletRequest request) {
    log.warn("IllegalStateException 발생: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            ErrorResponse.of(HttpStatus.CONFLICT.value(), e.getMessage(), request.getRequestURI()));
  }

  // 400 Bad Request - 파라미터 타입 불일치
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e, HttpServletRequest request) {
    log.warn("MethodArgumentTypeMismatchException 발생: {}", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "요청 파라미터의 타입이 올바르지 않습니다.",
            request.getRequestURI()));
  }

  // 404 Not Found - 잘못된 URL 경로 요청
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
      NoResourceFoundException e, HttpServletRequest request) {
    log.warn("NoResourceFoundException 발생: 잘못된 URL 요청 - {}", e.getResourcePath());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "요청하신 리소스를 찾을 수 없습니다.",
            request.getRequestURI()));
  }

  // 400 Bad Request - 유효성 검사 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      org.springframework.web.bind.MethodArgumentNotValidException e, HttpServletRequest request) {
    log.warn("MethodArgumentNotValidException 발생: 유효성 검사 실패");
    
    String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), errorMessage,
            request.getRequestURI()));
  }


  // 500 Internal Server Error - 나머지 모든 서버 에러
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
    log.error("Internal Server Error: 서버 내부 에러 발생", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다.",
            request.getRequestURI()));
  }
}