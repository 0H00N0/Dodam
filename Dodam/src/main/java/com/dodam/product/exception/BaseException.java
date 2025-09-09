package com.dodam.product.exception;

/**
 * 모든 커스텀 예외의 기본 클래스
 * 공통 기능과 메시지 처리를 담당합니다.
 */
public abstract class BaseException extends RuntimeException {

    /**
     * 에러 코드
     */
    private final String errorCode;

    /**
     * 기본 생성자
     * @param message 예외 메시지
     */
    protected BaseException(String message) {
        super(message);
        this.errorCode = getDefaultErrorCode();
    }

    /**
     * 에러 코드와 메시지를 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     */
    protected BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    protected BaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = getDefaultErrorCode();
    }

    /**
     * 에러 코드, 메시지, 원인 예외를 모두 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    protected BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 에러 코드 조회
     * @return 에러 코드
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 기본 에러 코드를 반환하는 추상 메소드
     * 각 예외 클래스에서 고유한 에러 코드를 정의해야 합니다.
     * @return 기본 에러 코드
     */
    protected abstract String getDefaultErrorCode();

    /**
     * 상세한 에러 정보를 문자열로 반환
     * @return 에러 정보 문자열
     */
    @Override
    public String toString() {
        return String.format("%s [%s]: %s", 
                           getClass().getSimpleName(), 
                           errorCode, 
                           getMessage());
    }
}