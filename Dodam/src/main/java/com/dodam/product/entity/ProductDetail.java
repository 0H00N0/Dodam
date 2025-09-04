package com.dodam.product.entity;

import jakarta.persistence.*;

/**
 * 상품 상세 정보 엔티티
 * 
 * <p>상품의 상세 설명, 사양 등 부가 정보를 관리합니다.</p>
 * 
 * @since 1.0.0
 */
@Entity
@Table(name = "PRODUCT_DETAIL")
public class ProductDetail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;
    
    @Column(name = "features", columnDefinition = "TEXT")
    private String features;
    
    @Column(name = "care_instructions", length = 1000)
    private String careInstructions;
    
    @Column(name = "warranty_info", length = 500)
    private String warrantyInfo;
    
    @Column(name = "origin_country", length = 50)
    private String originCountry;
    
    @Column(name = "material", length = 200)
    private String material;
    
    @Column(name = "dimensions", length = 100)
    private String dimensions;
    
    @Column(name = "weight", length = 50)
    private String weight;
    
    /**
     * 기본 생성자
     */
    protected ProductDetail() {}
    
    /**
     * 상품 상세 정보 생성자
     * 
     * @param description 상품 설명
     */
    public ProductDetail(String description) {
        this.description = description;
    }
    
    // === 비즈니스 메서드 ===
    
    /**
     * 상품 상세 정보를 수정합니다.
     * 
     * @param description 상품 설명
     * @param specifications 상품 사양
     * @param features 주요 특징
     */
    public void updateInfo(String description, String specifications, String features) {
        this.description = description;
        this.specifications = specifications;
        this.features = features;
    }
    
    /**
     * 관리 정보를 업데이트합니다.
     * 
     * @param careInstructions 관리 방법
     * @param warrantyInfo 보증 정보
     * @param originCountry 원산지
     */
    public void updateManagementInfo(String careInstructions, String warrantyInfo, String originCountry) {
        this.careInstructions = careInstructions;
        this.warrantyInfo = warrantyInfo;
        this.originCountry = originCountry;
    }
    
    /**
     * 물리적 속성을 업데이트합니다.
     * 
     * @param material 소재
     * @param dimensions 치수
     * @param weight 무게
     */
    public void updatePhysicalInfo(String material, String dimensions, String weight) {
        this.material = material;
        this.dimensions = dimensions;
        this.weight = weight;
    }
    
    // === Getters and Setters ===
    
    public Long getDetailId() {
        return detailId;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSpecifications() {
        return specifications;
    }
    
    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }
    
    public String getFeatures() {
        return features;
    }
    
    public void setFeatures(String features) {
        this.features = features;
    }
    
    public String getCareInstructions() {
        return careInstructions;
    }
    
    public void setCareInstructions(String careInstructions) {
        this.careInstructions = careInstructions;
    }
    
    public String getWarrantyInfo() {
        return warrantyInfo;
    }
    
    public void setWarrantyInfo(String warrantyInfo) {
        this.warrantyInfo = warrantyInfo;
    }
    
    public String getOriginCountry() {
        return originCountry;
    }
    
    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        this.material = material;
    }
    
    public String getDimensions() {
        return dimensions;
    }
    
    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }
    
    public String getWeight() {
        return weight;
    }
    
    public void setWeight(String weight) {
        this.weight = weight;
    }
}