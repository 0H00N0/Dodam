package com.dodam.product.service;

import com.dodam.product.dto.response.BrandResponse;
import com.dodam.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 브랜드 서비스 인터페이스
 * 
 * <p>브랜드 관련 비즈니스 로직을 정의하는 서비스 인터페이스입니다.</p>
 * 
 * @since 1.0.0
 */
public interface BrandService {
    
    /**
     * 모든 활성화된 브랜드를 조회합니다.
     * 
     * @return 브랜드 목록
     */
    List<BrandResponse> findAllActiveBrands();
    
    /**
     * 브랜드 ID로 브랜드를 조회합니다.
     * 
     * @param brandId 브랜드 ID
     * @return 브랜드 정보
     */
    BrandResponse findById(Long brandId);
    
    /**
     * 브랜드명으로 브랜드를 검색합니다.
     * 
     * @param brandName 브랜드명
     * @return 브랜드 목록
     */
    List<BrandResponse> findByBrandNameContaining(String brandName);
    
    /**
     * 브랜드별 상품을 조회합니다.
     * 
     * @param brandId 브랜드 ID
     * @param pageable 페이징 정보
     * @return 상품 목록
     */
    Page<ProductResponse> findProductsByBrand(Long brandId, Pageable pageable);
    
    /**
     * 브랜드가 존재하는지 확인합니다.
     * 
     * @param brandId 브랜드 ID
     * @return 존재 여부
     */
    boolean existsById(Long brandId);
    
    /**
     * 활성화된 브랜드인지 확인합니다.
     * 
     * @param brandId 브랜드 ID
     * @return 활성화 여부
     */
    boolean isActiveBrand(Long brandId);
}