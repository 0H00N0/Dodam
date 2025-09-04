package com.dodam.product.dto.response;

import com.dodam.product.entity.Product;
import com.dodam.product.entity.Product.ProductStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 응답 DTO
 * 상품 조회 결과를 클라이언트에 전달할 때 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    /**
     * 상품 고유 번호
     */
    private Long productId;

    /**
     * 상품명
     */
    private String productName;

    /**
     * 상품 이미지 파일명
     */
    private String imageName;

    /**
     * 상품 이미지 URL (전체 경로)
     */
    private String imageUrl;

    /**
     * 상품 가격
     */
    private BigDecimal price;

    /**
     * 가격 텍스트 (포맷팅된 문자열)
     */
    private String priceText;

    /**
     * 상품 설명
     */
    private String description;

    /**
     * 재고 수량
     */
    private Integer stockQuantity;

    /**
     * 판매 상태
     */
    private ProductStatus status;

    /**
     * 판매 상태 텍스트 (한글 설명)
     */
    private String statusText;

    /**
     * 구매 가능 여부
     */
    private Boolean available;

    /**
     * 카테고리 ID
     */
    private Long categoryId;

    /**
     * 카테고리 이름
     */
    private String categoryName;

    /**
     * 카테고리 설명
     */
    private String categoryDescription;

    /**
     * 평균 평점 (소수점 1자리)
     */
    private Double averageRating;

    /**
     * 평점 텍스트 (별점 표시용)
     */
    private String ratingText;

    /**
     * 리뷰 개수
     */
    private Integer reviewCount;

    /**
     * 좋아요 받은 리뷰 개수
     */
    private Integer likedReviewCount;

    /**
     * 재고 상태 텍스트
     */
    private String stockStatusText;

    /**
     * 품절 여부
     */
    private Boolean outOfStock;

    /**
     * 할인율 (선택 사항)
     */
    private Integer discountRate;

    /**
     * 할인가 (선택 사항)
     */
    private BigDecimal discountPrice;

    /**
     * 상품 생성일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 상품 수정일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    /**
     * 상품 삭제 여부
     */
    private Boolean isDeleted;

    /**
     * 신규 상품 여부 (7일 이내 등록)
     */
    private Boolean isNew;

    /**
     * 인기 상품 여부 (높은 평점과 리뷰 수 기준)
     */
    private Boolean isPopular;

    /**
     * 추천 상품 여부
     */
    private Boolean isRecommended;

    /**
     * Entity를 ResponseDto로 변환하는 메소드 (기본 정보)
     * 
     * @param product 상품 엔티티
     * @return ProductResponseDto 객체
     */
    public static ProductResponseDto fromEntity(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .imageName(product.getImageName())
                .imageUrl(buildImageUrl(product.getImageName()))
                .price(product.getPrice())
                .priceText(formatPrice(product.getPrice()))
                .description(product.getDescription())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .statusText(product.getStatus() != null ? product.getStatus().getDescription() : null)
                .available(product.getStatus() != null && product.getStatus().isAvailable())
                .categoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .averageRating(product.getAverageRating())
                .ratingText(formatRating(product.getAverageRating()))
                .reviewCount(product.getReviewCount())
                .stockStatusText(getStockStatusText(product.getStockQuantity()))
                .outOfStock(product.getStatus() == ProductStatus.OUT_OF_STOCK || product.getStockQuantity() <= 0)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .isDeleted(product.isDeleted())
                .isNew(isNewProduct(product.getCreatedAt()))
                .isPopular(isPopularProduct(product.getAverageRating(), product.getReviewCount()))
                .build();
    }

    /**
     * Entity를 ResponseDto로 변환하는 메소드 (상세 정보 포함)
     * 
     * @param product 상품 엔티티
     * @param includeDetails 상세 정보 포함 여부
     * @return ProductResponseDto 객체
     */
    public static ProductResponseDto fromEntity(Product product, boolean includeDetails) {
        ProductResponseDto dto = fromEntity(product);
        
        if (dto != null && includeDetails) {
            // 카테고리 상세 정보
            if (product.getCategory() != null) {
                dto.setCategoryDescription(product.getCategory().getDescription());
            }
            
            // 리뷰 통계 정보
            if (product.getReviews() != null) {
                int likedReviews = product.getReviews().stream()
                        .mapToInt(review -> review.getLikeCount())
                        .sum();
                dto.setLikedReviewCount(likedReviews);
            }
        }
        
        return dto;
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드
     * 
     * @param products 상품 엔티티 목록
     * @return ProductResponseDto 목록
     */
    public static List<ProductResponseDto> fromEntityList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        return products.stream()
                .map(ProductResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드 (상세 정보 포함)
     * 
     * @param products 상품 엔티티 목록
     * @param includeDetails 상세 정보 포함 여부
     * @return ProductResponseDto 목록
     */
    public static List<ProductResponseDto> fromEntityList(List<Product> products, boolean includeDetails) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        return products.stream()
                .map(product -> fromEntity(product, includeDetails))
                .collect(Collectors.toList());
    }

    /**
     * 목록 페이지용 간소화된 정보 포함 ResponseDto 생성
     * 
     * @param product 상품 엔티티
     * @return 목록용 ProductResponseDto 객체
     */
    public static ProductResponseDto forList(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .imageName(product.getImageName())
                .imageUrl(buildImageUrl(product.getImageName()))
                .price(product.getPrice())
                .priceText(formatPrice(product.getPrice()))
                .description(truncateDescription(product.getDescription(), 100))
                .status(product.getStatus())
                .statusText(product.getStatus() != null ? product.getStatus().getDescription() : null)
                .available(product.getStatus() != null && product.getStatus().isAvailable())
                .categoryName(product.getCategory() != null ? product.getCategory().getCategoryName() : null)
                .averageRating(product.getAverageRating())
                .ratingText(formatRating(product.getAverageRating()))
                .reviewCount(product.getReviewCount())
                .outOfStock(product.getStatus() == ProductStatus.OUT_OF_STOCK || product.getStockQuantity() <= 0)
                .isNew(isNewProduct(product.getCreatedAt()))
                .isPopular(isPopularProduct(product.getAverageRating(), product.getReviewCount()))
                .build();
    }

    /**
     * 카드 표시용 간단한 ResponseDto 생성
     * 
     * @param product 상품 엔티티
     * @return 카드용 ProductResponseDto 객체
     */
    public static ProductResponseDto forCard(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .imageName(product.getImageName())
                .imageUrl(buildImageUrl(product.getImageName()))
                .price(product.getPrice())
                .priceText(formatPrice(product.getPrice()))
                .available(product.getStatus() != null && product.getStatus().isAvailable())
                .averageRating(product.getAverageRating())
                .ratingText(formatRating(product.getAverageRating()))
                .reviewCount(product.getReviewCount())
                .outOfStock(product.getStatus() == ProductStatus.OUT_OF_STOCK || product.getStockQuantity() <= 0)
                .isNew(isNewProduct(product.getCreatedAt()))
                .isPopular(isPopularProduct(product.getAverageRating(), product.getReviewCount()))
                .build();
    }

    /**
     * 관리자용 상세 정보 포함 ResponseDto 생성
     * 
     * @param product 상품 엔티티
     * @return 관리자용 ProductResponseDto 객체
     */
    public static ProductResponseDto forAdmin(Product product) {
        ProductResponseDto dto = fromEntity(product, true);
        
        if (dto != null) {
            // 관리자용 추가 정보
            dto.setStockStatusText(getDetailedStockStatus(product.getStockQuantity(), product.getStatus()));
            
            if (product.isDeleted()) {
                dto.setStatusText(String.format("삭제됨 (%s)", 
                    product.getDeletedAt().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            }
        }
        
        return dto;
    }

    /**
     * 가격을 포맷팅된 문자열로 변환
     * 
     * @param price 가격
     * @return 포맷팅된 가격 문자열
     */
    private static String formatPrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        return String.format("%,d원", price.intValue());
    }

    /**
     * 평점을 별점 텍스트로 변환
     * 
     * @param rating 평점
     * @return 별점 텍스트
     */
    private static String formatRating(double rating) {
        if (rating <= 0) {
            return "평점 없음";
        }
        return String.format("★%.1f", rating);
    }

    /**
     * 이미지 URL 생성
     * 
     * @param imageName 이미지 파일명
     * @return 이미지 URL
     */
    private static String buildImageUrl(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return "/images/products/default.jpg"; // 기본 이미지
        }
        return String.format("/images/products/%s", imageName);
    }

    /**
     * 재고 상태 텍스트 생성
     * 
     * @param stockQuantity 재고 수량
     * @return 재고 상태 텍스트
     */
    private static String getStockStatusText(Integer stockQuantity) {
        if (stockQuantity == null || stockQuantity <= 0) {
            return "품절";
        } else if (stockQuantity <= 5) {
            return "재고 부족";
        } else if (stockQuantity <= 10) {
            return "재고 여유";
        } else {
            return "재고 충분";
        }
    }

    /**
     * 관리자용 상세 재고 상태 텍스트 생성
     * 
     * @param stockQuantity 재고 수량
     * @param status 상품 상태
     * @return 상세 재고 상태 텍스트
     */
    private static String getDetailedStockStatus(Integer stockQuantity, ProductStatus status) {
        if (stockQuantity == null || stockQuantity <= 0) {
            return "품절 (재고: 0개)";
        }
        
        String baseStatus = getStockStatusText(stockQuantity);
        String statusInfo = status != null ? 
            String.format(" | 상태: %s", status.getDescription()) : "";
        
        return String.format("%s (재고: %d개)%s", baseStatus, stockQuantity, statusInfo);
    }

    /**
     * 설명 텍스트 자르기
     * 
     * @param description 원본 설명
     * @param maxLength 최대 길이
     * @return 자른 설명
     */
    private static String truncateDescription(String description, int maxLength) {
        if (description == null || description.length() <= maxLength) {
            return description;
        }
        return description.substring(0, maxLength) + "...";
    }

    /**
     * 신규 상품 여부 확인 (7일 이내 등록)
     * 
     * @param createdAt 생성일시
     * @return 신규 상품 여부
     */
    private static boolean isNewProduct(LocalDateTime createdAt) {
        if (createdAt == null) {
            return false;
        }
        return createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    /**
     * 인기 상품 여부 확인 (평점 4.0 이상, 리뷰 10개 이상)
     * 
     * @param averageRating 평균 평점
     * @param reviewCount 리뷰 개수
     * @return 인기 상품 여부
     */
    private static boolean isPopularProduct(double averageRating, int reviewCount) {
        return averageRating >= 4.0 && reviewCount >= 10;
    }

    /**
     * 할인 정보 설정
     * 
     * @param discountRate 할인율
     * @param discountPrice 할인가
     */
    public void setDiscountInfo(Integer discountRate, BigDecimal discountPrice) {
        this.discountRate = discountRate;
        this.discountPrice = discountPrice;
    }

    /**
     * 추천 상품 여부 설정
     * 
     * @param recommended 추천 여부
     */
    public void setRecommended(boolean recommended) {
        this.isRecommended = recommended;
    }

    /**
     * 할인 중인 상품인지 확인
     * 
     * @return 할인 여부
     */
    public boolean isOnSale() {
        return discountRate != null && discountRate > 0 && discountPrice != null;
    }

    /**
     * 구매 가능한 상품인지 확인
     * 
     * @return 구매 가능 여부
     */
    public boolean isPurchasable() {
        return Boolean.TRUE.equals(available) && !Boolean.TRUE.equals(outOfStock);
    }

    /**
     * 상품 요약 정보 텍스트 생성
     * 
     * @return 요약 정보 텍스트
     */
    public String getSummaryText() {
        StringBuilder summary = new StringBuilder();
        
        if (priceText != null) {
            summary.append(priceText);
        }
        
        if (ratingText != null && reviewCount != null && reviewCount > 0) {
            summary.append(String.format(" | %s (%d개 리뷰)", ratingText, reviewCount));
        }
        
        if (Boolean.TRUE.equals(isNew)) {
            summary.append(" | 신상품");
        }
        
        if (Boolean.TRUE.equals(isPopular)) {
            summary.append(" | 인기상품");
        }
        
        if (Boolean.TRUE.equals(outOfStock)) {
            summary.append(" | 품절");
        } else if (stockStatusText != null && stockStatusText.contains("부족")) {
            summary.append(" | 재고부족");
        }
        
        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("ProductResponseDto(id=%d, name=%s, price=%s, status=%s)", 
                           productId, productName, priceText, statusText);
    }
}