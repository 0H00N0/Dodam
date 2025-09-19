package com.dodam.global;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
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
      log.error("[UNHANDLED] {}", e.toString(), e); // 스택 전체 출력
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(Map.of("message", "INTERNAL_SERVER_ERROR", "hint", e.getClass().getSimpleName()));
  }
  
  @ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<?> handleNoResource(NoResourceFoundException e) {
	    // 더 이상 500로 보이지 않게 404로 통일
	    return ResponseEntity.status(404).body(
	            Map.of("error", "NOT_FOUND", "path", e.getResourcePath()));
	}
  
  
}
