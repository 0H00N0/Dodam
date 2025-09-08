package com.dodam.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 리뷰 좋아요 정보를 관리하는 Entity
 * 회원이 리뷰에 대해 좋아요/싫어요를 표시한 정보를 저장합니다.
 */
@Entity
@Table(
		  name = "review_like",
		  uniqueConstraints = @UniqueConstraint(
		    name = "idx_review_like_unique",
		    columnNames = {"review_id","member_id"} // ✅
		  ),
		  indexes = {
		    @Index(name="idx_review_like_review", columnList="review_id"),
		    @Index(name="idx_review_like_member", columnList="member_id") // ✅
		  }
		)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"review"}) // 순환 참조 방지
public class ReviewLike {

    /**
     * 좋아요 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long likeId;

    /**
     * 회원 번호 (외래키 - Member Entity가 없어서 Long 타입으로 관리)
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * 좋아요 여부 (true: 좋아요, false: 취소됨)
     */
    @Column(name = "is_liked", nullable = false)
    @Builder.Default
    private Boolean isLiked = true;

    /**
     * 생성일시 (좋아요를 누른 시점)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시 (좋아요 상태 변경 시점)
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 리뷰 정보 (외래키)
     * 지연 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /**
     * 좋아요 토글 (좋아요 ↔ 취소)
     */
    public void toggle() {
        this.isLiked = !this.isLiked;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 좋아요 설정
     */
    public void like() {
        if (!this.isLiked) {
            this.isLiked = true;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 좋아요 취소
     */
    public void unlike() {
        if (this.isLiked) {
            this.isLiked = false;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 좋아요 상태 확인
     * @return 좋아요 여부
     */
    public boolean isLiked() {
        return this.isLiked != null && this.isLiked;
    }

    /**
     * 좋아요가 활성 상태인지 확인 (최근에 변경되지 않은 안정된 좋아요)
     * @param stabilityHours 안정성 확인 시간 (시간 단위)
     * @return 안정된 좋아요 여부
     */
    public boolean isStableLike(int stabilityHours) {
        if (!isLiked() || createdAt == null) {
            return false;
        }

        // 수정된 적이 없거나, 수정된 지 일정 시간이 지난 경우 안정된 것으로 판단
        LocalDateTime checkTime = updatedAt != null ? updatedAt : createdAt;
        LocalDateTime stabilityDeadline = checkTime.plusHours(stabilityHours);
        
        return LocalDateTime.now().isAfter(stabilityDeadline);
    }

    /**
     * 회원이 소유한 좋아요인지 확인
     * @param memberId 확인할 회원 번호
     * @return 소유 여부
     */
    public boolean isOwnedBy(Long memberId) {
        return this.memberId != null && this.memberId.equals(memberId);
    }

    /**
     * 좋아요 통계 정보를 위한 헬퍼 메소드
     * @return 통계용 가중치 (최근 좋아요일수록 높은 가중치)
     */
    public double getStatisticalWeight() {
        if (!isLiked() || createdAt == null) {
            return 0.0;
        }

        // 30일 전 좋아요는 가중치 0.5, 최근 좋아요는 가중치 1.0
        long daysSinceCreated = java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        double weight = Math.max(0.5, 1.0 - (daysSinceCreated / 60.0)); // 60일에 걸쳐 가중치 감소
        
        return weight;
    }
}