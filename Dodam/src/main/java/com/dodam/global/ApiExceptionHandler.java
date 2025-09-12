package com.dodam.global;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<?> handleRse(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of("message", e.getReason()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<?> handleUnreadable(HttpMessageNotReadableException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "MALFORMED_JSON"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleBind(MethodArgumentNotValidException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", "VALIDATION_FAILED"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> handleConstraint(DataIntegrityViolationException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("message", "DB_CONSTRAINT_VIOLATION"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleAny(Exception e) {
    // 개발 중에는 500의 원인을 프론트에서 볼 수 있게 간단 코드도 함께
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("message", "INTERNAL_SERVER_ERROR"));
  }
}
