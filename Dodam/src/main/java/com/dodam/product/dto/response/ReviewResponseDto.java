package com.dodam.product.dto.response;

import com.dodam.product.entity.Review;
import com.dodam.product.entity.Review.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 리뷰 응답 DTO
 * 리뷰 조회 결과를 클라이언트에 전달할 때 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

    /**
     * 리뷰 고유 번호
     */
    private Long reviewId;

    /**
     * 회원 ID
     */
    private Long memberId;

    /**
     * 작성자 이름 (마스킹 처리된)
     */
    private String memberName;

    /**
     * 상품 ID
     */
    private Long productId;

    /**
     * 상품명
     */
    private String productName;

    /**
     * 상품 이미지 URL
     */
    private String productImageUrl;

    /**
     * 리뷰 제목
     */
    private String title;

    /**
     * 리뷰 내용
     */
    private String content;

    /**
     * 리뷰 내용 요약 (목록용)
     */
    private String contentSummary;

    /**
     * 평점
     */
    private Integer rating;

    /**
     * 평점 텍스트 (별점 표시)
     */
    private String ratingText;

    /**
     * 리뷰 상태
     */
    private ReviewStatus status;

    /**
     * 리뷰 상태 텍스트
     */
    private String statusText;

    /**
     * 리뷰 표시 가능 여부
     */
    private Boolean displayable;

    /**
     * 좋아요 개수
     */
    private Integer likeCount;

    /**
     * 현재 사용자의 좋아요 여부 (로그인된 경우)
     */
    private Boolean isLikedByCurrentUser;

    /**
     * 리뷰 수정 가능 여부
     */
    private Boolean editable;

    /**
     * 수정 가능 마감시간 (작성 후 N시간 내)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime editableUntil;

    /**
     * 리뷰 생성일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 리뷰 수정일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    /**
     * 리뷰 삭제 여부
     */
    private Boolean isDeleted;

    /**
     * 작성일 텍스트 (상대 시간)
     */
    private String createdAtText;

    /**
     * 수정일 텍스트 (상대 시간)
     */
    private String updatedAtText;

    /**
     * 신규 리뷰 여부 (24시간 이내)
     */
    private Boolean isNew;

    /**
     * 인기 리뷰 여부 (좋아요 10개 이상)
     */
    private Boolean isPopular;

    /**
     * 베스트 리뷰 여부 (좋아요 50개 이상, 높은 평점)
     */
    private Boolean isBest;

    /**
     * 리뷰 길이 카테고리 (짧음, 보통, 길음)
     */
    private String lengthCategory;

    /**
     * 도움됨 지수 (좋아요 수와 평점을 고려한 종합 점수)
     */
    private Double helpfulScore;

    /**
     * Entity를 ResponseDto로 변환하는 메소드 (기본 정보)
     * 
     * @param review 리뷰 엔티티
     * @return ReviewResponseDto 객체
     */
    public static ReviewResponseDto fromEntity(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .memberId(review.getMemberId())
                .memberName(maskMemberName(review.getMemberId()))
                .productId(review.getProduct() != null ? review.getProduct().getProductId() : null)
                .productName(review.getProduct() != null ? review.getProduct().getProductName() : null)
                .productImageUrl(review.getProduct() != null ? 
                               buildProductImageUrl(review.getProduct().getImageName()) : null)
                .title(review.getTitle())
                .content(review.getContent())
                .contentSummary(createContentSummary(review.getContent()))
                .rating(review.getRating())
                .ratingText(formatRating(review.getRating()))
                .status(review.getStatus())
                .statusText(review.getStatus() != null ? review.getStatus().getDescription() : null)
                .displayable(review.isDisplayable())
                .likeCount(review.getLikeCount())
                .editable(review.isEditable(24)) // 24시간 내 수정 가능
                .editableUntil(review.getCreatedAt() != null ? 
                              review.getCreatedAt().plusHours(24) : null)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isDeleted(review.isDeleted())
                .createdAtText(formatRelativeTime(review.getCreatedAt()))
                .updatedAtText(formatRelativeTime(review.getUpdatedAt()))
                .isNew(isNewReview(review.getCreatedAt()))
                .isPopular(isPopularReview(review.getLikeCount()))
                .isBest(isBestReview(review.getLikeCount(), review.getRating()))
                .lengthCategory(categorizeLengthCategory(review.getContent()))
                .helpfulScore(calculateHelpfulScore(review.getLikeCount(), review.getRating()))
                .build();
    }

    /**
     * Entity를 ResponseDto로 변환하는 메소드 (사용자별 정보 포함)
     * 
     * @param review 리뷰 엔티티
     * @param currentMemberId 현재 로그인된 사용자 ID
     * @return ReviewResponseDto 객체
     */
    public static ReviewResponseDto fromEntity(Review review, Long currentMemberId) {
        ReviewResponseDto dto = fromEntity(review);
        
        if (dto != null && currentMemberId != null) {
            // 현재 사용자의 좋아요 여부 설정
            dto.setIsLikedByCurrentUser(review.isLikedByMember(currentMemberId));
            
            // 현재 사용자가 작성한 리뷰인 경우 실제 작성자명 표시
            if (review.getMemberId().equals(currentMemberId)) {
                dto.setMemberName("내가 작성한 리뷰");
            }
        }
        
        return dto;
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드
     * 
     * @param reviews 리뷰 엔티티 목록
     * @return ReviewResponseDto 목록
     */
    public static List<ReviewResponseDto> fromEntityList(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return List.of();
        }

        return reviews.stream()
                .map(ReviewResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드 (사용자별 정보 포함)
     * 
     * @param reviews 리뷰 엔티티 목록
     * @param currentMemberId 현재 로그인된 사용자 ID
     * @return ReviewResponseDto 목록
     */
    public static List<ReviewResponseDto> fromEntityList(List<Review> reviews, Long currentMemberId) {
        if (reviews == null || reviews.isEmpty()) {
            return List.of();
        }

        return reviews.stream()
                .map(review -> fromEntity(review, currentMemberId))
                .collect(Collectors.toList());
    }

    /**
     * 목록 페이지용 간소화된 정보 포함 ResponseDto 생성
     * 
     * @param review 리뷰 엔티티
     * @return 목록용 ReviewResponseDto 객체
     */
    public static ReviewResponseDto forList(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .memberName(maskMemberName(review.getMemberId()))
                .productName(review.getProduct() != null ? review.getProduct().getProductName() : null)
                .title(review.getTitle())
                .contentSummary(createContentSummary(review.getContent()))
                .rating(review.getRating())
                .ratingText(formatRating(review.getRating()))
                .displayable(review.isDisplayable())
                .likeCount(review.getLikeCount())
                .createdAt(review.getCreatedAt())
                .createdAtText(formatRelativeTime(review.getCreatedAt()))
                .isNew(isNewReview(review.getCreatedAt()))
                .isPopular(isPopularReview(review.getLikeCount()))
                .isBest(isBestReview(review.getLikeCount(), review.getRating()))
                .helpfulScore(calculateHelpfulScore(review.getLikeCount(), review.getRating()))
                .build();
    }

    /**
     * 카드 표시용 간단한 ResponseDto 생성
     * 
     * @param review 리뷰 엔티티
     * @return 카드용 ReviewResponseDto 객체
     */
    public static ReviewResponseDto forCard(Review review) {
        if (review == null) {
            return null;
        }

        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .memberName(maskMemberName(review.getMemberId()))
                .title(review.getTitle())
                .contentSummary(createContentSummary(review.getContent(), 50))
                .rating(review.getRating())
                .ratingText(formatRating(review.getRating()))
                .likeCount(review.getLikeCount())
                .createdAtText(formatRelativeTime(review.getCreatedAt()))
                .isPopular(isPopularReview(review.getLikeCount()))
                .isBest(isBestReview(review.getLikeCount(), review.getRating()))
                .build();
    }

    /**
     * 관리자용 상세 정보 포함 ResponseDto 생성
     * 
     * @param review 리뷰 엔티티
     * @return 관리자용 ReviewResponseDto 객체
     */
    public static ReviewResponseDto forAdmin(Review review) {
        ReviewResponseDto dto = fromEntity(review);
        
        if (dto != null) {
            // 관리자용 추가 정보
            dto.setMemberName(String.format("회원 #%d", review.getMemberId()));
            
            if (review.isDeleted()) {
                dto.setStatusText(String.format("삭제됨 (%s)", 
                    review.getDeletedAt().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            }
            
            // 관리자는 항상 모든 내용 볼 수 있음
            dto.setDisplayable(true);
        }
        
        return dto;
    }

    /**
     * 회원 ID를 마스킹 처리
     * 
     * @param memberId 회원 ID
     * @return 마스킹된 회원명
     */
    private static String maskMemberName(Long memberId) {
        if (memberId == null) {
            return "익명";
        }
        return String.format("회원***%d", memberId % 1000);
    }

    /**
     * 평점을 별점 텍스트로 변환
     * 
     * @param rating 평점
     * @return 별점 텍스트
     */
    private static String formatRating(Integer rating) {
        if (rating == null || rating <= 0) {
            return "평점 없음";
        }
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return String.format("%s (%d점)", stars.toString(), rating);
    }

    /**
     * 리뷰 내용 요약 생성
     * 
     * @param content 리뷰 내용
     * @return 요약 내용
     */
    private static String createContentSummary(String content) {
        return createContentSummary(content, 100);
    }

    /**
     * 리뷰 내용 요약 생성 (길이 지정)
     * 
     * @param content 리뷰 내용
     * @param maxLength 최대 길이
     * @return 요약 내용
     */
    private static String createContentSummary(String content, int maxLength) {
        if (content == null || content.trim().isEmpty()) {
            return "내용 없음";
        }
        
        String trimmed = content.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        
        return trimmed.substring(0, maxLength) + "...";
    }

    /**
     * 상품 이미지 URL 생성
     * 
     * @param imageName 이미지 파일명
     * @return 이미지 URL
     */
    private static String buildProductImageUrl(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) {
            return "/images/products/default.jpg";
        }
        return String.format("/images/products/%s", imageName);
    }

    /**
     * 상대 시간 텍스트 생성
     * 
     * @param dateTime 날짜시간
     * @return 상대 시간 텍스트
     */
    private static String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();
        
        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return String.format("%d분 전", minutes);
        } else if (minutes < 1440) { // 24시간
            return String.format("%d시간 전", minutes / 60);
        } else if (minutes < 43200) { // 30일
            return String.format("%d일 전", minutes / 1440);
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    /**
     * 신규 리뷰 여부 확인 (24시간 이내)
     * 
     * @param createdAt 생성일시
     * @return 신규 리뷰 여부
     */
    private static boolean isNewReview(LocalDateTime createdAt) {
        if (createdAt == null) {
            return false;
        }
        return createdAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * 인기 리뷰 여부 확인 (좋아요 10개 이상)
     * 
     * @param likeCount 좋아요 개수
     * @return 인기 리뷰 여부
     */
    private static boolean isPopularReview(int likeCount) {
        return likeCount >= 10;
    }

    /**
     * 베스트 리뷰 여부 확인 (좋아요 50개 이상, 평점 4점 이상)
     * 
     * @param likeCount 좋아요 개수
     * @param rating 평점
     * @return 베스트 리뷰 여부
     */
    private static boolean isBestReview(int likeCount, Integer rating) {
        return likeCount >= 50 && rating != null && rating >= 4;
    }

    /**
     * 리뷰 길이 카테고리 분류
     * 
     * @param content 리뷰 내용
     * @return 길이 카테고리
     */
    private static String categorizeLengthCategory(String content) {
        if (content == null) {
            return "없음";
        }
        
        int length = content.length();
        if (length < 50) {
            return "짧음";
        } else if (length < 200) {
            return "보통";
        } else {
            return "상세";
        }
    }

    /**
     * 도움됨 지수 계산 (좋아요 수와 평점을 고려)
     * 
     * @param likeCount 좋아요 개수
     * @param rating 평점
     * @return 도움됨 지수
     */
    private static Double calculateHelpfulScore(int likeCount, Integer rating) {
        if (rating == null || rating <= 0) {
            return (double) likeCount;
        }
        
        // 좋아요 수 * 0.7 + 평점 * 2 * 0.3
        return likeCount * 0.7 + rating * 2 * 0.3;
    }

    /**
     * 리뷰 통계 텍스트 생성
     * 
     * @return 통계 텍스트
     */
    public String getStatsText() {
        StringBuilder stats = new StringBuilder();
        
        if (ratingText != null) {
            stats.append(ratingText);
        }
        
        if (likeCount != null && likeCount > 0) {
            stats.append(String.format(" | 좋아요 %d개", likeCount));
        }
        
        if (createdAtText != null) {
            stats.append(String.format(" | %s", createdAtText));
        }
        
        if (Boolean.TRUE.equals(isBest)) {
            stats.append(" | 베스트");
        } else if (Boolean.TRUE.equals(isPopular)) {
            stats.append(" | 인기");
        } else if (Boolean.TRUE.equals(isNew)) {
            stats.append(" | 신규");
        }
        
        return stats.toString();
    }

    /**
     * 리뷰 품질 점수 계산 (1-100점)
     * 
     * @return 품질 점수
     */
    public int getQualityScore() {
        int score = 50; // 기본 점수
        
        // 내용 길이에 따른 점수
        if (content != null) {
            int length = content.length();
            if (length >= 200) {
                score += 20;
            } else if (length >= 100) {
                score += 10;
            } else if (length >= 50) {
                score += 5;
            }
        }
        
        // 좋아요 수에 따른 점수
        if (likeCount != null) {
            if (likeCount >= 50) {
                score += 20;
            } else if (likeCount >= 20) {
                score += 15;
            } else if (likeCount >= 10) {
                score += 10;
            } else if (likeCount >= 5) {
                score += 5;
            }
        }
        
        // 평점에 따른 점수
        if (rating != null && rating >= 4) {
            score += 10;
        }
        
        return Math.min(100, score);
    }

    @Override
    public String toString() {
        return String.format("ReviewResponseDto(id=%d, member=%s, product=%s, rating=%d, likes=%d)", 
                           reviewId, memberName, productName, rating, likeCount);
    }
}