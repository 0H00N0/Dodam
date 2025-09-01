package com.dodam.product.exception;

/**
 * 권한이 없는 접근이나 작업 시도 시 발생하는 예외
 * 인증되지 않은 사용자나 권한 부족 시 사용합니다.
 */
public class UnauthorizedException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "UNAUTHORIZED_ACCESS";

    /**
     * 기본 생성자
     * @param message 예외 메시지
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * 에러 코드와 메시지를 받는 생성자
     * @param errorCode 에러 코드
     * @param message 예외 메시지
     */
    public UnauthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * 작업과 리소스 타입으로 메시지를 생성하는 편의 생성자
     * @param action 시도한 작업 (예: "수정", "삭제")
     * @param resourceType 리소스 타입 (예: "리뷰", "상품")
     */
    public UnauthorizedException(String action, String resourceType) {
        super(String.format("%s에 대한 %s 권한이 없습니다.", resourceType, action));
    }

    /**
     * 회원 ID와 작업으로 메시지를 생성하는 편의 생성자
     * @param memberId 회원 ID
     * @param action 시도한 작업
     */
    public UnauthorizedException(Long memberId, String action) {
        super(String.format("회원(ID: %d)은 해당 작업('%s')에 대한 권한이 없습니다.", memberId, action));
    }

    @Override
    protected String getDefaultErrorCode() {
        return DEFAULT_ERROR_CODE;
    }
}