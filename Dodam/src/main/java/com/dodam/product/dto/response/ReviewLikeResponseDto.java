package com.dodam.product.dto.response;

import com.dodam.product.entity.ReviewLike;
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
 * 리뷰 좋아요 응답 DTO
 * 리뷰 좋아요 조회 결과를 클라이언트에 전달할 때 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLikeResponseDto {

    /**
     * 좋아요 고유 번호
     */
    private Long likeId;

    /**
     * 회원 ID
     */
    private Long memberId;

    /**
     * 회원명 (마스킹 처리)
     */
    private String memberName;

    /**
     * 리뷰 ID
     */
    private Long reviewId;

    /**
     * 리뷰 제목
     */
    private String reviewTitle;

    /**
     * 리뷰 내용 요약
     */
    private String reviewContentSummary;

    /**
     * 리뷰 평점
     */
    private Integer reviewRating;

    /**
     * 리뷰 평점 텍스트
     */
    private String reviewRatingText;

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
     * 좋아요 여부
     */
    private Boolean isLiked;

    /**
     * 좋아요 상태 텍스트
     */
    private String likeStatusText;

    /**
     * 좋아요 생성일시 (처음 좋아요를 누른 시점)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 좋아요 수정일시 (좋아요 상태 변경 시점)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    /**
     * 좋아요 생성일 텍스트 (상대 시간)
     */
    private String createdAtText;

    /**
     * 좋아요 수정일 텍스트 (상대 시간)
     */
    private String updatedAtText;

    /**
     * 안정된 좋아요 여부 (최근에 변경되지 않은 좋아요)
     */
    private Boolean isStable;

    /**
     * 통계용 가중치
     */
    private Double statisticalWeight;

    /**
     * 좋아요 활성 여부 (현재 좋아요 상태)
     */
    private Boolean isActive;

    /**
     * 최근 활동 여부 (최근 24시간 내 변경)
     */
    private Boolean isRecent;

    /**
     * Entity를 ResponseDto로 변환하는 메소드
     * 
     * @param reviewLike 리뷰 좋아요 엔티티
     * @return ReviewLikeResponseDto 객체
     */
    public static ReviewLikeResponseDto fromEntity(ReviewLike reviewLike) {
        if (reviewLike == null) {
            return null;
        }

        return ReviewLikeResponseDto.builder()
                .likeId(reviewLike.getLikeId())
                .memberId(reviewLike.getMemberId())
                .memberName(maskMemberName(reviewLike.getMemberId()))
                .reviewId(reviewLike.getReview() != null ? reviewLike.getReview().getReviewId() : null)
                .reviewTitle(reviewLike.getReview() != null ? reviewLike.getReview().getTitle() : null)
                .reviewContentSummary(reviewLike.getReview() != null ? 
                                    createContentSummary(reviewLike.getReview().getContent()) : null)
                .reviewRating(reviewLike.getReview() != null ? reviewLike.getReview().getRating() : null)
                .reviewRatingText(reviewLike.getReview() != null ? 
                                formatRating(reviewLike.getReview().getRating()) : null)
                .productId(reviewLike.getReview() != null && reviewLike.getReview().getProduct() != null ? 
                          reviewLike.getReview().getProduct().getProductId() : null)
                .productName(reviewLike.getReview() != null && reviewLike.getReview().getProduct() != null ? 
                           reviewLike.getReview().getProduct().getProductName() : null)
                .productImageUrl(reviewLike.getReview() != null && reviewLike.getReview().getProduct() != null ? 
                               buildProductImageUrl(reviewLike.getReview().getProduct().getImageName()) : null)
                .isLiked(reviewLike.getIsLiked())
                .likeStatusText(reviewLike.getIsLiked() ? "좋아요" : "좋아요 취소")
                .createdAt(reviewLike.getCreatedAt())
                .updatedAt(reviewLike.getUpdatedAt())
                .createdAtText(formatRelativeTime(reviewLike.getCreatedAt()))
                .updatedAtText(formatRelativeTime(reviewLike.getUpdatedAt()))
                .isStable(reviewLike.isStableLike(24)) // 24시간 기준 안정성
                .statisticalWeight(reviewLike.getStatisticalWeight())
                .isActive(reviewLike.isLiked())
                .isRecent(isRecentActivity(reviewLike.getUpdatedAt() != null ? 
                                         reviewLike.getUpdatedAt() : reviewLike.getCreatedAt()))
                .build();
    }

    /**
     * Entity를 ResponseDto로 변환하는 메소드 (사용자별 정보 포함)
     * 
     * @param reviewLike 리뷰 좋아요 엔티티
     * @param currentMemberId 현재 로그인된 사용자 ID
     * @return ReviewLikeResponseDto 객체
     */
    public static ReviewLikeResponseDto fromEntity(ReviewLike reviewLike, Long currentMemberId) {
        ReviewLikeResponseDto dto = fromEntity(reviewLike);
        
        if (dto != null && currentMemberId != null) {
            // 현재 사용자가 누른 좋아요인 경우 실제 회원명 표시
            if (reviewLike.getMemberId().equals(currentMemberId)) {
                dto.setMemberName("나");
            }
        }
        
        return dto;
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드
     * 
     * @param reviewLikes 리뷰 좋아요 엔티티 목록
     * @return ReviewLikeResponseDto 목록
     */
    public static List<ReviewLikeResponseDto> fromEntityList(List<ReviewLike> reviewLikes) {
        if (reviewLikes == null || reviewLikes.isEmpty()) {
            return List.of();
        }

        return reviewLikes.stream()
                .map(ReviewLikeResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드 (사용자별 정보 포함)
     * 
     * @param reviewLikes 리뷰 좋아요 엔티티 목록
     * @param currentMemberId 현재 로그인된 사용자 ID
     * @return ReviewLikeResponseDto 목록
     */
    public static List<ReviewLikeResponseDto> fromEntityList(List<ReviewLike> reviewLikes, Long currentMemberId) {
        if (reviewLikes == null || reviewLikes.isEmpty()) {
            return List.of();
        }

        return reviewLikes.stream()
                .map(like -> fromEntity(like, currentMemberId))
                .collect(Collectors.toList());
    }

    /**
     * 간단한 좋아요 정보 ResponseDto 생성 (목록용)
     * 
     * @param reviewLike 리뷰 좋아요 엔티티
     * @return 간단한 ReviewLikeResponseDto 객체
     */
    public static ReviewLikeResponseDto forList(ReviewLike reviewLike) {
        if (reviewLike == null) {
            return null;
        }

        return ReviewLikeResponseDto.builder()
                .likeId(reviewLike.getLikeId())
                .memberId(reviewLike.getMemberId())
                .memberName(maskMemberName(reviewLike.getMemberId()))
                .reviewId(reviewLike.getReview() != null ? reviewLike.getReview().getReviewId() : null)
                .isLiked(reviewLike.getIsLiked())
                .likeStatusText(reviewLike.getIsLiked() ? "좋아요" : "좋아요 취소")
                .createdAt(reviewLike.getCreatedAt())
                .createdAtText(formatRelativeTime(reviewLike.getCreatedAt()))
                .isActive(reviewLike.isLiked())
                .isRecent(isRecentActivity(reviewLike.getUpdatedAt() != null ? 
                                         reviewLike.getUpdatedAt() : reviewLike.getCreatedAt()))
                .build();
    }

    /**
     * 관리자용 상세 정보 포함 ResponseDto 생성
     * 
     * @param reviewLike 리뷰 좋아요 엔티티
     * @return 관리자용 ReviewLikeResponseDto 객체
     */
    public static ReviewLikeResponseDto forAdmin(ReviewLike reviewLike) {
        ReviewLikeResponseDto dto = fromEntity(reviewLike);
        
        if (dto != null) {
            // 관리자용 추가 정보
            dto.setMemberName(String.format("회원 #%d", reviewLike.getMemberId()));
            
            // 상세한 상태 정보
            if (reviewLike.getUpdatedAt() != null) {
                dto.setLikeStatusText(String.format("%s (수정됨: %s)", 
                    reviewLike.getIsLiked() ? "좋아요" : "좋아요 취소",
                    reviewLike.getUpdatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))));
            }
        }
        
        return dto;
    }

    /**
     * 통계용 간단한 ResponseDto 생성
     * 
     * @param reviewLike 리뷰 좋아요 엔티티
     * @return 통계용 ReviewLikeResponseDto 객체
     */
    public static ReviewLikeResponseDto forStatistics(ReviewLike reviewLike) {
        if (reviewLike == null) {
            return null;
        }

        return ReviewLikeResponseDto.builder()
                .likeId(reviewLike.getLikeId())
                .memberId(reviewLike.getMemberId())
                .reviewId(reviewLike.getReview() != null ? reviewLike.getReview().getReviewId() : null)
                .isLiked(reviewLike.getIsLiked())
                .createdAt(reviewLike.getCreatedAt())
                .updatedAt(reviewLike.getUpdatedAt())
                .isStable(reviewLike.isStableLike(24))
                .statisticalWeight(reviewLike.getStatisticalWeight())
                .isActive(reviewLike.isLiked())
                .build();
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
        if (content == null || content.trim().isEmpty()) {
            return "내용 없음";
        }
        
        String trimmed = content.trim();
        if (trimmed.length() <= 50) {
            return trimmed;
        }
        
        return trimmed.substring(0, 50) + "...";
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
     * 최근 활동 여부 확인 (24시간 이내)
     * 
     * @param dateTime 날짜시간
     * @return 최근 활동 여부
     */
    private static boolean isRecentActivity(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * 좋아요 액션 텍스트 생성
     * 
     * @return 액션 텍스트
     */
    public String getActionText() {
        if (Boolean.TRUE.equals(isLiked)) {
            return "좋아요를 눌렀습니다";
        } else {
            return "좋아요를 취소했습니다";
        }
    }

    /**
     * 좋아요 히스토리 텍스트 생성
     * 
     * @return 히스토리 텍스트
     */
    public String getHistoryText() {
        StringBuilder history = new StringBuilder();
        
        if (createdAtText != null) {
            history.append(String.format("처음 좋아요: %s", createdAtText));
        }
        
        if (updatedAt != null && updatedAtText != null && !updatedAt.equals(createdAt)) {
            history.append(String.format(" | 마지막 변경: %s", updatedAtText));
        }
        
        if (Boolean.TRUE.equals(isStable)) {
            history.append(" | 안정적");
        }
        
        if (Boolean.TRUE.equals(isRecent)) {
            history.append(" | 최근 활동");
        }
        
        return history.toString();
    }

    /**
     * 좋아요 품질 점수 계산 (안정성, 최신성 고려)
     * 
     * @return 품질 점수 (0-100)
     */
    public int getQualityScore() {
        int score = 50; // 기본 점수
        
        // 현재 좋아요 상태
        if (Boolean.TRUE.equals(isLiked)) {
            score += 30;
        }
        
        // 안정성
        if (Boolean.TRUE.equals(isStable)) {
            score += 15;
        }
        
        // 통계적 가중치
        if (statisticalWeight != null) {
            score += (int) (statisticalWeight * 5);
        }
        
        return Math.min(100, Math.max(0, score));
    }

    /**
     * 좋아요 상태 요약 정보 생성
     * 
     * @return 상태 요약
     */
    public String getStatusSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(likeStatusText != null ? likeStatusText : "상태 불명");
        
        if (createdAtText != null) {
            summary.append(String.format(" (%s)", createdAtText));
        }
        
        if (Boolean.TRUE.equals(isStable)) {
            summary.append(" [안정적]");
        }
        
        if (Boolean.TRUE.equals(isRecent)) {
            summary.append(" [최근]");
        }
        
        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("ReviewLikeResponseDto(id=%d, member=%s, review=%d, liked=%s)", 
                           likeId, memberName, reviewId, isLiked);
    }
}