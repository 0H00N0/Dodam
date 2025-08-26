package com.dodam.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 통일된 API 응답 래퍼 클래스
 * 
 * <p>모든 API 응답을 일관된 형태로 제공하기 위한 래퍼 클래스입니다.</p>
 * 
 * @param <T> 응답 데이터의 타입
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "API 응답 래퍼")
public class ApiResponse<T> {
    
    @Schema(description = "성공 여부", example = "true")
    private final boolean success;
    
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;
    
    @Schema(description = "응답 데이터")
    private final T data;
    
    @Schema(description = "응답 시간", example = "2024-08-26T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    
    /**
     * 기본 생성자
     */
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 성공 응답을 생성합니다. (데이터 포함)
     * 
     * @param <T> 응답 데이터 타입
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", data);
    }
    
    /**
     * 성공 응답을 생성합니다. (메시지와 데이터 포함)
     * 
     * @param <T> 응답 데이터 타입
     * @param message 응답 메시지
     * @param data 응답 데이터
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * 성공 응답을 생성합니다. (메시지만)
     * 
     * @param message 응답 메시지
     * @return 성공 응답 객체
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
    
    /**
     * 실패 응답을 생성합니다.
     * 
     * @param message 실패 메시지
     * @return 실패 응답 객체
     */
    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }
    
    /**
     * 실패 응답을 생성합니다. (데이터 포함)
     * 
     * @param <T> 응답 데이터 타입
     * @param message 실패 메시지
     * @param data 응답 데이터
     * @return 실패 응답 객체
     */
    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
    
    // === Getters ===
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}