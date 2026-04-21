package com.hrbank3.hrbank3.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /// 개발 중 발생하는 에러 핸들링 추가

  // 400 BadRequest
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException e, HttpServletRequest request) {
    ErrorResponse response = ErrorResponse.of(
        HttpStatus.BAD_REQUEST.value(),
        e.getMessage(),
        request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  // 404 Not Found
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNoSuchElementException(
      NoSuchElementException e, HttpServletRequest request) {
    ErrorResponse response = ErrorResponse.of(
        HttpStatus.NOT_FOUND.value(),
        e.getMessage(),
        request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  // 409 Conflict (중복 백업 시도 등)
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e, HttpServletRequest request) {
    ErrorResponse response = ErrorResponse.of(
        HttpStatus.CONFLICT.value(),
        e.getMessage(),
        request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }


  // 나머지 모든 서버 에러 500 Internal Server Error
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
    log.error("Internal Server Error: ", e);

    ErrorResponse response = ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "서버 내부 오류가 발생했습니다.",
        request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}