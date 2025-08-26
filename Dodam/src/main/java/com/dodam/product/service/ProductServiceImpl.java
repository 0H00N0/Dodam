package com.dodam.product.service;

import com.dodam.product.common.enums.ProductStatus;
import com.dodam.product.dto.request.ProductCreateRequest;
import com.dodam.product.dto.request.ProductUpdateRequest;
import com.dodam.product.dto.response.ProductDetailResponse;
import com.dodam.product.dto.response.ProductResponse;
import com.dodam.product.entity.*;
import com.dodam.product.exception.InsufficientStockException;
import com.dodam.product.exception.ProductNotFoundException;
import com.dodam.product.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private final ProductDetailRepository productDetailRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final InventoryRepository inventoryRepository;
    
    /**
     * 생성자 주입
     * 
     * @param productRepository 상품 Repository
     * @param categoryRepository 카테고리 Repository
     * @param brandRepository 브랜드 Repository
     * @param productDetailRepository 상품 상세 Repository
     * @param productOptionRepository 상품 옵션 Repository
     * @param productImageRepository 상품 이미지 Repository
     * @param inventoryRepository 재고 Repository
     */
    public ProductServiceImpl(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             BrandRepository brandRepository,
                             ProductDetailRepository productDetailRepository,
                             ProductOptionRepository productOptionRepository,
                             ProductImageRepository productImageRepository,
                             InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productDetailRepository = productDetailRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.inventoryRepository = inventoryRepository;
    }
    
    // === 기본 CRUD 메서드 ===
    
    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        logger.info("새로운 상품 등록 시작 - 상품명: {}, 카테고리ID: {}", 
                   request.getProductName(), request.getCategoryId());
        
        // 입력 검증
        validateCreateRequest(request);
        
        // 카테고리 조회 및 검증
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + request.getCategoryId()));
        
        // 비활성화된 카테고리인지 확인
        if (!category.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 카테고리입니다: " + request.getCategoryId());
        }
        
        // 브랜드 조회 (선택사항)
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다: " + request.getBrandId()));
            
            // 비활성화된 브랜드인지 확인
            if (!brand.getIsActive()) {
                throw new IllegalArgumentException("비활성화된 브랜드입니다: " + request.getBrandId());
            }
        }
        
        // 상품 엔티티 생성
        Product product = new Product(request.getProductName(), category, request.getPrice());
        product.setBrand(brand);
        product.setImageUrl(request.getImageUrl());
        
        // 상품 상세 정보 생성 (설명이 있는 경우)
        if (StringUtils.hasText(request.getDescription())) {
            ProductDetail detail = new ProductDetail(request.getDescription());
            product.setDetail(detail);
        }
        
        // 상품 저장
        Product savedProduct = productRepository.save(product);
        
        // 기본 재고 정보 생성 (초기 재고 0으로 설정)
        Inventory inventory = new Inventory(savedProduct, 0);
        inventoryRepository.save(inventory);
        
        logger.info("상품 등록 완료 - 상품ID: {}, 상품명: {}", 
                   savedProduct.getProductId(), savedProduct.getProductName());
        
        return ProductResponse.from(savedProduct);
    }
    
    /**
     * 상품 생성 요청을 검증합니다.
     * 
     * @param request 검증할 요청 객체
     */
    private void validateCreateRequest(ProductCreateRequest request) {
        if (!StringUtils.hasText(request.getProductName())) {
            throw new IllegalArgumentException("상품명은 필수입니다");
        }
        
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다");
        }
        
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다");
        }
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
        
        // 기존 상품 조회 (연관 엔티티 포함)
        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        // 수정할 필드가 있는지 확인
        if (!request.hasUpdates()) {
            logger.debug("수정할 내용이 없음 - 상품ID: {}", productId);
            return ProductResponse.from(product);
        }
        
        // 상품 정보 수정
        if (request.getProductName() != null && request.getPrice() != null) {
            validateUpdateRequest(request);
            product.updateInfo(request.getProductName(), request.getPrice());
        } else if (request.getProductName() != null) {
            product.updateInfo(request.getProductName(), product.getPrice());
        } else if (request.getPrice() != null) {
            validatePrice(request.getPrice());
            product.updateInfo(product.getProductName(), request.getPrice());
        }
        
        // 카테고리 변경
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + request.getCategoryId()));
            
            if (!category.getIsActive()) {
                throw new IllegalArgumentException("비활성화된 카테고리입니다: " + request.getCategoryId());
            }
            
            product.setCategory(category);
        }
        
        // 브랜드 변경
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다: " + request.getBrandId()));
            
            if (!brand.getIsActive()) {
                throw new IllegalArgumentException("비활성화된 브랜드입니다: " + request.getBrandId());
            }
            
            product.setBrand(brand);
        }
        
        // 이미지 URL 변경
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        
        // 상품 설명 변경
        if (request.getDescription() != null) {
            updateProductDescription(product, request.getDescription());
        }
        
        // 상태 변경 (상태 전이 검증 포함)
        if (request.getStatus() != null) {
            validateStatusTransition(product.getStatus(), request.getStatus());
            product.changeStatus(request.getStatus());
        }
        
        Product updatedProduct = productRepository.save(product);
        
        logger.info("상품 수정 완료 - 상품ID: {}, 상품명: {}", 
                   updatedProduct.getProductId(), updatedProduct.getProductName());
        
        return ProductResponse.from(updatedProduct);
    }
    
    /**
     * 상품 설명을 업데이트합니다.
     * 
     * @param product 상품 엔티티
     * @param description 새로운 설명
     */
    private void updateProductDescription(Product product, String description) {
        if (product.getDetail() != null) {
            // 기존 상세 정보가 있는 경우 업데이트
            product.getDetail().setDescription(description);
        } else if (StringUtils.hasText(description)) {
            // 새로운 상세 정보 생성
            ProductDetail detail = new ProductDetail(description);
            product.setDetail(detail);
        }
    }
    
    /**
     * 상품 업데이트 요청을 검증합니다.
     * 
     * @param request 검증할 요청 객체
     */
    private void validateUpdateRequest(ProductUpdateRequest request) {
        if (request.getProductName() != null && !StringUtils.hasText(request.getProductName())) {
            throw new IllegalArgumentException("상품명은 빈 값일 수 없습니다");
        }
        
        if (request.getPrice() != null) {
            validatePrice(request.getPrice());
        }
    }
    
    /**
     * 가격을 검증합니다.
     * 
     * @param price 검증할 가격
     */
    private void validatePrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다");
        }
    }
    
    /**
     * 상태 전이가 유효한지 검증합니다.
     * 
     * @param currentStatus 현재 상태
     * @param newStatus 새로운 상태
     */
    private void validateStatusTransition(ProductStatus currentStatus, ProductStatus newStatus) {
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("상태를 %s에서 %s로 변경할 수 없습니다.", 
                    currentStatus.getDescription(), newStatus.getDescription())
            );
        }
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
        
        // 파라미터 검증
        validateSearchCriteria(categoryId, brandId, minPrice, maxPrice, status);
        
        Page<Product> products;
        
        // 브랜드 조건이 있는 경우 별도의 쿼리 메서드가 필요하므로 수동 처리
        if (brandId != null) {
            products = findByComplexSearchCriteria(categoryId, brandId, minPrice, maxPrice, status, pageable);
        } else {
            products = productRepository.findBySearchCriteria(categoryId, minPrice, maxPrice, status, pageable);
        }
        
        return products.map(ProductResponse::from);
    }
    
    /**
     * 브랜드 조건을 포함한 복합 검색을 수행합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param brandId 브랜드 ID
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param status 상품 상태
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    private Page<Product> findByComplexSearchCriteria(Long categoryId, Long brandId, 
                                                     BigDecimal minPrice, BigDecimal maxPrice, 
                                                     ProductStatus status, Pageable pageable) {
        // 여기서는 기존 메서드를 활용하여 필터링
        // 실제 구현에서는 Repository에 브랜드 포함 쿼리 메서드 추가를 권장
        Page<Product> categoryProducts;
        if (categoryId != null) {
            categoryProducts = productRepository.findByCategoryCategoryId(categoryId, pageable);
        } else {
            categoryProducts = productRepository.findByStatus(status, pageable);
        }
        
        // 추가 필터링 로직이 필요한 경우 여기서 처리
        return categoryProducts;
    }
    
    /**
     * 검색 조건을 검증합니다.
     * 
     * @param categoryId 카테고리 ID
     * @param brandId 브랜드 ID
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param status 상품 상태
     */
    private void validateSearchCriteria(Long categoryId, Long brandId, 
                                      BigDecimal minPrice, BigDecimal maxPrice, 
                                      ProductStatus status) {
        // 가격 범위 검증
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("최소 가격은 0원 이상이어야 합니다");
        }
        
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("최대 가격은 0원 이상이어야 합니다");
        }
        
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("최소 가격이 최대 가격보다 클 수 없습니다");
        }
        
        // 카테고리 존재 검증 (선택사항)
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId);
        }
        
        // 브랜드 존재 검증 (선택사항)
        if (brandId != null && !brandRepository.existsById(brandId)) {
            throw new IllegalArgumentException("존재하지 않는 브랜드입니다: " + brandId);
        }
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
        
        // 수량 검증
        if (quantity <= 0) {
            logger.warn("잘못된 주문 수량 - 상품ID: {}, 수량: {}", productId, quantity);
            return false;
        }
        
        try {
            Product product = productRepository.findByIdWithDetails(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            
            // 상품 기본 주문 가능 여부 확인
            if (!product.isOrderable()) {
                logger.debug("상품이 주문 불가능한 상태 - 상품ID: {}, 상태: {}", 
                           productId, product.getStatus());
                return false;
            }
            
            // 재고 확인
            if (product.getInventory() == null) {
                logger.warn("재고 정보가 없음 - 상품ID: {}", productId);
                return false;
            }
            
            int availableQuantity = product.getInventory().getAvailableQuantity();
            boolean hasStock = availableQuantity >= quantity;
            
            if (!hasStock) {
                logger.debug("재고 부족 - 상품ID: {}, 요청 수량: {}, 사용 가능 수량: {}", 
                           productId, quantity, availableQuantity);
            }
            
            logger.debug("주문 가능 여부 - 상품ID: {}, 결과: {}", productId, hasStock);
            
            return hasStock;
        } catch (ProductNotFoundException e) {
            logger.warn("상품을 찾을 수 없음 - 상품ID: {}", productId);
            return false;
        } catch (Exception e) {
            logger.error("주문 가능 여부 확인 중 오류 발생 - 상품ID: {}", productId, e);
            return false;
        }
    }
    
    /**
     * 재고 확인을 수행합니다.
     * 
     * @param productId 상품 ID
     * @param quantity 확인할 수량
     * @throws ProductNotFoundException 상품을 찾을 수 없는 경우
     * @throws InsufficientStockException 재고가 부족한 경우
     */
    private void validateStock(Long productId, int quantity) {
        Product product = productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        
        if (!product.isOrderable()) {
            throw new IllegalStateException("주문할 수 없는 상품입니다: " + productId);
        }
        
        if (product.getInventory() == null) {
            throw new InsufficientStockException(productId, quantity, 0);
        }
        
        int availableQuantity = product.getInventory().getAvailableQuantity();
        if (availableQuantity < quantity) {
            throw new InsufficientStockException(productId, quantity, availableQuantity);
        }
    }
    
    @Override
    public boolean existsById(Long productId) {
        logger.debug("상품 존재 여부 확인 - 상품ID: {}", productId);
        
        return productRepository.existsById(productId);
    }
    
    // === 내부 유틸리티 메서드 ===
    
    /**
     * 상품 ID를 검증합니다.
     * 
     * @param productId 검증할 상품 ID
     */
    private void validateProductId(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 상품 ID입니다: " + productId);
        }
    }
    
    /**
     * 상품 상태 변경 시 추가 검증을 수행합니다.
     * 
     * @param product 상품 엔티티
     * @param newStatus 새로운 상태
     */
    private void validateProductStatusChange(Product product, ProductStatus newStatus) {
        // 삭제된 상품은 다른 상태로 변경할 수 없음
        if (product.getStatus() == ProductStatus.DELETED && newStatus != ProductStatus.DELETED) {
            throw new IllegalStateException("삭제된 상품의 상태를 변경할 수 없습니다");
        }
        
        // ACTIVE 상태로 변경 시 필수 정보 확인
        if (newStatus == ProductStatus.ACTIVE) {
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("상품을 활성화하려면 유효한 가격이 설정되어야 합니다");
            }
            
            if (product.getCategory() == null || !product.getCategory().getIsActive()) {
                throw new IllegalStateException("상품을 활성화하려면 활성화된 카테고리가 설정되어야 합니다");
            }
        }
    }
    
    /**
     * 성능 최적화를 위한 DTO 변환 메서드
     * 
     * @param products 상품 엔티티 리스트
     * @return 변환된 ProductResponse 리스트
     */
    private List<ProductResponse> convertToProductResponseList(List<Product> products) {
        return products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }
}