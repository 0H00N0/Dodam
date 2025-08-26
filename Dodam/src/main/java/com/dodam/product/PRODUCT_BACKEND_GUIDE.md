# 📦 상품 도메인 백엔드 개발 가이드

## 📌 프로젝트 개요
- **도메인**: 상품 관리 시스템 (Product Management System)
- **기술 스택**: Spring Boot 3.x, Spring Data JPA, H2 Database
- **담당 테이블**: PRODUCT, CATEGORY, BRAND, PRODUCT_OPTION, PRODUCT_DETAIL, PRODUCT_IMAGE, INVENTORY

## 🏗️ 패키지 구조
```
com.dodam.product/
├── controller/
│   ├── ProductController.java
│   ├── CategoryController.java
│   └── BrandController.java
├── service/
│   ├── ProductService.java
│   ├── ProductOptionService.java
│   └── InventoryService.java
├── repository/
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   ├── BrandRepository.java
│   ├── ProductOptionRepository.java
│   └── InventoryRepository.java
├── entity/
│   ├── Product.java
│   ├── Category.java
│   ├── Brand.java
│   ├── ProductOption.java
│   ├── ProductDetail.java
│   ├── ProductImage.java
│   └── Inventory.java
├── dto/
│   ├── request/
│   │   ├── ProductCreateRequest.java
│   │   ├── ProductUpdateRequest.java
│   │   └── ProductSearchRequest.java
│   └── response/
│       ├── ProductResponse.java
│       ├── ProductDetailResponse.java
│       └── ProductListResponse.java
├── exception/
│   ├── ProductNotFoundException.java
│   ├── InsufficientStockException.java
│   └── DuplicateSkuException.java
└── config/
    └── JpaConfig.java
```

## 📊 ERD 기반 엔티티 설계

### 1. Product (핵심 엔티티)
```java
@Entity
@Table(name = "PRODUCT")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    
    @Column(nullable = false, length = 100)
    private String productName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(length = 500)
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductStatus status; // DRAFT, ACTIVE, INACTIVE, DELETED
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @OrderBy("displayOrder ASC")
    private List<ProductOption> options = new ArrayList<>();
    
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProductDetail detail;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @OrderBy("imageOrder ASC")
    private List<ProductImage> images = new ArrayList<>();
    
    @OneToOne(mappedBy = "product")
    private Inventory inventory;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // 연관관계 편의 메서드
    // 비즈니스 메서드
}
```

### 2. Category (카테고리)
```java
@Entity
@Table(name = "CATEGORY")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;
    
    @Column(nullable = false, length = 50)
    private String categoryName;
    
    private Long parentCategoryId;
    
    @Column(length = 100)
    private String categoryPath;
    
    private Integer displayOrder;
}
```

### 3. Brand (브랜드)
```java
@Entity
@Table(name = "BRAND")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brandId;
    
    @Column(nullable = false, length = 50)
    private String brandName;
    
    @Column(length = 200)
    private String brandLogoUrl;
    
    private Boolean isActive;
}
```

### 4. ProductOption (상품 옵션)
```java
@Entity
@Table(name = "PRODUCT_OPTION")
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(nullable = false, length = 50)
    private String optionName;
    
    @Column(nullable = false, length = 100)
    private String optionValue;
    
    private BigDecimal additionalPrice;
    
    private Integer stockQuantity;
    
    private Integer displayOrder;
}
```

### 5. Inventory (재고)
```java
@Entity
@Table(name = "INVENTORY")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Integer reservedQuantity;
    
    @Column(nullable = false)
    private Integer availableQuantity;
    
    @Version
    private Long version; // 낙관적 락
    
    private LocalDateTime lastRestockedAt;
    
    // 재고 관리 비즈니스 메서드
    public void decreaseStock(int amount) {
        if (availableQuantity < amount) {
            throw new InsufficientStockException("재고가 부족합니다.");
        }
        this.availableQuantity -= amount;
        this.reservedQuantity += amount;
    }
}
```

## 🎯 핵심 비즈니스 로직

### 1. 상품 라이프사이클 관리
```java
public enum ProductStatus {
    DRAFT,      // 작성중
    ACTIVE,     // 판매중
    INACTIVE,   // 판매중지
    DELETED     // 삭제됨
}

// Service Layer
public void updateProductStatus(Long productId, ProductStatus newStatus) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException());
    
    // 상태 전이 검증 로직
    validateStatusTransition(product.getStatus(), newStatus);
    product.setStatus(newStatus);
}
```

### 2. 재고 관리 (동시성 제어)
```java
@Service
@Transactional
public class InventoryService {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public void decreaseStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
            .orElseThrow(() -> new ProductNotFoundException());
        
        inventory.decreaseStock(quantity);
    }
}
```

### 3. 가격 계산 로직
```java
public BigDecimal calculateTotalPrice(Product product, List<ProductOption> selectedOptions) {
    BigDecimal basePrice = product.getPrice();
    BigDecimal optionPrice = selectedOptions.stream()
        .map(ProductOption::getAdditionalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    return basePrice.add(optionPrice);
}
```

## 🌐 REST API 엔드포인트

### 상품 API
| Method | Endpoint | 설명 | Request Body | Response |
|--------|----------|------|--------------|----------|
| GET | `/api/products` | 상품 목록 조회 | - | Page<ProductResponse> |
| GET | `/api/products/{id}` | 상품 상세 조회 | - | ProductDetailResponse |
| POST | `/api/products` | 상품 등록 | ProductCreateRequest | ProductResponse |
| PUT | `/api/products/{id}` | 상품 수정 | ProductUpdateRequest | ProductResponse |
| DELETE | `/api/products/{id}` | 상품 삭제 (소프트) | - | void |
| GET | `/api/products/search` | 상품 검색 | ProductSearchRequest | Page<ProductResponse> |

### 옵션/재고 API
| Method | Endpoint | 설명 | Request Body | Response |
|--------|----------|------|--------------|----------|
| POST | `/api/products/{id}/options` | 옵션 추가 | ProductOptionRequest | ProductOptionResponse |
| PUT | `/api/products/{id}/inventory` | 재고 수정 | InventoryUpdateRequest | InventoryResponse |
| POST | `/api/products/{id}/images` | 이미지 업로드 | MultipartFile | ProductImageResponse |

### 카테고리/브랜드 API
| Method | Endpoint | 설명 | Request Body | Response |
|--------|----------|------|--------------|----------|
| GET | `/api/categories` | 카테고리 목록 | - | List<CategoryResponse> |
| GET | `/api/brands` | 브랜드 목록 | - | List<BrandResponse> |

## ✅ 데이터 검증 및 예외 처리

### 1. Bean Validation
```java
public class ProductCreateRequest {
    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다")
    private String productName;
    
    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0", message = "가격은 0원 이상이어야 합니다")
    private BigDecimal price;
    
    @NotNull(message = "카테고리는 필수입니다")
    private Long categoryId;
}
```

### 2. Custom Validator
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SkuValidator.class)
public @interface ValidSku {
    String message() default "올바른 SKU 형식이 아닙니다";
}

public class SkuValidator implements ConstraintValidator<ValidSku, String> {
    // SKU 형식 검증 로직
}
```

### 3. GlobalExceptionHandler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("PRODUCT_001", e.getMessage()));
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse("INVENTORY_001", e.getMessage()));
    }
}
```

## ⚡ 성능 최적화 전략

### 1. N+1 문제 해결
```java
// @EntityGraph 사용
@EntityGraph(attributePaths = {"category", "brand", "options"})
@Query("SELECT p FROM Product p WHERE p.status = :status")
Page<Product> findByStatusWithDetails(@Param("status") ProductStatus status, Pageable pageable);

// Fetch Join 사용
@Query("SELECT DISTINCT p FROM Product p " +
       "LEFT JOIN FETCH p.category " +
       "LEFT JOIN FETCH p.brand " +
       "WHERE p.productId IN :ids")
List<Product> findByIdsWithDetails(@Param("ids") List<Long> ids);
```

### 2. 캐싱 전략
```java
@Service
public class CategoryService {
    
    @Cacheable(value = "categories", key = "#root.methodName")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    @CacheEvict(value = "categories", allEntries = true)
    public void updateCategory(Category category) {
        categoryRepository.save(category);
    }
}
```

### 3. 인덱스 설정
```sql
-- 자주 조회되는 컬럼에 인덱스 추가
CREATE INDEX idx_product_name ON PRODUCT(product_name);
CREATE INDEX idx_product_category ON PRODUCT(category_id);
CREATE INDEX idx_product_status ON PRODUCT(status);
CREATE INDEX idx_product_composite ON PRODUCT(category_id, brand_id, status);
```

### 4. QueryDSL 동적 쿼리
```java
public Page<Product> searchProducts(ProductSearchRequest request, Pageable pageable) {
    QProduct product = QProduct.product;
    
    BooleanBuilder builder = new BooleanBuilder();
    
    if (request.getKeyword() != null) {
        builder.and(product.productName.contains(request.getKeyword()));
    }
    if (request.getCategoryId() != null) {
        builder.and(product.category.categoryId.eq(request.getCategoryId()));
    }
    if (request.getMinPrice() != null) {
        builder.and(product.price.goe(request.getMinPrice()));
    }
    
    return productRepository.findAll(builder, pageable);
}
```

## 🧪 테스트 전략

### 1. 단위 테스트 (Service Layer)
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductRepository productRepository;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    @DisplayName("상품 등록 성공 테스트")
    void createProduct_Success() {
        // given
        ProductCreateRequest request = createValidRequest();
        Product product = createProductFromRequest(request);
        
        when(productRepository.save(any(Product.class))).thenReturn(product);
        
        // when
        ProductResponse response = productService.createProduct(request);
        
        // then
        assertThat(response.getProductName()).isEqualTo(request.getProductName());
    }
}
```

### 2. 통합 테스트 (Repository Layer)
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductRepositoryTest {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    @DisplayName("카테고리별 상품 조회")
    void findByCategoryId() {
        // given
        Category category = createCategory();
        Product product = createProduct(category);
        productRepository.save(product);
        
        // when
        List<Product> products = productRepository.findByCategoryId(category.getCategoryId());
        
        // then
        assertThat(products).hasSize(1);
    }
}
```

### 3. API 테스트 (Controller Layer)
```java
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProductService productService;
    
    @Test
    @DisplayName("상품 목록 조회 API")
    void getProducts() throws Exception {
        // given
        List<ProductResponse> products = createProductResponses();
        when(productService.getProducts(any())).thenReturn(new PageImpl<>(products));
        
        // when & then
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].productName").exists());
    }
}
```

### 4. 동시성 테스트 (재고 관리)
```java
@SpringBootTest
@Transactional
class InventoryConcurrencyTest {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Test
    @DisplayName("동시 재고 감소 테스트")
    void decreaseStock_Concurrency() throws InterruptedException {
        // given
        Long productId = 1L;
        int threadCount = 100;
        int decreaseAmount = 1;
        
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    inventoryService.decreaseStock(productId, decreaseAmount);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        
        // then
        Inventory inventory = inventoryRepository.findById(productId).orElseThrow();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(초기재고 - threadCount);
    }
}
```

## 🔧 H2 데이터베이스 설정

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
        default_batch_fetch_size: 100
```

### 초기 데이터 (data.sql)
```sql
-- 카테고리 초기 데이터
INSERT INTO CATEGORY (category_name, parent_category_id, display_order) VALUES
('전자제품', NULL, 1),
('의류', NULL, 2),
('식품', NULL, 3);

-- 브랜드 초기 데이터
INSERT INTO BRAND (brand_name, is_active) VALUES
('삼성', true),
('LG', true),
('나이키', true);

-- 상품 초기 데이터
INSERT INTO PRODUCT (product_name, category_id, brand_id, price, status, created_at) VALUES
('갤럭시 S24', 1, 1, 1200000, 'ACTIVE', CURRENT_TIMESTAMP),
('LG 그램', 1, 2, 1500000, 'ACTIVE', CURRENT_TIMESTAMP);

-- 재고 초기 데이터
INSERT INTO INVENTORY (product_id, quantity, reserved_quantity, available_quantity) VALUES
(1, 100, 0, 100),
(2, 50, 10, 40);
```

## 📝 구현 단계별 체크리스트

### Phase 1: 기본 구조 (1주차)
- [ ] 프로젝트 초기 설정 (Spring Boot, H2)
- [ ] 기본 엔티티 생성 (Product, Category, Brand)
- [ ] Repository 인터페이스 작성
- [ ] Service 레이어 기본 CRUD 구현
- [ ] Controller REST API 구현
- [ ] 기본 예외 처리
- [ ] 단위 테스트 작성

### Phase 2: 기능 확장 (2주차)
- [ ] ProductOption 엔티티 및 기능 구현
- [ ] ProductDetail 엔티티 및 기능 구현
- [ ] ProductImage 엔티티 및 업로드 기능
- [ ] Inventory 엔티티 및 재고 관리 로직
- [ ] 동시성 제어 구현 (비관적 락)
- [ ] QueryDSL 설정 및 검색 기능
- [ ] 통합 테스트 작성

### Phase 3: 최적화 및 완성 (3-4일)
- [ ] N+1 문제 해결 (@EntityGraph, Fetch Join)
- [ ] 캐싱 전략 적용
- [ ] 인덱스 최적화
- [ ] API 문서화 (Swagger)
- [ ] 성능 테스트
- [ ] 코드 리팩토링
- [ ] 최종 검증

## 💡 개발 팁

### 1. JPA 성능 최적화
- 지연 로딩 기본 사용 (LAZY)
- 필요시 @EntityGraph나 Fetch Join 활용
- DTO Projection으로 필요한 필드만 조회
- @BatchSize로 N+1 문제 완화

### 2. 트랜잭션 관리
- Service 레이어에서 @Transactional 관리
- 읽기 전용 메서드는 @Transactional(readOnly = true)
- 긴 트랜잭션 회피, 필요시 분할

### 3. 예외 처리
- 비즈니스 예외는 커스텀 예외 클래스 생성
- GlobalExceptionHandler로 중앙화
- 에러 응답 표준화 (ErrorResponse)

### 4. 테스트
- 계층별 독립적 테스트
- 테스트 데이터는 @BeforeEach에서 준비
- 동시성 테스트 필수 (재고 관리)

## 🔍 참고 자료
- [Spring Data JPA 공식 문서](https://spring.io/projects/spring-data-jpa)
- [JPA Best Practices](https://www.baeldung.com/jpa-hibernate-best-practices)
- [H2 Database 문서](http://www.h2database.com/html/main.html)

---
*이 문서는 Dodam Pay 상품 도메인 백엔드 개발을 위한 가이드입니다.*
*작성일: 2025-08-26*