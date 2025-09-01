package com.dodam.product.dto.request;

import com.dodam.product.entity.Product;
import com.dodam.product.entity.Review;
import com.dodam.product.entity.Review.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 요청 DTO
 * 리뷰 생성, 수정, 검색 요청 시 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {

    /**
     * 회원 ID (필수값)
     * 리뷰 작성자의 회원 번호입니다.
     */
    @NotNull(message = "회원 ID는 필수입니다.")
    @Min(value = 1, message = "유효한 회원 ID를 입력해주세요.")
    private Long memberId;

    /**
     * 상품 ID (필수값)
     * 리뷰 대상 상품의 번호입니다.
     */
    @NotNull(message = "상품 ID는 필수입니다.")
    @Min(value = 1, message = "유효한 상품 ID를 입력해주세요.")
    private Long productId;

    /**
     * 리뷰 제목 (필수값)
     * 1자 이상 200자 이하로 입력해야 합니다.
     */
    @NotBlank(message = "리뷰 제목은 필수입니다.")
    @Size(min = 1, max = 200, message = "리뷰 제목은 1자 이상 200자 이하로 입력해주세요.")
    private String title;

    /**
     * 리뷰 내용 (필수값)
     * 10자 이상 2000자 이하로 입력해야 합니다.
     */
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 10, max = 2000, message = "리뷰 내용은 10자 이상 2000자 이하로 입력해주세요.")
    private String content;

    /**
     * 평점 (필수값)
     * 1점 이상 5점 이하로 입력해야 합니다.
     */
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Integer rating;

    /**
     * 리뷰 상태
     * ACTIVE, HIDDEN, REPORTED 중 하나를 입력할 수 있습니다.
     */
    private ReviewStatus status;

    /**
     * 생성일시 (수정 시에만 사용)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 수정일시 (수정 시에만 사용)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    // 검색용 필드들 (선택값)

    /**
     * 검색 키워드 (제목, 내용에서 검색)
     */
    private String keyword;

    /**
     * 최소 평점 (검색용)
     */
    @Min(value = 1, message = "최소 평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "최소 평점은 5점 이하여야 합니다.")
    private Integer minRating;

    /**
     * 최대 평점 (검색용)
     */
    @Min(value = 1, message = "최대 평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "최대 평점은 5점 이하여야 합니다.")
    private Integer maxRating;

    /**
     * 회원 ID 목록 (검색용)
     */
    private List<Long> memberIds;

    /**
     * 상품 ID 목록 (검색용)
     */
    private List<Long> productIds;

    /**
     * 리뷰 상태 목록 (검색용)
     */
    private List<ReviewStatus> statuses;

    /**
     * 검색 시작 날짜 (검색용)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startDate;

    /**
     * 검색 종료 날짜 (검색용)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endDate;

    /**
     * 좋아요 개수 최소값 (검색용)
     */
    @Min(value = 0, message = "좋아요 개수는 0 이상이어야 합니다.")
    private Integer minLikeCount;

    /**
     * 정렬 기준 (검색용)
     * createdAt, rating, likeCount 등
     */
    private String sortBy;

    /**
     * 정렬 방향 (검색용)
     * ASC, DESC
     */
    private String sortDirection;

    /**
     * 페이지 번호 (검색용, 0부터 시작)
     */
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private Integer page;

    /**
     * 페이지 크기 (검색용)
     */
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 100 이하로 설정해주세요.")
    private Integer size;

    /**
     * RequestDto를 Entity로 변환하는 메소드 (생성용)
     * 
     * @param product 상품 엔티티
     * @return Review 엔티티 객체
     */
    public Review toEntity(Product product) {
        return Review.builder()
                .memberId(this.memberId)
                .title(this.title)
                .content(this.content)
                .rating(this.rating)
                .status(this.status != null ? this.status : ReviewStatus.ACTIVE)
                .product(product)
                .build();
    }

    /**
     * RequestDto를 Entity로 변환하는 메소드 (수정용)
     * 
     * @param reviewId 리뷰 ID
     * @param product 상품 엔티티
     * @return Review 엔티티 객체
     */
    public Review toEntity(Long reviewId, Product product) {
        return Review.builder()
                .reviewId(reviewId)
                .memberId(this.memberId)
                .title(this.title)
                .content(this.content)
                .rating(this.rating)
                .status(this.status != null ? this.status : ReviewStatus.ACTIVE)
                .product(product)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 기존 Entity를 RequestDto로 업데이트하는 메소드
     * 
     * @param review 업데이트할 리뷰 엔티티
     */
    public void updateEntity(Review review) {
        if (this.title != null) {
            review.setTitle(this.title);
        }
        if (this.content != null) {
            review.setContent(this.content);
        }
        if (this.rating != null) {
            review.setRating(this.rating);
        }
        if (this.status != null) {
            review.setStatus(this.status);
        }
    }

    /**
     * 유효성 검사 통과 여부 확인
     * 
     * @return 유효성 검사 통과 여부
     */
    public boolean isValid() {
        return memberId != null && 
               memberId > 0 &&
               productId != null && 
               productId > 0 &&
               title != null && 
               !title.trim().isEmpty() && 
               title.length() <= 200 &&
               content != null && 
               content.trim().length() >= 10 && 
               content.length() <= 2000 &&
               rating != null && 
               rating >= 1 && 
               rating <= 5;
    }

    /**
     * 검색 조건이 있는지 확인
     * 
     * @return 검색 조건 존재 여부
     */
    public boolean hasSearchConditions() {
        return keyword != null || 
               minRating != null || 
               maxRating != null || 
               (memberIds != null && !memberIds.isEmpty()) ||
               (productIds != null && !productIds.isEmpty()) ||
               (statuses != null && !statuses.isEmpty()) ||
               startDate != null || 
               endDate != null ||
               minLikeCount != null;
    }

    /**
     * 평점 범위 유효성 검사
     * 
     * @return 평점 범위 유효성
     */
    public boolean isValidRatingRange() {
        if (minRating != null && maxRating != null) {
            return minRating <= maxRating;
        }
        return true;
    }

    /**
     * 날짜 범위 유효성 검사
     * 
     * @return 날짜 범위 유효성
     */
    public boolean isValidDateRange() {
        if (startDate != null && endDate != null) {
            return startDate.isBefore(endDate) || startDate.isEqual(endDate);
        }
        return true;
    }

    /**
     * 리뷰 정보 정규화 (앞뒤 공백 제거, 기본값 설정)
     * 
     * @return 정규화된 RequestDto
     */
    public ReviewRequestDto normalize() {
        if (this.title != null) {
            this.title = this.title.trim();
        }
        if (this.content != null) {
            this.content = this.content.trim();
        }
        if (this.keyword != null) {
            this.keyword = this.keyword.trim();
            if (this.keyword.isEmpty()) {
                this.keyword = null;
            }
        }
        if (this.status == null) {
            this.status = ReviewStatus.ACTIVE;
        }
        return this;
    }

    /**
     * 생성용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @param title 제목
     * @param content 내용
     * @param rating 평점
     * @return ReviewRequestDto 객체
     */
    public static ReviewRequestDto createRequest(Long memberId, Long productId, 
                                               String title, String content, Integer rating) {
        return ReviewRequestDto.builder()
                .memberId(memberId)
                .productId(productId)
                .title(title)
                .content(content)
                .rating(rating)
                .status(ReviewStatus.ACTIVE)
                .build();
    }

    /**
     * 검색용 RequestDto 생성 팩토리 메소드
     * 
     * @param productId 상품 ID
     * @param minRating 최소 평점
     * @return ReviewRequestDto 객체
     */
    public static ReviewRequestDto searchRequest(Long productId, Integer minRating) {
        return ReviewRequestDto.builder()
                .productId(productId)
                .minRating(minRating)
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
    }

    /**
     * 수정용 RequestDto 생성 팩토리 메소드
     * 
     * @param title 제목
     * @param content 내용
     * @param rating 평점
     * @return ReviewRequestDto 객체
     */
    public static ReviewRequestDto updateRequest(String title, String content, Integer rating) {
        return ReviewRequestDto.builder()
                .title(title)
                .content(content)
                .rating(rating)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Entity로부터 RequestDto 생성 (수정 시 기존 값 로드용)
     * 
     * @param review 리뷰 엔티티
     * @return ReviewRequestDto 객체
     */
    public static ReviewRequestDto fromEntity(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewRequestDto.builder()
                .memberId(review.getMemberId())
                .productId(review.getProduct() != null ? review.getProduct().getProductId() : null)
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    @Override
    public String toString() {
        return String.format("ReviewRequestDto(memberId=%d, productId=%d, title=%s, rating=%d)", 
                           memberId, productId, title, rating);
    }
}