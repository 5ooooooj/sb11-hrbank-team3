package com.hrbank3.hrbank3.common.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    String details
) {

  public static ErrorResponse of(int status, String message, String details) {
    return new ErrorResponse(LocalDateTime.now(), status, message, details);
  }
}