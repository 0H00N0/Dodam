package com.dodam.product.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외
 * 데이터베이스에서 조회한 엔티티가 없는 경우 사용합니다.
 */
public class ResourceNotFoundException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * 기본 생성자
     * @param message 예외 메시지
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 에러 코드와 메시지를 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     */
    public ResourceNotFoundException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 원인 예외와 함께 생성하는 생성자
     * @param message 예외 메시지
     * @param cause 원인 예외
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 리소스 타입과 ID로 메시지를 생성하는 편의 팩토리 메서드
     * @param resourceType 리소스 타입 (예: "Product", "Category")
     * @param resourceId 리소스 ID
     */
    public static ResourceNotFoundException forResourceId(String resourceType, Long resourceId) {
        return new ResourceNotFoundException(String.format("%s를 찾을 수 없습니다. ID: %d", resourceType, resourceId));
    }

    /**
     * 리소스 타입과 식별자로 메시지를 생성하는 편의 팩토리 메서드
     * @param resourceType 리소스 타입 (예: "Product", "Category")
     * @param identifier 식별자 (예: name, code)
     */
    public static ResourceNotFoundException forIdentifier(String resourceType, String identifier) {
        return new ResourceNotFoundException(String.format("%s를 찾을 수 없습니다. 식별자: %s", resourceType, identifier));
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}