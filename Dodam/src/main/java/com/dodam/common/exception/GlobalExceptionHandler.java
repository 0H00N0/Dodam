package com.dodam.common.exception;

import com.dodam.common.dto.ErrorResponse;
import com.dodam.product.exception.DuplicateSkuException;
import com.dodam.product.exception.InsufficientStockException;
import com.dodam.product.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리 핸들러
 * 
 * <p>애플리케이션 전체에서 발생하는 예외를 일관된 형태로 처리합니다.</p>
 * 
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    // === 비즈니스 예외 처리 ===
    
    /**
     * 상품을 찾을 수 없는 경우
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException e, HttpServletRequest request) {
        
        log.warn("상품을 찾을 수 없습니다: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "PRODUCT_001", 
            "상품을 찾을 수 없습니다.", 
            e.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
    }
    
    /**
     * 재고 부족인 경우
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException e, HttpServletRequest request) {
        
        log.warn("재고가 부족합니다: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "INVENTORY_001", 
            "재고가 부족합니다.", 
            e.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * 중복된 SKU인 경우
     */
    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSku(
            DuplicateSkuException e, HttpServletRequest request) {
        
        log.warn("중복된 SKU입니다: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "PRODUCT_002", 
            "이미 존재하는 SKU입니다.", 
            e.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(errorResponse);
    }
    
    // === 검증 예외 처리 ===
    
    /**
     * Request Body 검증 실패 (@Valid 사용 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        log.warn("요청 데이터 검증에 실패했습니다: {}", e.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.of(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_001", 
            "요청 데이터 검증에 실패했습니다.", 
            fieldErrors,
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(errorResponse);
    }
    
    /**
     * Form 데이터 바인딩 실패
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException e, HttpServletRequest request) {
        
        log.warn("데이터 바인딩에 실패했습니다: {}", e.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorResponse.FieldError.of(
                error.getField(),
                error.getRejectedValue(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_002", 
            "데이터 바인딩에 실패했습니다.", 
            fieldErrors,
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * PathVariable이나 RequestParam 검증 실패
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException e, HttpServletRequest request) {
        
        log.warn("제약 조건 위반: {}", e.getMessage());
        
        List<ErrorResponse.FieldError> fieldErrors = e.getConstraintViolations()
            .stream()
            .map(violation -> ErrorResponse.FieldError.of(
                getPropertyName(violation),
                violation.getInvalidValue(),
                violation.getMessage()
            ))
            .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "VALIDATION_003", 
            "제약 조건을 위반했습니다.", 
            fieldErrors,
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    // === HTTP 요청 관련 예외 처리 ===
    
    /**
     * JSON 파싱 실패
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        
        log.warn("HTTP 메시지를 읽을 수 없습니다: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "REQUEST_001", 
            "요청 데이터 형식이 올바르지 않습니다.", 
            "JSON 형식을 확인해주세요.",
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * 필수 요청 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        
        log.warn("필수 요청 파라미터가 누락되었습니다: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "REQUEST_002", 
            "필수 요청 파라미터가 누락되었습니다.", 
            String.format("파라미터 '%s' (타입: %s)이 필요합니다.", e.getParameterName(), e.getParameterType()),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * 메서드 인수 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        
        log.warn("메서드 인수 타입이 일치하지 않습니다: {}", e.getMessage());
        
        String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown";
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "REQUEST_003", 
            "요청 파라미터 타입이 올바르지 않습니다.", 
            String.format("파라미터 '%s'는 %s 타입이어야 합니다.", e.getName(), requiredType),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * 핸들러를 찾을 수 없는 경우 (404)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException e, HttpServletRequest request) {
        
        log.warn("요청한 리소스를 찾을 수 없습니다: {} {}", e.getHttpMethod(), e.getRequestURL());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "RESOURCE_001", 
            "요청한 리소스를 찾을 수 없습니다.", 
            String.format("%s %s", e.getHttpMethod(), e.getRequestURL()),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
    }
    
    /**
     * 잘못된 상태 전이 예외
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException e, HttpServletRequest request) {
        
        log.warn("잘못된 상태 전이: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "STATE_001", 
            "잘못된 상태입니다.", 
            e.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    /**
     * 잘못된 인수 예외
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {
        
        log.warn("잘못된 인수: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "ARGUMENT_001", 
            "잘못된 요청 인수입니다.", 
            e.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errorResponse);
    }
    
    // === 일반 예외 처리 ===
    
    /**
     * 기타 모든 예외 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception e, HttpServletRequest request) {
        
        log.error("예상하지 못한 오류가 발생했습니다", e);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "INTERNAL_001", 
            "내부 서버 오류가 발생했습니다.", 
            "요청 처리 중 예상하지 못한 오류가 발생했습니다. 관리자에게 문의하세요.",
            request.getRequestURI()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse);
    }
    
    // === 유틸리티 메서드 ===
    
    /**
     * ConstraintViolation에서 프로퍼티 이름을 추출합니다.
     */
    private String getPropertyName(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        String[] parts = propertyPath.split("\\.");
        return parts.length > 0 ? parts[parts.length - 1] : propertyPath;
    }
}