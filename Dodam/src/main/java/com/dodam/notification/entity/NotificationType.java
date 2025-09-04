package com.dodam.notification.entity;

public enum NotificationType {
    SYSTEM("시스템"), // 시스템 알림
    BOARD("게시판"), // 게시판 관련 알림
    MEMBER("멤버"), // 멤버 관련 알림
    PRODUCT("상품"), // 상품 관련 알림
    ADMIN("관리자"), // 관리자 알림
    EVENT("이벤트"), // 이벤트 알림
    WARNING("경고"), // 경고 알림
    INFO("정보"); // 정보성 알림
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}