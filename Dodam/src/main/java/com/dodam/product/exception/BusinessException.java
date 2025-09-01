package com.dodam.product.exception;

/**
 * 비즈니스 로직 위반 시 발생하는 예외
 * 업무 규칙이나 제약사항을 위반한 경우 사용합니다.
 */
public class BusinessException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    /**
     * 기본 생성자
     * @param message 예외 메시지
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 에러 코드와 메시지를 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     */
    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}