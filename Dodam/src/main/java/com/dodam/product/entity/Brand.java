package com.dodam.product.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.dodam.admin.dto.AdminBrandDto.Response.ResponseBuilder;

import lombok.*;

/**
 * 브랜드 엔티티
 * 
 * <p>상품의 브랜드 정보를 관리합니다.</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "BRAND", indexes = {
    @Index(name = "idx_brand_name", columnList = "brand_name"),
    @Index(name = "idx_brand_active", columnList = "is_active")
})
public class Brand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long brandId;
    
    @Column(name = "brand_name", nullable = false, length = 50, unique = true)
    private String brandName;
    
    @Column(name = "brand_logo_url", length = 200)
    private String brandLogoUrl;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
    
    /**
     * 기본 생성자 (테스트용 public 접근자)
     */
    public Brand() {}
    
    /**
     * 브랜드 생성자
     * 
     * @param brandName 브랜드명
     */
    public Brand(String brandName) {
        this.brandName = brandName;
        this.isActive = true;
    }
    
    /**
     * 브랜드 생성자 (로고 포함)
     * 
     * @param brandName 브랜드명
     * @param brandLogoUrl 브랜드 로고 URL
     */
    public Brand(String brandName, String brandLogoUrl) {
        this(brandName);
        this.brandLogoUrl = brandLogoUrl;
    }
    
    // === 비즈니스 메서드 ===
    
    /**
     * 브랜드를 비활성화합니다.
     */
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * 브랜드를 활성화합니다.
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * 브랜드 정보를 수정합니다.
     * 
     * @param brandName 브랜드명
     * @param brandLogoUrl 브랜드 로고 URL
     * @param description 브랜드 설명
     */
    public void updateInfo(String brandName, String brandLogoUrl, String description) {
        this.brandName = brandName;
        this.brandLogoUrl = brandLogoUrl;
        this.description = description;
    }
    
    /**
     * 활성화된 브랜드인지 확인합니다.
     * 
     * @return 활성화 여부
     */
    public boolean isActive() {
        return isActive;
    }
    
    // === Getters and Setters ===
    
    public Long getBrandId() {
        return brandId;
    }
    
    public String getBrandName() {
        return brandName;
    }
    
    public String getBrandLogoUrl() {
        return brandLogoUrl;
    }
    
    public void setBrandLogoUrl(String brandLogoUrl) {
        this.brandLogoUrl = brandLogoUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }
    
    // === 테스트용 setter 메서드들 ===
    
    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }
    
    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    @OneToMany(mappedBy = "brand", orphanRemoval = false)
    private List<Product> product = new ArrayList<>();
}