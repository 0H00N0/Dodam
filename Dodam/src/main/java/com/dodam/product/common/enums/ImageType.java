package com.dodam.product.common.enums;

/**
 * 상품 이미지 타입 열거형
 * 
 * <p>상품 이미지의 용도를 분류하는 타입입니다.</p>
 * 
 * @since 1.0.0
 */
public enum ImageType {
    
    /**
     * 썸네일 이미지 - 목록에서 보여지는 대표 이미지
     */
    THUMBNAIL("썸네일", true),
    
    /**
     * 상세 이미지 - 상품 상세페이지의 주요 이미지
     */
    DETAIL("상세", false),
    
    /**
     * 갤러리 이미지 - 추가 참고 이미지
     */
    GALLERY("갤러리", false),
    
    /**
     * 옵션 이미지 - 특정 옵션을 나타내는 이미지
     */
    OPTION("옵션", false);
    
    private final String description;
    private final boolean isRequired;
    
    ImageType(String description, boolean isRequired) {
        this.description = description;
        this.isRequired = isRequired;
    }
    
    /**
     * 이미지 타입에 대한 한국어 설명을 반환합니다.
     * 
     * @return 타입 설명
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 필수 이미지 타입인지 확인합니다.
     * 
     * @return 필수 여부
     */
    public boolean isRequired() {
        return isRequired;
    }
}