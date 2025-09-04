package com.dodam.product.common.enums;

/**
 * 상품 옵션 타입 열거형
 * 
 * <p>상품 옵션의 종류를 분류하는 타입입니다.</p>
 * 
 * @since 1.0.0
 */
public enum OptionType {
    
    /**
     * 색상 옵션
     */
    COLOR("색상"),
    
    /**
     * 크기 옵션
     */
    SIZE("크기"),
    
    /**
     * 소재 옵션
     */
    MATERIAL("소재"),
    
    /**
     * 용량 옵션
     */
    CAPACITY("용량"),
    
    /**
     * 스타일 옵션
     */
    STYLE("스타일"),
    
    /**
     * 기타 옵션
     */
    OTHER("기타");
    
    private final String description;
    
    OptionType(String description) {
        this.description = description;
    }
    
    /**
     * 옵션 타입에 대한 한국어 설명을 반환합니다.
     * 
     * @return 타입 설명
     */
    public String getDescription() {
        return description;
    }
}