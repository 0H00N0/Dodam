package com.dodam.product.exception;

/**
 * 중복된 리소스 생성 시도 시 발생하는 예외
 * 고유성 제약조건 위반이나 중복 데이터 생성 시 사용합니다.
 */
public class DuplicateResourceException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "DUPLICATE_RESOURCE";

    /**
     * 기본 생성자
     * @param message 예외 메시지
     */
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * 에러 코드와 메시지를 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     */
    public DuplicateResourceException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 리소스 타입과 중복된 값으로 메시지를 생성하는 편의 생성자
     * @param resourceType 리소스 타입 (예: "Category", "Product")
     * @param duplicateValue 중복된 값
     */
    public static DuplicateResourceException forDuplicateResource(String resourceType, String duplicateValue) {
        return new DuplicateResourceException(String.format("이미 존재하는 %s입니다: %s", resourceType, duplicateValue));
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}