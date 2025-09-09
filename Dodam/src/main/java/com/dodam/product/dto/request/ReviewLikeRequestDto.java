package com.dodam.product.dto.request;

import com.dodam.product.entity.Review;
import com.dodam.product.entity.ReviewLike;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 리뷰 좋아요 요청 DTO
 * 리뷰 좋아요/싫어요 토글 및 조회 요청 시 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLikeRequestDto {

    /**
     * 회원 ID (필수값)
     * 좋아요를 누르는 회원의 번호입니다.
     */
    @NotNull(message = "회원 ID는 필수입니다.")
    @Min(value = 1, message = "유효한 회원 ID를 입력해주세요.")
    private Long memberId;

    /**
     * 리뷰 ID (필수값)
     * 좋아요 대상 리뷰의 번호입니다.
     */
    @NotNull(message = "리뷰 ID는 필수입니다.")
    @Min(value = 1, message = "유효한 리뷰 ID를 입력해주세요.")
    private Long reviewId;

    /**
     * 좋아요 여부
     * true: 좋아요, false: 좋아요 취소
     * null인 경우 토글 동작을 수행합니다.
     */
    private Boolean isLiked;

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
     * 회원 ID 목록 (검색용)
     */
    private List<Long> memberIds;

    /**
     * 리뷰 ID 목록 (검색용)
     */
    private List<Long> reviewIds;

    /**
     * 좋아요 상태 (검색용)
     * true: 좋아요만, false: 취소된 것만, null: 전체
     */
    private Boolean likedStatus;

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
     * 안정된 좋아요만 조회 (검색용)
     * 최근에 변경되지 않은 안정된 좋아요만 조회합니다.
     */
    private Boolean stableOnly;

    /**
     * 안정성 확인 시간 (시간 단위, 검색용)
     * stableOnly가 true인 경우 사용됩니다.
     */
    @Min(value = 1, message = "안정성 확인 시간은 1시간 이상이어야 합니다.")
    private Integer stabilityHours;

    /**
     * 정렬 기준 (검색용)
     * createdAt, updatedAt 등
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
    @Min(value = 1, message = "페이지 크기는 1 이상 100 이하로 설정해주세요.")
    private Integer size;

    /**
     * RequestDto를 Entity로 변환하는 메소드 (생성용)
     * 
     * @param review 리뷰 엔티티
     * @return ReviewLike 엔티티 객체
     */
    public ReviewLike toEntity(Review review) {
        return ReviewLike.builder()
                .memberId(this.memberId)
                .isLiked(this.isLiked != null ? this.isLiked : true)
                .review(review)
                .build();
    }

    /**
     * RequestDto를 Entity로 변환하는 메소드 (수정용)
     * 
     * @param likeId 좋아요 ID
     * @param review 리뷰 엔티티
     * @return ReviewLike 엔티티 객체
     */
    public ReviewLike toEntity(Long likeId, Review review) {
        return ReviewLike.builder()
                .likeId(likeId)
                .memberId(this.memberId)
                .isLiked(this.isLiked != null ? this.isLiked : true)
                .review(review)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 기존 Entity를 RequestDto로 업데이트하는 메소드
     * 
     * @param reviewLike 업데이트할 리뷰 좋아요 엔티티
     */
    public void updateEntity(ReviewLike reviewLike) {
        if (this.isLiked != null) {
            if (this.isLiked) {
                reviewLike.like();
            } else {
                reviewLike.unlike();
            }
        }
    }

    /**
     * 토글 동작을 위한 Entity 업데이트 메소드
     * 
     * @param reviewLike 토글할 리뷰 좋아요 엔티티
     */
    public void toggleEntity(ReviewLike reviewLike) {
        reviewLike.toggle();
    }

    /**
     * 유효성 검사 통과 여부 확인
     * 
     * @return 유효성 검사 통과 여부
     */
    public boolean isValid() {
        return memberId != null && 
               memberId > 0 &&
               reviewId != null && 
               reviewId > 0;
    }

    /**
     * 검색 조건이 있는지 확인
     * 
     * @return 검색 조건 존재 여부
     */
    public boolean hasSearchConditions() {
        return (memberIds != null && !memberIds.isEmpty()) ||
               (reviewIds != null && !reviewIds.isEmpty()) ||
               likedStatus != null ||
               startDate != null || 
               endDate != null ||
               stableOnly != null;
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
     * 토글 요청인지 확인
     * 
     * @return 토글 요청 여부
     */
    public boolean isToggleRequest() {
        return isLiked == null;
    }

    /**
     * 좋아요 요청인지 확인
     * 
     * @return 좋아요 요청 여부
     */
    public boolean isLikeRequest() {
        return isLiked != null && isLiked;
    }

    /**
     * 좋아요 취소 요청인지 확인
     * 
     * @return 좋아요 취소 요청 여부
     */
    public boolean isUnlikeRequest() {
        return isLiked != null && !isLiked;
    }

    /**
     * 안정성 확인이 필요한 요청인지 확인
     * 
     * @return 안정성 확인 필요 여부
     */
    public boolean needsStabilityCheck() {
        return stableOnly != null && stableOnly;
    }

    /**
     * 리뷰 좋아요 정보 정규화 (기본값 설정)
     * 
     * @return 정규화된 RequestDto
     */
    public ReviewLikeRequestDto normalize() {
        if (this.stabilityHours == null && needsStabilityCheck()) {
            this.stabilityHours = 1; // 기본값: 1시간
        }
        return this;
    }

    /**
     * 좋아요 토글용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return ReviewLikeRequestDto 객체
     */
    public static ReviewLikeRequestDto toggleRequest(Long memberId, Long reviewId) {
        return ReviewLikeRequestDto.builder()
                .memberId(memberId)
                .reviewId(reviewId)
                .isLiked(null) // 토글을 위해 null 설정
                .build();
    }

    /**
     * 좋아요 요청용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return ReviewLikeRequestDto 객체
     */
    public static ReviewLikeRequestDto likeRequest(Long memberId, Long reviewId) {
        return ReviewLikeRequestDto.builder()
                .memberId(memberId)
                .reviewId(reviewId)
                .isLiked(true)
                .build();
    }

    /**
     * 좋아요 취소 요청용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return ReviewLikeRequestDto 객체
     */
    public static ReviewLikeRequestDto unlikeRequest(Long memberId, Long reviewId) {
        return ReviewLikeRequestDto.builder()
                .memberId(memberId)
                .reviewId(reviewId)
                .isLiked(false)
                .build();
    }

    /**
     * 검색용 RequestDto 생성 팩토리 메소드
     * 
     * @param reviewId 리뷰 ID
     * @param likedStatus 좋아요 상태
     * @return ReviewLikeRequestDto 객체
     */
    public static ReviewLikeRequestDto searchRequest(Long reviewId, Boolean likedStatus) {
        return ReviewLikeRequestDto.builder()
                .reviewId(reviewId)
                .likedStatus(likedStatus)
                .page(0)
                .size(50)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
    }

    /**
     * 안정된 좋아요 조회용 RequestDto 생성 팩토리 메소드
     * 
     * @param reviewId 리뷰 ID
     * @param stabilityHours 안정성 확인 시간
     * @return ReviewLikeRequestDto 객체
     */
    public static ReviewLikeRequestDto stableLikesRequest(Long reviewId, Integer stabilityHours) {
        return ReviewLikeRequestDto.builder()
                .reviewId(reviewId)
                .likedStatus(true)
                .stableOnly(true)
                .stabilityHours(stabilityHours != null ? stabilityHours : 24)
                .page(0)
                .size(100)
                .sortBy("createdAt")
                .sortDirection("ASC")
                .build();
    }

    /**
     * Entity로부터 RequestDto 생성
     * 
     * @param reviewLike 리뷰 좋아요 엔티티
     * @return ReviewLikeRequestDto 객체
     */
    public static ReviewLikeRequestDto fromEntity(ReviewLike reviewLike) {
        if (reviewLike == null) {
            return null;
        }

        return ReviewLikeRequestDto.builder()
                .memberId(reviewLike.getMemberId())
                .reviewId(reviewLike.getReview() != null ? reviewLike.getReview().getReviewId() : null)
                .isLiked(reviewLike.getIsLiked())
                .createdAt(reviewLike.getCreatedAt())
                .updatedAt(reviewLike.getUpdatedAt())
                .build();
    }

    @Override
    public String toString() {
        return String.format("ReviewLikeRequestDto(memberId=%d, reviewId=%d, isLiked=%s)", 
                           memberId, reviewId, isLiked);
    }
}