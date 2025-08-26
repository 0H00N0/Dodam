package com.dodam.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 에러 응답 형식 클래스
 * 
 * <p>API 에러 발생 시 일관된 형태의 응답을 제공하기 위한 클래스입니다.</p>
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "에러 응답")
public class ErrorResponse {
    
    @Schema(description = "에러 코드", example = "PRODUCT_001")
    private final String errorCode;
    
    @Schema(description = "에러 메시지", example = "상품을 찾을 수 없습니다.")
    private final String message;
    
    @Schema(description = "상세 에러 메시지", example = "ID가 123인 상품을 찾을 수 없습니다.")
    private final String details;
    
    @Schema(description = "API 경로", example = "/api/products/123")
    private final String path;
    
    @Schema(description = "검증 실패 필드 목록")
    private final List<FieldError> fieldErrors;
    
    @Schema(description = "에러 발생 시간", example = "2024-08-26T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    
    /**
     * 기본 에러 응답 생성자
     */
    private ErrorResponse(String errorCode, String message, String details, String path, List<FieldError> fieldErrors) {
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.path = path;
        this.fieldErrors = fieldErrors;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 일반 에러 응답을 생성합니다.
     * 
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param path API 경로
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(String errorCode, String message, String path) {
        return new ErrorResponse(errorCode, message, null, path, null);
    }
    
    /**
     * 상세 정보가 포함된 에러 응답을 생성합니다.
     * 
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param details 상세 정보
     * @param path API 경로
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(String errorCode, String message, String details, String path) {
        return new ErrorResponse(errorCode, message, details, path, null);
    }
    
    /**
     * 검증 실패 에러 응답을 생성합니다.
     * 
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param fieldErrors 검증 실패 필드 목록
     * @param path API 경로
     * @return 에러 응답 객체
     */
    public static ErrorResponse of(String errorCode, String message, List<FieldError> fieldErrors, String path) {
        return new ErrorResponse(errorCode, message, null, path, fieldErrors);
    }
    
    /**
     * 필드 검증 에러 정보 클래스
     */
    @Schema(description = "필드 검증 에러")
    public static class FieldError {
        
        @Schema(description = "필드명", example = "productName")
        private final String field;
        
        @Schema(description = "거부된 값", example = "")
        private final Object rejectedValue;
        
        @Schema(description = "에러 메시지", example = "상품명은 필수입니다")
        private final String message;
        
        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }
        
        public static FieldError of(String field, Object rejectedValue, String message) {
            return new FieldError(field, rejectedValue, message);
        }
        
        // === Getters ===
        
        public String getField() {
            return field;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    // === Getters ===
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getDetails() {
        return details;
    }
    
    public String getPath() {
        return path;
    }
    
    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}