package com.dodam.product.service;

import com.dodam.product.common.enums.ProductStatus;
import com.dodam.product.dto.request.ProductCreateRequest;
import com.dodam.product.dto.request.ProductUpdateRequest;
import com.dodam.product.dto.response.ProductDetailResponse;
import com.dodam.product.dto.response.ProductResponse;
import com.dodam.product.entity.Brand;
import com.dodam.product.entity.Category;
import com.dodam.product.entity.Inventory;
import com.dodam.product.entity.Product;
import com.dodam.product.exception.ProductNotFoundException;
import com.dodam.product.repository.BrandRepository;
import com.dodam.product.repository.CategoryRepository;
import com.dodam.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * ProductService 단위 테스트
 * 
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("상품 서비스 테스트")
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private BrandRepository brandRepository;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    private Category testCategory;
    private Brand testBrand;
    private Product testProduct;
    private Inventory testInventory;
    
    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        testCategory = createTestCategory();
        testBrand = createTestBrand();
        testProduct = createTestProduct();
        testInventory = createTestInventory();
        
        testProduct.setInventory(testInventory);
    }
    
    @Test
    @DisplayName("상품 생성 성공")
    void createProduct_Success() {
        // given
        ProductCreateRequest request = createValidProductCreateRequest();
        
        given(categoryRepository.findById(anyLong())).willReturn(Optional.of(testCategory));
        given(brandRepository.findById(anyLong())).willReturn(Optional.of(testBrand));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);
        
        // when
        ProductResponse response = productService.createProduct(request);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getProductName()).isEqualTo(testProduct.getProductName());
        assertThat(response.getPrice()).isEqualTo(testProduct.getPrice());
        assertThat(response.getCategoryId()).isEqualTo(testCategory.getCategoryId());
        assertThat(response.getBrandId()).isEqualTo(testBrand.getBrandId());
        assertThat(response.getStatus()).isEqualTo(ProductStatus.DRAFT);
        
        verify(categoryRepository).findById(request.getCategoryId());
        verify(brandRepository).findById(request.getBrandId());
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("상품 생성 실패 - 존재하지 않는 카테고리")
    void createProduct_FailWithInvalidCategory() {
        // given
        ProductCreateRequest request = createValidProductCreateRequest();
        
        given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 카테고리입니다: " + request.getCategoryId());
        
        verify(categoryRepository).findById(request.getCategoryId());
        verify(productRepository, never()).save(any(Product.class));
    }
    
    @Test
    @DisplayName("상품 ID로 조회 성공")
    void findById_Success() {
        // given
        Long productId = 1L;
        
        given(productRepository.findByIdWithDetails(productId)).willReturn(Optional.of(testProduct));
        
        // when
        ProductResponse response = productService.findById(productId);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(testProduct.getProductId());
        assertThat(response.getProductName()).isEqualTo(testProduct.getProductName());
        
        verify(productRepository).findByIdWithDetails(productId);
    }
    
    @Test
    @DisplayName("상품 ID로 조회 실패 - 존재하지 않는 상품")
    void findById_FailWithNotFound() {
        // given
        Long productId = 999L;
        
        given(productRepository.findByIdWithDetails(productId)).willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> productService.findById(productId))
                .isInstanceOf(ProductNotFoundException.class);
        
        verify(productRepository).findByIdWithDetails(productId);
    }
    
    @Test
    @DisplayName("상품 상세 조회 성공")
    void findDetailById_Success() {
        // given
        Long productId = 1L;
        
        given(productRepository.findByIdWithFullDetails(productId)).willReturn(Optional.of(testProduct));
        
        // when
        ProductDetailResponse response = productService.findDetailById(productId);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(testProduct.getProductId());
        assertThat(response.getProductName()).isEqualTo(testProduct.getProductName());
        assertThat(response.getAvailableQuantity()).isEqualTo(testInventory.getAvailableQuantity());
        
        verify(productRepository).findByIdWithFullDetails(productId);
    }
    
    @Test
    @DisplayName("전체 상품 목록 조회")
    void findAll_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, pageable, 1);
        
        given(productRepository.findAll(pageable)).willReturn(productPage);
        
        // when
        Page<ProductResponse> response = productService.findAll(pageable);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getProductName()).isEqualTo(testProduct.getProductName());
        
        verify(productRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct_Success() {
        // given
        Long productId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setProductName("수정된 상품명");
        request.setPrice(new BigDecimal("20000"));
        
        given(productRepository.findById(productId)).willReturn(Optional.of(testProduct));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);
        
        // when
        ProductResponse response = productService.updateProduct(productId, request);
        
        // then
        assertThat(response).isNotNull();
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("상품 삭제 성공 - 소프트 삭제")
    void deleteProduct_Success() {
        // given
        Long productId = 1L;
        
        given(productRepository.findById(productId)).willReturn(Optional.of(testProduct));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);
        
        // when
        productService.deleteProduct(productId);
        
        // then
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("상품 상태 변경 성공")
    void updateProductStatus_Success() {
        // given
        Long productId = 1L;
        ProductStatus newStatus = ProductStatus.ACTIVE;
        
        given(productRepository.findById(productId)).willReturn(Optional.of(testProduct));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);
        
        // when
        ProductResponse response = productService.updateProductStatus(productId, newStatus);
        
        // then
        assertThat(response).isNotNull();
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }
    
    @Test
    @DisplayName("상품 상태 일괄 변경 성공")
    void updateProductStatusBatch_Success() {
        // given
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        ProductStatus newStatus = ProductStatus.INACTIVE;
        int expectedUpdatedCount = 3;
        
        given(productRepository.updateStatusByIds(productIds, newStatus))
                .willReturn(expectedUpdatedCount);
        
        // when
        int updatedCount = productService.updateProductStatusBatch(productIds, newStatus);
        
        // then
        assertThat(updatedCount).isEqualTo(expectedUpdatedCount);
        verify(productRepository).updateStatusByIds(productIds, newStatus);
    }
    
    @Test
    @DisplayName("상품 상태별 조회")
    void findByStatus_Success() {
        // given
        ProductStatus status = ProductStatus.ACTIVE;
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, pageable, 1);
        
        given(productRepository.findByStatusWithDetails(status, pageable))
                .willReturn(productPage);
        
        // when
        Page<ProductResponse> response = productService.findByStatus(status, pageable);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findByStatusWithDetails(status, pageable);
    }
    
    @Test
    @DisplayName("키워드 검색")
    void searchByKeyword_Success() {
        // given
        String keyword = "테스트";
        ProductStatus status = ProductStatus.ACTIVE;
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, pageable, 1);
        
        given(productRepository.findByKeywordSearch(keyword, status, pageable))
                .willReturn(productPage);
        
        // when
        Page<ProductResponse> response = productService.searchByKeyword(keyword, status, pageable);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findByKeywordSearch(keyword, status, pageable);
    }
    
    @Test
    @DisplayName("총 가격 계산")
    void calculateTotalPrice_Success() {
        // given
        Long productId = 1L;
        List<Long> selectedOptionIds = Arrays.asList(1L, 2L);
        BigDecimal expectedPrice = new BigDecimal("15000");
        
        given(productRepository.findByIdWithDetails(productId)).willReturn(Optional.of(testProduct));
        
        // when
        BigDecimal totalPrice = productService.calculateTotalPrice(productId, selectedOptionIds);
        
        // then
        assertThat(totalPrice).isNotNull();
        verify(productRepository).findByIdWithDetails(productId);
    }
    
    @Test
    @DisplayName("주문 가능 여부 확인 - 가능")
    void isOrderable_True() {
        // given
        Long productId = 1L;
        int quantity = 5;
        
        testProduct.changeStatus(ProductStatus.ACTIVE);  // 활성 상태로 변경
        testInventory.setAvailableQuantity(10);  // 충분한 재고
        
        given(productRepository.findByIdWithDetails(productId)).willReturn(Optional.of(testProduct));
        
        // when
        boolean result = productService.isOrderable(productId, quantity);
        
        // then
        assertThat(result).isTrue();
        verify(productRepository).findByIdWithDetails(productId);
    }
    
    @Test
    @DisplayName("주문 가능 여부 확인 - 불가능 (재고 부족)")
    void isOrderable_FalseInsufficientStock() {
        // given
        Long productId = 1L;
        int quantity = 15;
        
        testProduct.changeStatus(ProductStatus.ACTIVE);
        testInventory.setAvailableQuantity(10);  // 부족한 재고
        
        given(productRepository.findByIdWithDetails(productId)).willReturn(Optional.of(testProduct));
        
        // when
        boolean result = productService.isOrderable(productId, quantity);
        
        // then
        assertThat(result).isFalse();
        verify(productRepository).findByIdWithDetails(productId);
    }
    
    @Test
    @DisplayName("상품 존재 여부 확인")
    void existsById_Success() {
        // given
        Long productId = 1L;
        
        given(productRepository.existsById(productId)).willReturn(true);
        
        // when
        boolean exists = productService.existsById(productId);
        
        // then
        assertThat(exists).isTrue();
        verify(productRepository).existsById(productId);
    }
    
    // === 테스트 헬퍼 메서드 ===
    
    private Category createTestCategory() {
        Category category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("전자제품");
        category.setDisplayOrder(1);
        category.setIsActive(true);
        return category;
    }
    
    private Brand createTestBrand() {
        Brand brand = new Brand();
        brand.setBrandId(1L);
        brand.setBrandName("삼성");
        brand.setIsActive(true);
        return brand;
    }
    
    private Product createTestProduct() {
        Product product = new Product("테스트 상품", testCategory, new BigDecimal("10000"));
        product.setProductId(1L);
        product.setBrand(testBrand);
        product.setImageUrl("http://example.com/image.jpg");
        return product;
    }
    
    private Inventory createTestInventory() {
        Inventory inventory = new Inventory();
        inventory.setInventoryId(1L);
        inventory.setQuantity(100);
        inventory.setReservedQuantity(10);
        inventory.setAvailableQuantity(90);
        inventory.setLastRestockedAt(LocalDateTime.now());
        return inventory;
    }
    
    private ProductCreateRequest createValidProductCreateRequest() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setProductName("새로운 상품");
        request.setCategoryId(1L);
        request.setBrandId(1L);
        request.setPrice(new BigDecimal("15000"));
        request.setImageUrl("http://example.com/new-image.jpg");
        request.setDescription("새로운 상품 설명");
        return request;
    }
}