package com.dodam.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 리뷰 정보를 관리하는 Entity
 * 상품에 대한 고객 리뷰 정보를 저장합니다.
 */
@Entity

@Table(name="review",
  indexes = {
    @Index(name="idx_review_member", columnList="member_id"), // ✅
    @Index(name="idx_review_product", columnList="product_id"),
    @Index(name="idx_review_rating", columnList="rating"),
    @Index(name="idx_review_created", columnList="created_at")
  })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"product", "reviewLikes"}) // 순환 참조 방지
public class Review {

    /**
     * 리뷰 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    /**
     * 회원 번호 (외래키 - Member Entity가 없어서 Long 타입으로 관리)
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * 리뷰 제목 (필수값)
     */
    @Column(name = "title", nullable = false, length = 200)
    @NotBlank(message = "리뷰 제목은 필수입니다.")
    @Size(max = 200, message = "리뷰 제목은 200자를 초과할 수 없습니다.")
    private String title;

    /**
     * 리뷰 내용 (필수값)
     */
    @Column(name = "content", nullable = false, length = 2000)
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 2000, message = "리뷰 내용은 2000자를 초과할 수 없습니다.")
    private String content;

    /**
     * 평점 (1-5점)
     */
    @Column(name = "rating", nullable = false)
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Integer rating;

    /**
     * 리뷰 상태 (ACTIVE: 활성, HIDDEN: 숨김, REPORTED: 신고됨)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ACTIVE;

    /**
     * 생성일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 삭제일시 (소프트 삭제용)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 상품 정보 (외래키)
     * 지연 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * 이 리뷰에 대한 좋아요들
     * 지연 로딩으로 성능 최적화
     */
    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<ReviewLike> reviewLikes = new ArrayList<>();

    /**
     * 리뷰 상태 열거형
     */
    public enum ReviewStatus {
        ACTIVE("활성"),
        HIDDEN("숨김"),
        REPORTED("신고됨");

        private final String description;

        ReviewStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 리뷰가 삭제되었는지 확인
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 리뷰 소프트 삭제
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ReviewStatus.HIDDEN;
    }

    /**
     * 리뷰 복구
     */
    public void restore() {
        this.deletedAt = null;
        this.status = ReviewStatus.ACTIVE;
    }

    /**
     * 리뷰 숨김 처리
     */
    public void hide() {
        this.status = ReviewStatus.HIDDEN;
    }

    /**
     * 리뷰 신고 처리
     */
    public void report() {
        this.status = ReviewStatus.REPORTED;
    }

    /**
     * 리뷰 활성화
     */
    public void activate() {
        this.status = ReviewStatus.ACTIVE;
    }

    /**
     * 리뷰가 표시 가능한 상태인지 확인
     * @return 표시 가능 여부
     */
    public boolean isDisplayable() {
        return !isDeleted() && status == ReviewStatus.ACTIVE;
    }

    /**
     * 리뷰가 숨김 상태인지 확인
     * @return 숨김 상태 여부
     */
    public boolean isHidden() {
        return status == ReviewStatus.HIDDEN || isDeleted();
    }

    /**
     * 좋아요 개수 조회
     * @return 좋아요 개수
     */
    public int getLikeCount() {
        if (reviewLikes == null) {
            return 0;
        }
        
        return (int) reviewLikes.stream()
            .filter(ReviewLike::isLiked)
            .count();
    }

    /**
     * 특정 회원이 이 리뷰에 좋아요를 눌렀는지 확인
     * @param memberId 회원 번호
     * @return 좋아요 여부
     */
    public boolean isLikedByMember(Long memberId) {
        if (reviewLikes == null || memberId == null) {
            return false;
        }
        
        return reviewLikes.stream()
            .anyMatch(like -> like.getMemberId().equals(memberId) && like.isLiked());
    }

    /**
     * 리뷰 수정 가능한지 확인 (작성 후 일정 시간 내에만 수정 가능)
     * @param allowedHours 허용 시간 (시간 단위)
     * @return 수정 가능 여부
     */
    public boolean isEditable(int allowedHours) {
        if (isDeleted() || createdAt == null) {
            return false;
        }
        
        LocalDateTime deadline = createdAt.plusHours(allowedHours);
        return LocalDateTime.now().isBefore(deadline);
    }
}