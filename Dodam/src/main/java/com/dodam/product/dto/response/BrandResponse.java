package com.dodam.product.dto.response;

import com.dodam.product.entity.Brand;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 브랜드 응답 DTO
 * 
 * <p>브랜드 정보를 클라이언트에게 전달하기 위한 응답 객체입니다.</p>
 * 
 * @since 1.0.0
 */
@Schema(description = "브랜드 응답")
public class BrandResponse {
    
    @Schema(description = "브랜드 ID", example = "1")
    private final Long brandId;
    
    @Schema(description = "브랜드명", example = "삼성")
    private final String brandName;
    
    @Schema(description = "브랜드 로고 URL", example = "https://example.com/logo/samsung.png")
    private final String brandLogoUrl;
    
    @Schema(description = "브랜드 설명", example = "글로벌 전자제품 브랜드")
    private final String description;
    
    @Schema(description = "활성화 여부", example = "true")
    private final Boolean isActive;
    
    @Schema(description = "브랜드별 상품 개수", example = "25")
    private final Long productCount;
    
    /**
     * 생성자
     */
    private BrandResponse(Long brandId, String brandName, String brandLogoUrl,
                         String description, Boolean isActive, Long productCount) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.brandLogoUrl = brandLogoUrl;
        this.description = description;
        this.isActive = isActive;
        this.productCount = productCount;
    }
    
    /**
     * Brand 엔티티로부터 BrandResponse를 생성합니다.
     * 
     * @param brand Brand 엔티티
     * @return BrandResponse 객체
     */
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
            brand.getBrandId(),
            brand.getBrandName(),
            brand.getBrandLogoUrl(),
            brand.getDescription(),
            brand.getIsActive(),
            (long) brand.getProducts().size()
        );
    }
    
    /**
     * Brand 엔티티로부터 상품 개수가 포함된 BrandResponse를 생성합니다.
     * 
     * @param brand Brand 엔티티
     * @param productCount 상품 개수
     * @return BrandResponse 객체
     */
    public static BrandResponse of(Brand brand, Long productCount) {
        return new BrandResponse(
            brand.getBrandId(),
            brand.getBrandName(),
            brand.getBrandLogoUrl(),
            brand.getDescription(),
            brand.getIsActive(),
            productCount
        );
    }
    
    /**
     * 직접 정보를 지정하여 BrandResponse를 생성합니다.
     */
    public static BrandResponse of(Long brandId, String brandName, String brandLogoUrl,
                                  String description, Boolean isActive, Long productCount) {
        return new BrandResponse(brandId, brandName, brandLogoUrl, description, isActive, productCount);
    }
    
    // === Getters ===
    
    public Long getBrandId() {
        return brandId;
    }
    
    public String getBrandName() {
        return brandName;
    }
    
    public String getBrandLogoUrl() {
        return brandLogoUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public Long getProductCount() {
        return productCount;
    }
}