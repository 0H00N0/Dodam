package com.dodam.product.exception;

import java.time.LocalDateTime;

/**
 * 만료된 리소스에 접근하거나 사용하려 할 때 발생하는 예외
 * 기프티콘 만료, 이벤트 보상 만료 등에 사용합니다.
 */
public class ExpiredException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_EXPIRED";

    /**
     * 기본 생성자
     * @param message 예외 메시지
     */
    public ExpiredException(String message) {
        super(message);
    }

    /**
     * 에러 코드와 메시지를 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     */
    public ExpiredException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 리소스 타입과 만료일로 메시지를 생성하는 편의 생성자
     * @param resourceType 리소스 타입 (예: "기프티콘", "이벤트 보상")
     * @param expiredAt 만료일시
     */
    public ExpiredException(String resourceType, LocalDateTime expiredAt) {
        super(String.format("%s가 만료되었습니다. 만료일: %s", resourceType, expiredAt));
    }

    /**
     * 리소스 타입, ID, 만료일로 메시지를 생성하는 편의 생성자
     * @param resourceType 리소스 타입
     * @param resourceId 리소스 ID
     * @param expiredAt 만료일시
     */
    public ExpiredException(String resourceType, Long resourceId, LocalDateTime expiredAt) {
        super(String.format("%s(ID: %d)가 만료되었습니다. 만료일: %s", resourceType, resourceId, expiredAt));
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}