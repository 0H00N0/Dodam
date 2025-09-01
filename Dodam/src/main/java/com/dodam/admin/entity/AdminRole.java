package com.dodam.admin.entity;

public enum AdminRole {
    ADMIN("관리자"),
    SUPER_ADMIN("슈퍼 관리자"),
    DELIVERYMAN("배송원"),
    STAFF("직원");

    private final String displayName;

    AdminRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}