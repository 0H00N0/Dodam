package com.dodam.product.service;

import com.dodam.product.dto.request.ProductRequestDto;
import com.dodam.product.dto.response.ProductResponseDto;
import com.dodam.product.dto.statistics.ProductStatisticsDto;
import com.dodam.product.entity.Category;
import com.dodam.product.entity.Product;
import com.dodam.product.entity.Product.ProductStatus;
import com.dodam.product.exception.*;
import com.dodam.product.repository.CategoryRepository;
import com.dodam.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 서비스 클래스
 * 상품 관련 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 재고 부족 임계값
    private static final int LOW_STOCK_THRESHOLD = 10;

    // ========================== 생성 메소드 ==========================

    /**
     * 새로운 상품을 생성합니다.
     * @param requestDto 상품 생성 요청 DTO
     * @return 생성된 상품 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        log.info("상품 생성 시작: {}", requestDto.getProductName());
        
        // 유효성 검증
        validateProductRequest(requestDto);
        
        // 상품명 중복 검사
        if (productRepository.existsByProductNameIgnoreCase(requestDto.getProductName(), null)) {
            throw new DuplicateResourceException("상품", requestDto.getProductName());
        }
        
        // 카테고리 조회
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("카테고리", requestDto.getCategoryId()));
        
        // Entity 생성 및 저장
        Product product = Product.builder()
                .productName(requestDto.getProductName())
                .imageName(requestDto.getImageName())
                .price(requestDto.getPrice())
                .description(requestDto.getDescription())
                .stockQuantity(requestDto.getStockQuantity())
                .status(ProductStatus.ACTIVE)
                .category(category)
                .build();
        
        // 재고가 0인 경우 품절 상태로 설정
        if (product.getStockQuantity() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }
        
        Product savedProduct = productRepository.save(product);
        
        log.info("상품 생성 완료: ID={}, 이름={}", savedProduct.getProductId(), savedProduct.getProductName());
        return convertToResponseDto(savedProduct);
    }

    // ========================== 조회 메소드 ==========================

    /**
     * ID로 상품을 조회합니다.
     * @param productId 상품 ID
     * @return 상품 응답 DTO
     */
    @Cacheable(value = "products", key = "#productId")
    public ProductResponseDto getProductById(Long productId) {
        log.debug("상품 조회: ID={}", productId);
        
        Product product = productRepository.findByIdWithCategory(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", productId));
        
        return convertToResponseDto(product);
    }

    /**
     * 상품명으로 상품을 조회합니다.
     * @param productName 상품명
     * @return 상품 응답 DTO
     */
    public ProductResponseDto getProductByName(String productName) {
        log.debug("상품명으로 조회: {}", productName);
        
        Product product = productRepository.findByProductName(productName)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", productName));
        
        return convertToResponseDto(product);
    }

    /**
     * 모든 활성 상품을 조회합니다.
     * @param pageable 페이징 정보
     * @return 상품 응답 DTO 페이지
     */
    public Page<ProductResponseDto> getProducts(Pageable pageable) {
        log.debug("상품 페이징 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        return productRepository.findByDeletedAtIsNull(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 특정 상태의 상품을 조회합니다.
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 상품 응답 DTO 페이지
     */
    public Page<ProductResponseDto> getProductsByStatus(ProductStatus status, Pageable pageable) {
        log.debug("상태별 상품 조회: status={}", status);
        
        return productRepository.findByStatusAndDeletedAtIsNull(status, pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 카테고리별 상품을 조회합니다.
     * @param categoryId 카테고리 ID
     * @param pageable 페이징 정보
     * @return 상품 응답 DTO 페이지
     */
    public Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("카테고리별 상품 조회: categoryId={}", categoryId);
        
        // 카테고리 존재 확인
        if (!categoryRepository.findById(categoryId).filter(c -> !c.isDeleted()).isPresent()) {
            throw new ResourceNotFoundException("카테고리", categoryId);
        }
        
        return productRepository.findByCategoryCategoryIdAndDeletedAtIsNull(categoryId, pageable)
                .map(this::convertToResponseDto);
    }

    // ========================== 검색 메소드 ==========================

    /**
     * 키워드로 상품을 검색합니다.
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 상품 응답 DTO 페이지
     */
    public Page<ProductResponseDto> searchProducts(String keyword, Pageable pageable) {
        log.debug("상품 검색: keyword={}", keyword);
        
        if (!StringUtils.hasText(keyword)) {
            return getProducts(pageable);
        }
        
        return productRepository.searchProducts(keyword.trim(), pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 가격 범위로 상품을 검색합니다.
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param pageable 페이징 정보
     * @return 가격 범위 내 상품 응답 DTO 페이지
     */
    public Page<ProductResponseDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        log.debug("가격 범위별 상품 조회: {}~{}", minPrice, maxPrice);
        
        validatePriceRange(minPrice, maxPrice);
        
        return productRepository.findByPriceBetweenAndDeletedAtIsNull(minPrice, maxPrice, pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 고급 검색을 수행합니다.
     * @param categoryId 카테고리 ID (선택)
     * @param minPrice 최소 가격 (선택)
     * @param maxPrice 최대 가격 (선택)
     * @param status 상품 상태 (선택)
     * @param minStock 최소 재고 (선택)
     * @param pageable 페이징 정보
     * @return 검색된 상품 응답 DTO 페이지
     */
    public Page<ProductResponseDto> advancedSearch(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
                                                   ProductStatus status, Integer minStock, Pageable pageable) {
        log.debug("고급 검색: categoryId={}, price={}~{}, status={}, minStock={}", 
                  categoryId, minPrice, maxPrice, status, minStock);
        
        if (minPrice != null && maxPrice != null) {
            validatePriceRange(minPrice, maxPrice);
        }
        
        return productRepository.advancedSearch(categoryId, minPrice, maxPrice, status, minStock, pageable)
                .map(this::convertToResponseDto);
    }

    // ========================== 인기/추천 상품 메소드 ==========================

    /**
     * 최신 상품을 조회합니다.
     * @param pageable 페이징 정보
     * @return 최신 상품 응답 DTO 목록
     */
    public Slice<ProductResponseDto> getLatestProducts(Pageable pageable) {
        log.debug("최신 상품 조회");
        
        return productRepository.findLatestProducts(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 인기 상품을 조회합니다 (리뷰 수 기준).
     * @param pageable 페이징 정보
     * @return 인기 상품 응답 DTO 목록
     */
    public Slice<ProductResponseDto> getPopularProducts(Pageable pageable) {
        log.debug("인기 상품 조회");
        
        return productRepository.findPopularProductsByReviewCount(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 평점이 높은 상품을 조회합니다.
     * @param minRating 최소 평점
     * @param minReviewCount 최소 리뷰 수
     * @param pageable 페이징 정보
     * @return 고평점 상품 응답 DTO 목록
     */
    public Slice<ProductResponseDto> getHighRatedProducts(Double minRating, Long minReviewCount, Pageable pageable) {
        log.debug("고평점 상품 조회: minRating={}, minReviewCount={}", minRating, minReviewCount);
        
        return productRepository.findHighRatedProducts(minRating, minReviewCount, pageable)
                .map(this::convertToResponseDto);
    }

    // ========================== 재고 관리 메소드 ==========================

    /**
     * 재고를 증가시킵니다.
     * @param productId 상품 ID
     * @param quantity 증가할 수량
     * @return 업데이트된 상품 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponseDto increaseStock(Long productId, int quantity) {
        log.info("재고 증가: productId={}, quantity={}", productId, quantity);
        
        if (quantity <= 0) {
            throw new ValidationException("수량은 0보다 커야 합니다.");
        }
        
        Product product = productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", productId));
        
        product.increaseStock(quantity);
        Product updatedProduct = productRepository.save(product);
        
        log.info("재고 증가 완료: productId={}, 이전={}, 현재={}", 
                 productId, product.getStockQuantity() - quantity, product.getStockQuantity());
        
        return convertToResponseDto(updatedProduct);
    }

    /**
     * 재고를 감소시킵니다.
     * @param productId 상품 ID
     * @param quantity 감소할 수량
     * @return 업데이트된 상품 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponseDto decreaseStock(Long productId, int quantity) {
        log.info("재고 감소: productId={}, quantity={}", productId, quantity);
        
        if (quantity <= 0) {
            throw new ValidationException("수량은 0보다 커야 합니다.");
        }
        
        Product product = productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", productId));
        
        try {
            product.decreaseStock(quantity);
            Product updatedProduct = productRepository.save(product);
            
            log.info("재고 감소 완료: productId={}, 현재 재고={}", productId, product.getStockQuantity());
            return convertToResponseDto(updatedProduct);
            
        } catch (IllegalArgumentException e) {
            throw new InsufficientStockException(product.getProductName(), quantity, product.getStockQuantity());
        }
    }

    /**
     * 재고 부족 상품을 조회합니다.
     * @param pageable 페이징 정보
     * @return 재고 부족 상품 응답 DTO 페이지
     */
    public Page<ProductResponseDto> getLowStockProducts(Pageable pageable) {
        log.debug("재고 부족 상품 조회");
        
        return productRepository.findByStockQuantityLessThanAndDeletedAtIsNull(LOW_STOCK_THRESHOLD, pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 품절된 상품의 상태를 일괄 업데이트합니다.
     * @return 업데이트된 상품 수
     */
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public int updateOutOfStockProducts() {
        log.info("품절 상품 상태 일괄 업데이트");
        
        int updatedCount = productRepository.updateOutOfStockProducts();
        
        log.info("품절 상품 상태 업데이트 완료: {}개", updatedCount);
        return updatedCount;
    }

    // ========================== 가격 관리 메소드 ==========================

    /**
     * 상품 가격을 변경합니다.
     * @param productId 상품 ID
     * @param newPrice 새로운 가격
     * @return 업데이트된 상품 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponseDto updatePrice(Long productId, BigDecimal newPrice) {
        log.info("상품 가격 변경: productId={}, newPrice={}", productId, newPrice);
        
        validatePrice(newPrice);
        
        Product product = productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", productId));
        
        BigDecimal oldPrice = product.getPrice();
        product.setPrice(newPrice);
        Product updatedProduct = productRepository.save(product);
        
        log.info("상품 가격 변경 완료: productId={}, 이전 가격={}, 현재 가격={}", 
                 productId, oldPrice, newPrice);
        
        return convertToResponseDto(updatedProduct);
    }

    // ========================== 상태 관리 메소드 ==========================

    /**
     * 상품 상태를 변경합니다.
     * @param productId 상품 ID
     * @param status 새로운 상태
     * @return 업데이트된 상품 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponseDto updateStatus(Long productId, ProductStatus status) {
        log.info("상품 상태 변경: productId={}, status={}", productId, status);
        
        Product product = productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", productId));
        
        ProductStatus oldStatus = product.getStatus();
        product.setStatus(status);
        Product updatedProduct = productRepository.save(product);
        
        log.info("상품 상태 변경 완료: productId={}, 이전 상태={}, 현재 상태={}", 
                 productId, oldStatus, status);
        
        return convertToResponseDto(updatedProduct);
    }

    // ========================== 수정 메소드 ==========================

    /**
     * 상품 정보를 수정합니다.
     * @param productId 상품 ID
     * @param requestDto 상품 수정 요청 DTO
     * @return 수정된 상품 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponseDto updateProduct(Long productId, ProductRequestDto requestDto) {
        log.info("상품 수정 시작: ID={}", productId);
        
        // 유효성 검증
        validateProductRequest(requestDto);
        
        // 기존 상품 조회
        Product product = productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", productId));
        
        // 상품명 중복 검사 (본인 제외)
        if (productRepository.existsByProductNameIgnoreCase(requestDto.getProductName(), productId)) {
            throw new DuplicateResourceException("상품", requestDto.getProductName());
        }
        
        // 카테고리 조회
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("카테고리", requestDto.getCategoryId()));
        
        // 정보 업데이트
        product.setProductName(requestDto.getProductName());
        product.setImageName(requestDto.getImageName());
        product.setPrice(requestDto.getPrice());
        product.setDescription(requestDto.getDescription());
        product.setStockQuantity(requestDto.getStockQuantity());
        product.setCategory(category);
        
        // 재고에 따른 상태 업데이트
        if (product.getStockQuantity() == 0) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        
        Product updatedProduct = productRepository.save(product);
        
        log.info("상품 수정 완료: ID={}, 이름={}", updatedProduct.getProductId(), updatedProduct.getProductName());
        return convertToResponseDto(updatedProduct);
    }

    // ========================== 삭제 메소드 ==========================

    /**
     * 상품을 소프트 삭제합니다.
     * @param productId 상품 ID
     */
    @Transactional
    @CacheEvict(value = "products", key = "#productId")
    public void deleteProduct(Long productId) {
        log.info("상품 삭제 시작: ID={}", productId);
        
        Product product = productRepository.findById(productId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", productId));
        
        // 리뷰가 있는 상품은 삭제 불가
        if (product.getReviewCount() > 0) {
            throw new BusinessException("리뷰가 등록된 상품은 삭제할 수 없습니다.");
        }
        
        // 소프트 삭제 수행
        product.delete();
        productRepository.save(product);
        
        log.info("상품 삭제 완료: ID={}", productId);
    }

    // ========================== 통계 메소드 ==========================

    /**
     * 상품 통계 정보를 조회합니다.
     * @return 상품 통계 DTO
     */
    public ProductStatisticsDto getProductStatistics() {
        log.debug("상품 통계 조회");
        
        long totalProducts = productRepository.countActiveProducts();
        long activeProducts = productRepository.countByStatusAndDeletedAtIsNull(ProductStatus.ACTIVE);
        long outOfStockProducts = productRepository.countByStatusAndDeletedAtIsNull(ProductStatus.OUT_OF_STOCK);
        long inactiveProducts = productRepository.countByStatusAndDeletedAtIsNull(ProductStatus.INACTIVE);
        
        Object[] priceStats = productRepository.getPriceStats();
        Object[] stockStats = productRepository.getStockStats(LOW_STOCK_THRESHOLD);
        
        return ProductStatisticsDto.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .outOfStockProducts(outOfStockProducts)
                .inactiveProducts(inactiveProducts)
                .highestPrice((BigDecimal) priceStats[0])
                .lowestPrice((BigDecimal) priceStats[1])
                .averagePrice((BigDecimal) priceStats[2])
                .totalStock(((Number) stockStats[0]).longValue())
                .averageStock(((Number) stockStats[1]).doubleValue())
                .lowStockProducts(((Number) stockStats[2]).longValue())
                .build();
    }

    /**
     * 특정 기간 내 등록된 상품 수를 조회합니다.
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 등록된 상품 수
     */
    public long countProductsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("기간별 상품 등록 수 조회: {} ~ {}", startDate, endDate);
        
        validateDateRange(startDate, endDate);
        return productRepository.countProductsCreatedBetween(startDate, endDate);
    }

    // ========================== 유효성 검증 메소드 ==========================

    /**
     * 상품 요청 DTO의 유효성을 검증합니다.
     * @param requestDto 검증할 요청 DTO
     */
    private void validateProductRequest(ProductRequestDto requestDto) {
        if (requestDto == null) {
            throw new ValidationException("상품 요청 데이터가 null입니다.");
        }
        
        if (!StringUtils.hasText(requestDto.getProductName())) {
            throw new ValidationException("상품명", "상품명은 필수입니다.");
        }
        
        if (requestDto.getProductName().trim().length() > 200) {
            throw new ValidationException("상품명", "상품명은 200자를 초과할 수 없습니다.");
        }
        
        if (requestDto.getCategoryId() == null) {
            throw new ValidationException("카테고리", "카테고리는 필수입니다.");
        }
        
        validatePrice(requestDto.getPrice());
        
        if (requestDto.getStockQuantity() == null || requestDto.getStockQuantity() < 0) {
            throw new ValidationException("재고수량", "재고 수량은 0 이상이어야 합니다.");
        }
        
        if (requestDto.getDescription() != null && requestDto.getDescription().length() > 2000) {
            throw new ValidationException("설명", "설명은 2000자를 초과할 수 없습니다.");
        }
    }

    /**
     * 가격의 유효성을 검증합니다.
     * @param price 검증할 가격
     */
    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new ValidationException("가격", "가격은 필수입니다.");
        }
        
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("가격", "가격은 0보다 커야 합니다.");
        }
        
        if (price.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new ValidationException("가격", "가격이 너무 큽니다.");
        }
    }

    /**
     * 가격 범위의 유효성을 검증합니다.
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     */
    private void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice == null || maxPrice == null) {
            throw new ValidationException("최소 가격과 최대 가격은 필수입니다.");
        }
        
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new ValidationException("최소 가격이 최대 가격보다 클 수 없습니다.");
        }
    }

    /**
     * 날짜 범위의 유효성을 검증합니다.
     * @param startDate 시작일
     * @param endDate 종료일
     */
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("시작일과 종료일은 필수입니다.");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("시작일이 종료일보다 늦을 수 없습니다.");
        }
    }

    // ========================== DTO 변환 메소드 ==========================

    /**
     * Product Entity를 ProductResponseDto로 변환합니다.
     * @param product 변환할 Product Entity
     * @return ProductResponseDto
     */
    private ProductResponseDto convertToResponseDto(Product product) {
        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .imageName(product.getImageName())
                .price(product.getPrice())
                .description(product.getDescription())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .categoryId(product.getCategory().getCategoryId())
                .categoryName(product.getCategory().getCategoryName())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    // ========================== 기타 유틸리티 메소드 ==========================

    /**
     * 상품 존재 여부를 확인합니다.
     * @param productId 상품 ID
     * @return 존재 여부
     */
    public boolean existsById(Long productId) {
        return productRepository.findById(productId)
                .map(product -> !product.isDeleted())
                .orElse(false);
    }

    /**
     * 상품명 존재 여부를 확인합니다.
     * @param productName 상품명
     * @return 존재 여부
     */
    public boolean existsByName(String productName) {
        return productRepository.existsByProductNameIgnoreCase(productName, null);
    }
}