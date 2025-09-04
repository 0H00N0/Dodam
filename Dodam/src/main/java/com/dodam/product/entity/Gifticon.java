package com.dodam.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 기프티콘 정보를 관리하는 Entity
 * 기프티콘 발행, 사용, 거래 내역을 저장합니다.
 */
@Entity
@Table(name = "gifticon", indexes = {
    @Index(name = "idx_gifticon_member", columnList = "memberId"),
    @Index(name = "idx_gifticon_status", columnList = "status"),
    @Index(name = "idx_gifticon_type", columnList = "transactionType"),
    @Index(name = "idx_gifticon_expiry", columnList = "expiryDate"),
    @Index(name = "idx_gifticon_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Gifticon {

    /**
     * 기프티콘 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gifticon_id")
    private Long gifticonId;

    /**
     * 회원 번호 (외래키 - Member Entity가 없어서 Long 타입으로 관리)
     */
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * 거래 타입 (ISSUED: 발행, USED: 사용, TRANSFERRED: 양도, EXPIRED: 만료)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @Builder.Default
    private TransactionType transactionType = TransactionType.ISSUED;

    /**
     * 거래 타입 이름 (사용자 친화적 이름)
     */
    @Column(name = "transaction_name", length = 100)
    private String transactionName;

    /**
     * 상품명
     */
    @Column(name = "product_name", nullable = false, length = 200)
    @NotBlank(message = "상품명은 필수입니다.")
    private String productName;

    /**
     * 기프티콘 금액
     */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    @NotNull(message = "기프티콘 금액은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "기프티콘 금액은 0보다 커야 합니다.")
    private BigDecimal amount;

    /**
     * 보상 포인트 (기프티콘 사용 시 지급되는 포인트)
     */
    @Column(name = "reward_amount", precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "보상 금액은 0 이상이어야 합니다.")
    @Builder.Default
    private BigDecimal rewardAmount = BigDecimal.ZERO;

    /**
     * 기프티콘 코드 (고유 식별자)
     */
    @Column(name = "gifticon_code", nullable = false, unique = true, length = 50)
    @NotBlank(message = "기프티콘 코드는 필수입니다.")
    private String gifticonCode;

    /**
     * 브랜드명
     */
    @Column(name = "brand_name", length = 100)
    private String brandName;

    /**
     * 기프티콘 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private GifticonStatus status = GifticonStatus.ACTIVE;

    /**
     * 유효기간
     */
    @Column(name = "expiry_date", nullable = false)
    @NotNull(message = "유효기간은 필수입니다.")
    private LocalDateTime expiryDate;

    /**
     * 사용일시 (사용된 경우)
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * 사용처 정보
     */
    @Column(name = "used_place", length = 200)
    private String usedPlace;

    /**
     * 바코드 이미지 경로
     */
    @Column(name = "barcode_image_path", length = 500)
    private String barcodeImagePath;

    /**
     * 기프티콘 이미지 경로
     */
    @Column(name = "gifticon_image_path", length = 500)
    private String gifticonImagePath;

    /**
     * 발행처 정보
     */
    @Column(name = "issuer", length = 100)
    private String issuer;

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
     * 거래 타입 열거형
     */
    public enum TransactionType {
        ISSUED("발행"),
        USED("사용"),
        TRANSFERRED("양도"),
        EXPIRED("만료"),
        REFUNDED("환불");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 기프티콘 상태 열거형
     */
    public enum GifticonStatus {
        ACTIVE("활성"),
        USED("사용완료"),
        EXPIRED("만료"),
        TRANSFERRED("양도완료"),
        REFUNDED("환불완료"),
        SUSPENDED("정지");

        private final String description;

        GifticonStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 기프티콘이 삭제되었는지 확인
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 기프티콘 소프트 삭제
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
        this.status = GifticonStatus.SUSPENDED;
    }

    /**
     * 기프티콘 복구
     */
    public void restore() {
        this.deletedAt = null;
        this.status = GifticonStatus.ACTIVE;
    }

    /**
     * 기프티콘 사용 처리
     * @param usedPlace 사용처
     */
    public void use(String usedPlace) {
        if (this.status != GifticonStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 기프티콘만 사용할 수 있습니다.");
        }
        
        if (isExpired()) {
            throw new IllegalStateException("만료된 기프티콘은 사용할 수 없습니다.");
        }

        this.status = GifticonStatus.USED;
        this.transactionType = TransactionType.USED;
        this.usedAt = LocalDateTime.now();
        this.usedPlace = usedPlace;
    }

    /**
     * 기프티콘 양도 처리
     * @param newMemberId 새로운 소유자 회원 번호
     */
    public void transfer(Long newMemberId) {
        if (this.status != GifticonStatus.ACTIVE) {
            throw new IllegalStateException("활성 상태의 기프티콘만 양도할 수 있습니다.");
        }
        
        if (isExpired()) {
            throw new IllegalStateException("만료된 기프티콘은 양도할 수 없습니다.");
        }

        this.memberId = newMemberId;
        this.status = GifticonStatus.TRANSFERRED;
        this.transactionType = TransactionType.TRANSFERRED;
    }

    /**
     * 기프티콘 만료 처리
     */
    public void expire() {
        this.status = GifticonStatus.EXPIRED;
        this.transactionType = TransactionType.EXPIRED;
    }

    /**
     * 기프티콘이 만료되었는지 확인
     * @return 만료 여부
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 기프티콘이 사용 가능한지 확인
     * @return 사용 가능 여부
     */
    public boolean isUsable() {
        return !isDeleted() && 
               status == GifticonStatus.ACTIVE && 
               !isExpired();
    }

    /**
     * 기프티콘이 양도 가능한지 확인
     * @return 양도 가능 여부
     */
    public boolean isTransferable() {
        return isUsable(); // 사용 가능한 조건과 동일
    }

    /**
     * 만료까지 남은 일수 계산
     * @return 만료까지 남은 일수 (만료된 경우 음수)
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE; // 만료일이 없는 경우
        }
        
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);
    }

    /**
     * 회원이 소유한 기프티콘인지 확인
     * @param memberId 확인할 회원 번호
     * @return 소유 여부
     */
    public boolean isOwnedBy(Long memberId) {
        return this.memberId != null && this.memberId.equals(memberId);
    }
}