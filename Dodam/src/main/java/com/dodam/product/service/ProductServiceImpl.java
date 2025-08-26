package com.dodam.product.service;

import com.dodam.product.common.enums.ProductStatus;
import com.dodam.product.dto.request.ProductCreateRequest;
import com.dodam.product.dto.request.ProductUpdateRequest;
import com.dodam.product.dto.response.ProductDetailResponse;
import com.dodam.product.dto.response.ProductResponse;
import com.dodam.product.entity.Brand;
import com.dodam.product.entity.Category;
import com.dodam.product.entity.Product;
import com.dodam.product.exception.ProductNotFoundException;
import com.dodam.product.repository.BrandRepository;
import com.dodam.product.repository.CategoryRepository;
import com.dodam.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 서비스 구현체
 * 
 * <p>상품 도메인의 비즈니스 로직을 구현하는 서비스 클래스입니다.</p>
 * 
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    
    /**
     * 생성자 주입
     * 
     * @param productRepository 상품 Repository
     * @param categoryRepository 카테고리 Repository
     * @param brandRepository 브랜드 Repository
     */
    public ProductServiceImpl(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }
    
    // === 기본 CRUD 메서드 ===
    
    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        logger.info("새로운 상품 등록 시작 - 상품명: {}, 카테고리ID: {}", 
                   request.getProductName(), request.getCategoryId());
        
        // 카테고리 조회 및 검증
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + request.getCategoryId()));
        
        // 브랜드 조회 (선택사항)
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다: " + request.getBrandId()));
        }
        
        // 상품 엔티티 생성
        Product product = new Product(request.getProductName(), category, request.getPrice());
        product.setBrand(brand);
        product.setImageUrl(request.getImageUrl());
        
        // 상품 저장
        Product savedProduct = productRepository.save(product);
        
        logger.info("상품 등록 완료 - 상품ID: {}, 상품명: {}", 
                   savedProduct.getProductId(), savedProduct.getProductName());
        
        return ProductResponse.from(savedProduct);
    }
    
    @Override
    public ProductResponse findById(Long productId) {
        logger.debug("상품 조회 - 상품ID: {}", productId);
        
        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        return ProductResponse.from(product);
    }
    
    @Override
    public ProductDetailResponse findDetailById(Long productId) {
        logger.debug("상품 상세 조회 - 상품ID: {}", productId);
        
        Product product = productRepository.findByIdWithFullDetails(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        return ProductDetailResponse.from(product);
    }
    
    @Override
    public Page<ProductResponse> findAll(Pageable pageable) {
        logger.debug("전체 상품 목록 조회 - 페이지: {}, 크기: {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(ProductResponse::from);
    }
    
    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request) {
        logger.info("상품 수정 시작 - 상품ID: {}", productId);
        
        // 기존 상품 조회
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        // 수정할 필드가 있는지 확인
        if (!request.hasUpdates()) {
            logger.debug("수정할 내용이 없음 - 상품ID: {}", productId);
            return ProductResponse.from(product);
        }
        
        // 상품 정보 수정
        if (request.getProductName() != null && request.getPrice() != null) {
            product.updateInfo(request.getProductName(), request.getPrice());
        } else if (request.getProductName() != null) {
            product.updateInfo(request.getProductName(), product.getPrice());
        } else if (request.getPrice() != null) {
            product.updateInfo(product.getProductName(), request.getPrice());
        }
        
        // 카테고리 변경
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + request.getCategoryId()));
            product.setCategory(category);
        }
        
        // 브랜드 변경
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다: " + request.getBrandId()));
            product.setBrand(brand);
        }
        
        // 이미지 URL 변경
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        
        // 상태 변경
        if (request.getStatus() != null) {
            product.changeStatus(request.getStatus());
        }
        
        Product updatedProduct = productRepository.save(product);
        
        logger.info("상품 수정 완료 - 상품ID: {}, 상품명: {}", 
                   updatedProduct.getProductId(), updatedProduct.getProductName());
        
        return ProductResponse.from(updatedProduct);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        logger.info("상품 삭제 시작 - 상품ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        // 소프트 삭제 (상태를 DELETED로 변경)
        product.changeStatus(ProductStatus.DELETED);
        productRepository.save(product);
        
        logger.info("상품 삭제 완료 - 상품ID: {}", productId);
    }
    
    // === 상태 관리 메서드 ===
    
    @Override
    @Transactional
    public ProductResponse updateProductStatus(Long productId, ProductStatus newStatus) {
        logger.info("상품 상태 변경 시작 - 상품ID: {}, 새로운 상태: {}", productId, newStatus);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        ProductStatus oldStatus = product.getStatus();
        product.changeStatus(newStatus);
        
        Product updatedProduct = productRepository.save(product);
        
        logger.info("상품 상태 변경 완료 - 상품ID: {}, 이전 상태: {} → 새로운 상태: {}", 
                   productId, oldStatus, newStatus);
        
        return ProductResponse.from(updatedProduct);
    }
    
    @Override
    @Transactional
    public int updateProductStatusBatch(List<Long> productIds, ProductStatus newStatus) {
        logger.info("상품 상태 일괄 변경 시작 - 상품 개수: {}, 새로운 상태: {}", 
                   productIds.size(), newStatus);
        
        int updatedCount = productRepository.updateStatusByIds(productIds, newStatus);
        
        logger.info("상품 상태 일괄 변경 완료 - 변경된 상품 개수: {}", updatedCount);
        
        return updatedCount;
    }
    
    // === 검색 및 필터링 메서드 ===
    
    @Override
    public Page<ProductResponse> findByStatus(ProductStatus status, Pageable pageable) {
        logger.debug("상태별 상품 조회 - 상태: {}, 페이지: {}", status, pageable.getPageNumber());
        
        Page<Product> products = productRepository.findByStatusWithDetails(status, pageable);
        return products.map(ProductResponse::from);
    }
    
    @Override
    public Page<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        logger.debug("카테고리별 상품 조회 - 카테고리ID: {}, 페이지: {}", categoryId, pageable.getPageNumber());
        
        Page<Product> products = productRepository.findByCategoryCategoryId(categoryId, pageable);
        return products.map(ProductResponse::from);
    }
    
    @Override
    public Page<ProductResponse> findByBrand(Long brandId, Pageable pageable) {
        logger.debug("브랜드별 상품 조회 - 브랜드ID: {}, 페이지: {}", brandId, pageable.getPageNumber());
        
        Page<Product> products = productRepository.findByBrandBrandId(brandId, pageable);
        return products.map(ProductResponse::from);
    }
    
    @Override
    public Page<ProductResponse> findByProductName(String productName, Pageable pageable) {
        logger.debug("상품명 검색 - 검색어: {}, 페이지: {}", productName, pageable.getPageNumber());
        
        Page<Product> products = productRepository.findByProductNameContainingIgnoreCase(productName, pageable);
        return products.map(ProductResponse::from);
    }
    
    @Override
    public Page<ProductResponse> searchByKeyword(String keyword, ProductStatus status, Pageable pageable) {
        logger.debug("키워드 검색 - 검색어: {}, 상태: {}, 페이지: {}", 
                    keyword, status, pageable.getPageNumber());
        
        ProductStatus searchStatus = (status != null) ? status : ProductStatus.ACTIVE;
        Page<Product> products = productRepository.findByKeywordSearch(keyword, searchStatus, pageable);
        return products.map(ProductResponse::from);
    }
    
    @Override
    public Page<ProductResponse> findBySearchCriteria(Long categoryId, Long brandId, 
                                                     BigDecimal minPrice, BigDecimal maxPrice, 
                                                     ProductStatus status, Pageable pageable) {
        logger.debug("복합 조건 검색 - 카테고리: {}, 브랜드: {}, 가격 범위: {}-{}, 상태: {}", 
                    categoryId, brandId, minPrice, maxPrice, status);
        
        Page<Product> products = productRepository.findBySearchCriteria(
                categoryId, minPrice, maxPrice, status, pageable);
        return products.map(ProductResponse::from);
    }
    
    // === 가격 계산 메서드 ===
    
    @Override
    public BigDecimal calculateTotalPrice(Long productId, List<Long> selectedOptionIds) {
        logger.debug("상품 총 가격 계산 - 상품ID: {}, 선택된 옵션: {}", productId, selectedOptionIds);
        
        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        BigDecimal totalPrice = product.calculateTotalPrice(selectedOptionIds);
        
        logger.debug("가격 계산 완료 - 상품ID: {}, 총 가격: {}", productId, totalPrice);
        
        return totalPrice;
    }
    
    // === 통계 및 집계 메서드 ===
    
    @Override
    public long countByCategory(Long categoryId, ProductStatus status) {
        logger.debug("카테고리별 상품 개수 조회 - 카테고리ID: {}, 상태: {}", categoryId, status);
        
        return productRepository.countByCategoryAndStatus(categoryId, status);
    }
    
    @Override
    public long countByBrand(Long brandId, ProductStatus status) {
        logger.debug("브랜드별 상품 개수 조회 - 브랜드ID: {}, 상태: {}", brandId, status);
        
        return productRepository.countByBrandAndStatus(brandId, status);
    }
    
    @Override
    public long countByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, ProductStatus status) {
        logger.debug("가격 범위별 상품 개수 조회 - 가격 범위: {}-{}, 상태: {}", minPrice, maxPrice, status);
        
        return productRepository.countByPriceRangeAndStatus(minPrice, maxPrice, status);
    }
    
    @Override
    public List<ProductResponse> findPopularProducts(int limit) {
        logger.debug("인기 상품 조회 - 개수: {}", limit);
        
        List<Product> products = productRepository.findPopularProducts(limit);
        return products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
    
    // === 재고 관련 메서드 ===
    
    @Override
    public boolean isOrderable(Long productId, int quantity) {
        logger.debug("주문 가능 여부 확인 - 상품ID: {}, 수량: {}", productId, quantity);
        
        try {
            Product product = productRepository.findByIdWithDetails(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            
            boolean orderable = product.isOrderable() && 
                               product.getInventory() != null && 
                               product.getInventory().getAvailableQuantity() >= quantity;
            
            logger.debug("주문 가능 여부 - 상품ID: {}, 결과: {}", productId, orderable);
            
            return orderable;
        } catch (Exception e) {
            logger.error("주문 가능 여부 확인 중 오류 발생 - 상품ID: {}", productId, e);
            return false;
        }
    }
    
    @Override
    public boolean existsById(Long productId) {
        logger.debug("상품 존재 여부 확인 - 상품ID: {}", productId);
        
        return productRepository.existsById(productId);
    }
}