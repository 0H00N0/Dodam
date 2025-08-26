package com.dodam.product.service;

import com.dodam.product.common.enums.ProductStatus;
import com.dodam.product.dto.request.ProductCreateRequest;
import com.dodam.product.dto.request.ProductUpdateRequest;
import com.dodam.product.dto.response.ProductDetailResponse;
import com.dodam.product.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 상품 서비스 인터페이스
 * 
 * <p>상품 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스입니다.</p>
 * 
 * @since 1.0.0
 */
public interface ProductService {
    
    // === 기본 CRUD 메서드 ===
    
    /**
     * 새로운 상품을 등록합니다.
     * 
     * @param request 상품 등록 요청 데이터
     * @return 등록된 상품 정보
     */
    ProductResponse createProduct(ProductCreateRequest request);
    
    /**
     * 상품 ID로 상품 정보를 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 상품 정보
     */
    ProductResponse findById(Long productId);
    
    /**
     * 상품 ID로 상품 상세 정보를 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 상품 상세 정보
     */
    ProductDetailResponse findDetailById(Long productId);
    
    /**
     * 모든 상품을 페이징하여 조회합니다.
     * 
     * @param pageable 페이징 정보
     * @return 상품 목록 페이지
     */
    Page<ProductResponse> findAll(Pageable pageable);
    
    /**
     * 상품 정보를 수정합니다.
     * 
     * @param productId 상품 ID
     * @param request 수정 요청 데이터
     * @return 수정된 상품 정보
     */
    ProductResponse updateProduct(Long productId, ProductUpdateRequest request);
    
    /**
     * 상품을 삭제합니다. (소프트 삭제)
     * 
     * @param productId 상품 ID
     */
    void deleteProduct(Long productId);
    
    // === 상태 관리 메서드 ===
    
    /**
     * 상품 상태를 변경합니다.
     * 
     * @param productId 상품 ID
     * @param newStatus 새로운 상태
     * @return 상태가 변경된 상품 정보
     */
    ProductResponse updateProductStatus(Long productId, ProductStatus newStatus);
    
    /**
     * 여러 상품의 상태를 일괄 변경합니다.
     * 
     * @param productIds 상품 ID 목록
     * @param newStatus 새로운 상태
     * @return 변경된 상품 수
     */
    int updateProductStatusBatch(List<Long> productIds, ProductStatus newStatus);
    
    // === 검색 및 필터링 메서드 ===
    
    /**
     * 상품 상태별로 상품을 조회합니다.
     * 
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 상품 목록 페이지
     */
    Page<ProductResponse> findByStatus(ProductStatus status, Pageable pageable);
    
    /**
     * 카테고리별로 상품을 조회합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보
     * @return 상품 목록 페이지
     */
    Page<ProductResponse> findByCategory(Long categoryId, Pageable pageable);
    
    /**
     * 브랜드별로 상품을 조회합니다.
     * 
     * @param brandId 브랜드 ID
     * @param pageable 페이징 정보
     * @return 상품 목록 페이지
     */
    Page<ProductResponse> findByBrand(Long brandId, Pageable pageable);
    
    /**
     * 상품명으로 상품을 검색합니다.
     * 
     * @param productName 검색할 상품명
     * @param pageable 페이징 정보
     * @return 상품 목록 페이지
     */
    Page<ProductResponse> findByProductName(String productName, Pageable pageable);
    
    /**
     * 키워드로 상품을 검색합니다. (상품명, 브랜드명, 카테고리명에서 검색)
     * 
     * @param keyword 검색 키워드
     * @param status 상품 상태 (null이면 전체)
     * @param pageable 페이징 정보
     * @return 상품 목록 페이지
     */
    Page<ProductResponse> searchByKeyword(String keyword, ProductStatus status, Pageable pageable);
    
    /**
     * 복합 조건으로 상품을 검색합니다.
     * 
     * @param categoryId 카테고리 ID (선택사항)
     * @param brandId 브랜드 ID (선택사항)
     * @param minPrice 최소 가격 (선택사항)
     * @param maxPrice 최대 가격 (선택사항)
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 상품 목록 페이지
     */
    Page<ProductResponse> findBySearchCriteria(Long categoryId, Long brandId, 
                                              BigDecimal minPrice, BigDecimal maxPrice, 
                                              ProductStatus status, Pageable pageable);
    
    // === 가격 계산 메서드 ===
    
    /**
     * 선택된 옵션을 포함한 상품의 총 가격을 계산합니다.
     * 
     * @param productId 상품 ID
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 총 가격
     */
    BigDecimal calculateTotalPrice(Long productId, List<Long> selectedOptionIds);
    
    // === 통계 및 집계 메서드 ===
    
    /**
     * 카테고리별 상품 개수를 조회합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param status 상품 상태
     * @return 상품 개수
     */
    long countByCategory(Long categoryId, ProductStatus status);
    
    /**
     * 브랜드별 상품 개수를 조회합니다.
     * 
     * @param brandId 브랜드 ID
     * @param status 상품 상태
     * @return 상품 개수
     */
    long countByBrand(Long brandId, ProductStatus status);
    
    /**
     * 가격 범위별 상품 개수를 조회합니다.
     * 
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param status 상품 상태
     * @return 상품 개수
     */
    long countByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, ProductStatus status);
    
    /**
     * 인기 상품 목록을 조회합니다.
     * 
     * @param limit 조회할 개수
     * @return 인기 상품 목록
     */
    List<ProductResponse> findPopularProducts(int limit);
    
    // === 재고 관련 메서드 ===
    
    /**
     * 상품의 주문 가능 여부를 확인합니다.
     * 
     * @param productId 상품 ID
     * @param quantity 주문하려는 수량
     * @return 주문 가능 여부
     */
    boolean isOrderable(Long productId, int quantity);
    
    /**
     * 상품이 존재하는지 확인합니다.
     * 
     * @param productId 상품 ID
     * @return 존재 여부
     */
    boolean existsById(Long productId);
}