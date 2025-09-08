package com.dodam.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 이벤트 보상 정보를 관리하는 Entity
 * 이벤트 참여에 따른 보상 지급 내역을 저장합니다.
 */
@Entity
@Table(name="event_reward",
indexes = {
  @Index(name="idx_event_reward_member", columnList="member_id"), // ✅
  @Index(name="idx_event_reward_event",  columnList="event_code"), // ✅
  @Index(name="idx_event_reward_status", columnList="status"),
  @Index(name="idx_event_reward_type",   columnList="reward_type"), // ✅
  @Index(name="idx_event_reward_created",columnList="created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EventReward {

    /**
     * 이벤트 보상 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_reward_id")
    private Long eventRewardId;

    /**
     * 회원 번호 (외래키 - Member Entity가 없어서 Long 타입으로 관리)
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * 이벤트 코드 (이벤트 식별자)
     */
    @Column(name = "event_code", nullable = false, length = 50)
    @NotBlank(message = "이벤트 코드는 필수입니다.")
    private String eventCode;

    /**
     * 이벤트 이름
     */
    @Column(name = "event_name", nullable = false, length = 200)
    @NotBlank(message = "이벤트 이름은 필수입니다.")
    private String eventName;

    /**
     * 보상 고유 번호
     */
    @Column(name = "reward_id", nullable = false)
    private Long rewardId;

    /**
     * 보상 타입 (POINT: 포인트, COUPON: 쿠폰, GIFTICON: 기프티콘, ITEM: 아이템)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false)
    @Builder.Default
    private RewardType rewardType = RewardType.POINT;

    /**
     * 보상 금액/수량
     */
    @Column(name = "reward_amount", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "보상 금액은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "보상 금액은 0보다 커야 합니다.")
    private BigDecimal rewardAmount;

    /**
     * 보상 설명
     */
    @Column(name = "reward_description", length = 500)
    private String rewardDescription;

    /**
     * 보상 지급 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RewardStatus status = RewardStatus.PENDING;

    /**
     * 이벤트 참여 조건 충족 여부
     */
    @Column(name = "condition_met", nullable = false)
    @Builder.Default
    private Boolean conditionMet = false;

    /**
     * 보상 지급 예정일
     */
    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    /**
     * 보상 지급일시
     */
    @Column(name = "rewarded_at")
    private LocalDateTime rewardedAt;

    /**
     * 보상 만료일시
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * 이벤트 시작일시
     */
    @Column(name = "event_start_date")
    private LocalDateTime eventStartDate;

    /**
     * 이벤트 종료일시  
     */
    @Column(name = "event_end_date")
    private LocalDateTime eventEndDate;

    /**
     * 관련 참조 번호 (주문번호, 리뷰번호 등)
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * 관련 참조 타입 (ORDER, REVIEW, SIGNUP 등)
     */
    @Column(name = "reference_type", length = 50)
    private String referenceType;

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
     * 보상 타입 열거형
     */
    public enum RewardType {
        POINT("포인트"),
        COUPON("쿠폰"),
        GIFTICON("기프티콘"),
        ITEM("아이템"),
        DISCOUNT("할인혜택"),
        CASHBACK("캐시백");

        private final String description;

        RewardType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 보상 상태 열거형
     */
    public enum RewardStatus {
        PENDING("대기중"),
        APPROVED("승인됨"),
        REWARDED("지급완료"),
        EXPIRED("만료됨"),
        CANCELLED("취소됨"),
        FAILED("지급실패");

        private final String description;

        RewardStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 이벤트 보상이 삭제되었는지 확인
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 이벤트 보상 소프트 삭제
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = RewardStatus.CANCELLED;
    }

    /**
     * 이벤트 보상 복구
     */
    public void restore() {
        this.deletedAt = null;
        this.status = RewardStatus.PENDING;
    }

    /**
     * 이벤트 조건 충족 처리
     */
    public void meetCondition() {
        this.conditionMet = true;
        if (this.status == RewardStatus.PENDING) {
            this.status = RewardStatus.APPROVED;
        }
    }

    /**
     * 보상 지급 처리
     */
    public void grantReward() {
        if (this.status != RewardStatus.APPROVED) {
            throw new IllegalStateException("승인된 보상만 지급할 수 있습니다.");
        }
        
        if (isExpired()) {
            throw new IllegalStateException("만료된 보상은 지급할 수 없습니다.");
        }

        this.status = RewardStatus.REWARDED;
        this.rewardedAt = LocalDateTime.now();
    }

    /**
     * 보상 지급 실패 처리
     * @param reason 실패 사유
     */
    public void failReward(String reason) {
        this.status = RewardStatus.FAILED;
        this.rewardDescription = (this.rewardDescription != null ? this.rewardDescription + " | " : "") 
                                + "실패사유: " + reason;
    }

    /**
     * 보상 취소 처리
     * @param reason 취소 사유
     */
    public void cancel(String reason) {
        this.status = RewardStatus.CANCELLED;
        this.rewardDescription = (this.rewardDescription != null ? this.rewardDescription + " | " : "") 
                                + "취소사유: " + reason;
    }

    /**
     * 보상이 만료되었는지 확인
     * @return 만료 여부
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 보상 지급이 가능한지 확인
     * @return 지급 가능 여부
     */
    public boolean isGrantable() {
        return !isDeleted() && 
               status == RewardStatus.APPROVED && 
               conditionMet && 
               !isExpired();
    }

    /**
     * 이벤트가 진행 중인지 확인
     * @return 이벤트 진행 여부
     */
    public boolean isEventActive() {
        LocalDateTime now = LocalDateTime.now();
        
        if (eventStartDate != null && now.isBefore(eventStartDate)) {
            return false; // 아직 시작하지 않음
        }
        
        if (eventEndDate != null && now.isAfter(eventEndDate)) {
            return false; // 이미 종료됨
        }
        
        return true; // 진행 중
    }

    /**
     * 이벤트 종료까지 남은 일수 계산
     * @return 종료까지 남은 일수 (종료된 경우 음수)
     */
    public long getDaysUntilEventEnd() {
        if (eventEndDate == null) {
            return Long.MAX_VALUE; // 종료일이 없는 경우
        }
        
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), eventEndDate);
    }

    /**
     * 보상 만료까지 남은 일수 계산
     * @return 만료까지 남은 일수 (만료된 경우 음수)
     */
    public long getDaysUntilExpiry() {
        if (expiresAt == null) {
            return Long.MAX_VALUE; // 만료일이 없는 경우
        }
        
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
    }

    /**
     * 회원이 소유한 보상인지 확인
     * @param memberId 확인할 회원 번호
     * @return 소유 여부
     */
    public boolean isOwnedBy(Long memberId) {
        return this.memberId != null && this.memberId.equals(memberId);
    }

    /**
     * 보상 지급률 계산 (전체 대상자 중 실제 지급된 비율)
     * 통계용 메소드로 별도 서비스에서 활용
     */
    public boolean isSuccessfullyRewarded() {
        return this.status == RewardStatus.REWARDED && this.rewardedAt != null;
    }
}