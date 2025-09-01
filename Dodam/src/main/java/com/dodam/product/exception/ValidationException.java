package com.dodam.product.exception;

/**
 * 유효성 검증 실패 시 발생하는 예외
 * 입력값 검증이나 데이터 무결성 위반 시 사용합니다.
 */
public class ValidationException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "VALIDATION_FAILED";

    /**
     * 기본 생성자
     * @param message 예외 메시지
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * 에러 코드와 메시지를 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     */
    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 필드명과 유효성 위반 내용으로 메시지를 생성하는 편의 생성자
     * @param fieldName 필드명
     * @param violationMessage 위반 내용
     */
    public ValidationException(String fieldName, String violationMessage) {
        super(String.format("%s 필드 유효성 검증 실패: %s", fieldName, violationMessage));
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}