package com.dodam.product.dto.response;

import com.dodam.product.entity.EventReward;
import com.dodam.product.entity.EventReward.RewardStatus;
import com.dodam.product.entity.EventReward.RewardType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 이벤트 보상 응답 DTO
 * 이벤트 보상 정보를 다양한 형태로 제공하는 응답 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventRewardResponseDto {

    /**
     * 보상 ID
     */
    private Long id;

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 이벤트 ID
     */
    private Long eventId;

    /**
     * 이벤트 제목
     */
    private String eventTitle;

    /**
     * 이벤트 설명
     */
    private String eventDescription;

    /**
     * 보상 유형 (POINT, COUPON, GIFTICON, BADGE, DISCOUNT)
     */
    private RewardType rewardType;

    /**
     * 보상 유형 한국어 명
     */
    private String rewardTypeDisplay;

    /**
     * 보상 값
     */
    private BigDecimal rewardValue;

    /**
     * 보상 값 표시용 (포맷팅된 문자열)
     */
    private String rewardValueDisplay;

    /**
     * 보상 상태 (PENDING, ELIGIBLE, REWARDED, EXPIRED, CANCELLED)
     */
    private RewardStatus status;

    /**
     * 보상 상태 한국어 명
     */
    private String statusDisplay;

    /**
     * 상태 색상 코드
     */
    private String statusColor;

    /**
     * 조건 데이터 (JSON)
     */
    private Map<String, Object> conditions;

    /**
     * 보상 데이터 (JSON)
     */
    private Map<String, Object> rewardData;

    /**
     * 발급 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime issuedAt;

    /**
     * 발급 일시 표시용
     */
    private String issuedAtDisplay;

    /**
     * 만료 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime expiredAt;

    /**
     * 만료 일시 표시용
     */
    private String expiredAtDisplay;

    /**
     * 수령 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime claimedAt;

    /**
     * 수령 일시 표시용
     */
    private String claimedAtDisplay;

    /**
     * 생성 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    // === 계산된 필드들 ===

    /**
     * 클레임 가능 여부
     */
    private Boolean claimable;

    /**
     * 만료까지 남은 일수
     */
    private Long daysUntilExpiry;

    /**
     * 만료 임박 경고 여부 (7일 이내)
     */
    private Boolean expiryWarning;

    /**
     * 보상 우선순위 점수 (높을수록 중요)
     */
    private Integer priorityScore;

    /**
     * 사용자 친화적 상태 메시지
     */
    private String userFriendlyMessage;

    /**
     * 다음 액션 가이드
     */
    private List<String> actionGuides;

    // === 정적 팩토리 메서드들 ===

    /**
     * EventReward 엔티티로부터 응답 DTO 생성
     */
    public static EventRewardResponseDto fromEntity(EventReward eventReward) {
        if (eventReward == null) {
            return null;
        }

        EventRewardResponseDto dto = EventRewardResponseDto.builder()
                .id(eventReward.getEventRewardId())
                .userId(eventReward.getMemberId())
                .eventId(null)
                .rewardType(eventReward.getRewardType())
                .rewardTypeDisplay(eventReward.getRewardType() != null ? 
                    eventReward.getRewardType().getDescription() : "알 수 없음")
                .rewardValue(eventReward.getRewardAmount())
                .status(eventReward.getStatus())
                .statusDisplay(eventReward.getStatus() != null ? 
                    eventReward.getStatus().getDescription() : "알 수 없음")
                .conditions(null)
                .rewardData(null)
                .issuedAt(eventReward.getCreatedAt())
                .expiredAt(eventReward.getExpiresAt())
                .claimedAt(eventReward.getRewardedAt())
                .createdAt(eventReward.getCreatedAt())
                .updatedAt(eventReward.getUpdatedAt())
                .build();

        // 계산된 필드들 설정
        dto.calculateDerivedFields();
        dto.setDisplayFields();
        dto.generateActionGuides();

        return dto;
    }

    /**
     * 리스트 뷰용 간소화된 DTO 생성
     */
    public static EventRewardResponseDto forListView(EventReward eventReward) {
        if (eventReward == null) {
            return null;
        }

        EventRewardResponseDto dto = EventRewardResponseDto.builder()
                .id(eventReward.getEventRewardId())
                .eventTitle(eventReward.getEventName())
                .rewardType(eventReward.getRewardType())
                .rewardTypeDisplay(eventReward.getRewardType().getDescription())
                .rewardValueDisplay(formatRewardValue(eventReward.getRewardType(), eventReward.getRewardAmount()))
                .status(eventReward.getStatus())
                .statusDisplay(eventReward.getStatus().getDescription())
                .statusColor(getStatusColor(eventReward.getStatus()))
                .expiredAt(eventReward.getExpiresAt())
                .claimedAt(eventReward.getRewardedAt())
                .build();

        dto.setClaimable(isClaimableStatus(eventReward.getStatus()));
        dto.calculateExpiryInfo();
        dto.setUserFriendlyMessage(generateUserFriendlyMessage(eventReward.getStatus(), dto.getDaysUntilExpiry()));

        return dto;
    }

    /**
     * 카드 뷰용 DTO 생성
     */
    public static EventRewardResponseDto forCardView(EventReward eventReward) {
        if (eventReward == null) {
            return null;
        }

        EventRewardResponseDto dto = EventRewardResponseDto.builder()
                .id(eventReward.getEventRewardId())
                .eventTitle(eventReward.getEventName())
                .eventDescription(eventReward.getRewardDescription())
                .rewardType(eventReward.getRewardType())
                .rewardTypeDisplay(eventReward.getRewardType().getDescription())
                .rewardValue(eventReward.getRewardAmount())
                .rewardValueDisplay(formatRewardValue(eventReward.getRewardType(), eventReward.getRewardAmount()))
                .status(eventReward.getStatus())
                .statusDisplay(eventReward.getStatus().getDescription())
                .statusColor(getStatusColor(eventReward.getStatus()))
                .issuedAt(eventReward.getCreatedAt())
                .expiredAt(eventReward.getExpiresAt())
                .claimedAt(eventReward.getRewardedAt())
                .build();

        dto.calculateDerivedFields();
        dto.setDisplayFields();

        return dto;
    }

    /**
     * 관리자 뷰용 상세 DTO 생성
     */
    public static EventRewardResponseDto forAdminView(EventReward eventReward) {
        EventRewardResponseDto dto = fromEntity(eventReward);
        if (dto != null) {
            // 관리자는 모든 정보를 볼 수 있으므로 마스킹하지 않음
            dto.setPriorityScore(calculatePriorityScore(eventReward));
        }
        return dto;
    }

    // === 내부 계산 메서드들 ===

    /**
     * 파생 필드들 계산
     */
    private void calculateDerivedFields() {
        calculateExpiryInfo();
        setClaimable(isClaimableStatus(this.status));
        setPriorityScore(calculatePriorityScore());
    }

    /**
     * 만료 정보 계산
     */
    private void calculateExpiryInfo() {
        if (this.expiredAt != null) {
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), this.expiredAt);
            setDaysUntilExpiry(Math.max(0, days));
            setExpiryWarning(days > 0 && days <= 7);
        } else {
            setDaysUntilExpiry(Long.MAX_VALUE);
            setExpiryWarning(false);
        }
    }

    /**
     * 우선순위 점수 계산
     */
    private Integer calculatePriorityScore() {
        if (this.status == null) return 0;

        int score = 50; // 기본 점수

        // 상태별 가중치
        switch (this.status) {
            case APPROVED: score += 40; break;
            case PENDING: score += 30; break;
            case REWARDED: score += 10; break;
            case EXPIRED: score -= 20; break;
            case CANCELLED: score -= 30; break;
        }

        // 보상 유형별 가중치
        if (this.rewardType != null) {
            switch (this.rewardType) {
                case GIFTICON: score += 20; break;
                case COUPON: score += 15; break;
                case POINT: score += 10; break;
                case DISCOUNT: score += 8; break;
                case ITEM: score += 5; break;
            }
        }

        // 만료 임박 가중치
        if (Boolean.TRUE.equals(this.expiryWarning)) {
            score += 25;
        }

        return Math.max(0, score);
    }

    /**
     * 표시용 필드들 설정
     */
    private void setDisplayFields() {
        // 날짜 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
        
        if (this.issuedAt != null) {
            this.issuedAtDisplay = this.issuedAt.format(formatter);
        }
        
        if (this.expiredAt != null) {
            this.expiredAtDisplay = this.expiredAt.format(formatter);
        }
        
        if (this.claimedAt != null) {
            this.claimedAtDisplay = this.claimedAt.format(formatter);
        }

        // 보상 값 포맷팅
        if (this.rewardType != null && this.rewardValue != null) {
            this.rewardValueDisplay = formatRewardValue(this.rewardType, this.rewardValue);
        }

        // 상태 색상 설정
        if (this.status != null) {
            this.statusColor = getStatusColor(this.status);
        }

        // 사용자 친화적 메시지 설정
        this.userFriendlyMessage = generateUserFriendlyMessage(this.status, this.daysUntilExpiry);
    }

    /**
     * 액션 가이드 생성
     */
    private void generateActionGuides() {
        List<String> guides = new ArrayList<>();

        if (this.status == null) {
            this.actionGuides = guides;
            return;
        }

        switch (this.status) {
            case APPROVED:
                guides.add("지금 바로 보상을 수령하세요!");
                if (Boolean.TRUE.equals(this.expiryWarning)) {
                    guides.add("⚠️ 만료가 임박했습니다. 서둘러 수령하세요.");
                }
                break;

            case PENDING:
                guides.add("이벤트 조건을 확인하고 참여를 완료하세요.");
                guides.add("조건 달성 후 자동으로 수령 가능 상태가 됩니다.");
                break;

            case REWARDED:
                guides.add("✅ 보상이 성공적으로 지급되었습니다.");
                guides.add("내 계정에서 보상 내역을 확인할 수 있습니다.");
                break;

            case EXPIRED:
                guides.add("❌ 보상 수령 기간이 만료되었습니다.");
                guides.add("새로운 이벤트를 확인해보세요.");
                break;

            case CANCELLED:
                guides.add("이벤트가 취소되어 보상을 받을 수 없습니다.");
                guides.add("다른 진행 중인 이벤트를 확인해보세요.");
                break;
        }

        this.actionGuides = guides;
    }

    // === 정적 유틸리티 메서드들 ===

    /**
     * 보상 값 포맷팅
     */
    private static String formatRewardValue(RewardType type, BigDecimal value) {
        if (type == null || value == null) {
            return "0";
        }
        return formatValueForType(type, value);
    }

    /**
     * 상태별 클레임 가능 여부 확인
     */
    private static boolean isClaimableStatus(RewardStatus status) {
        return status == RewardStatus.APPROVED;
    }

    /**
     * 타입별 보상 값 포맷팅
     */
    private static String formatValueForType(RewardType type, BigDecimal value) {
        if (type == null || value == null) {
            return "0";
        }

        switch (type) {
            case POINT:
                return String.format("%,d P", value.intValue());
            case COUPON:
            case DISCOUNT:
                return String.format("%,d원 할인", value.intValue());
            case GIFTICON:
                return String.format("%,d원 기프티콘", value.intValue());
            case ITEM:
                return "아이템 1개";
            case CASHBACK:
                return String.format("%,d원 캐시백", value.intValue());
            default:
                return value.toString();
        }
    }

    /**
     * 상태별 색상 반환
     */
    private static String getStatusColor(RewardStatus status) {
        if (status == null) return "#gray";
        
        switch (status) {
            case APPROVED: return "#28a745"; // 초록색
            case PENDING: return "#ffc107";  // 노란색
            case REWARDED: return "#007bff"; // 파란색
            case EXPIRED: return "#dc3545";  // 빨간색
            case CANCELLED: return "#6c757d"; // 회색
            default: return "#gray";
        }
    }

    /**
     * 사용자 친화적 메시지 생성
     */
    private static String generateUserFriendlyMessage(RewardStatus status, Long daysUntilExpiry) {
        if (status == null) {
            return "상태 정보가 없습니다.";
        }

        switch (status) {
            case APPROVED:
                if (daysUntilExpiry != null && daysUntilExpiry <= 7) {
                    return String.format("수령 가능! ⏰ %d일 후 만료", daysUntilExpiry);
                }
                return "수령 가능한 보상이 있습니다! 🎁";

            case PENDING:
                return "이벤트 조건 달성 시 자동 지급됩니다. 📝";

            case REWARDED:
                return "보상이 성공적으로 지급되었습니다. ✅";

            case EXPIRED:
                return "수령 기간이 만료되었습니다. ⏰";

            case CANCELLED:
                return "이벤트가 취소되었습니다. ❌";

            default:
                return status.getDescription();
        }
    }

    /**
     * 우선순위 점수 계산 (정적 버전)
     */
    private static Integer calculatePriorityScore(EventReward eventReward) {
        if (eventReward == null || eventReward.getStatus() == null) {
            return 0;
        }

        int score = 50;

        // 상태별 가중치
        switch (eventReward.getStatus()) {
            case APPROVED: score += 40; break;
            case PENDING: score += 30; break;
            case REWARDED: score += 10; break;
            case EXPIRED: score -= 20; break;
            case CANCELLED: score -= 30; break;
        }

        // 보상 유형별 가중치
        if (eventReward.getRewardType() != null) {
            switch (eventReward.getRewardType()) {
                case GIFTICON: score += 20; break;
                case COUPON: score += 15; break;
                case POINT: score += 10; break;
                case DISCOUNT: score += 8; break;
                case ITEM: score += 5; break;
            }
        }

        // 만료 임박 가중치
        if (eventReward.getExpiresAt() != null) {
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), eventReward.getExpiresAt());
            if (days > 0 && days <= 7) {
                score += 25;
            }
        }

        return Math.max(0, score);
    }

    // === 비즈니스 로직 메서드들 ===

    /**
     * 클레임 가능 여부 확인
     */
    public boolean isClaimable() {
        return Boolean.TRUE.equals(this.claimable);
    }

    /**
     * 만료 임박 여부 확인
     */
    public boolean isExpiryWarning() {
        return Boolean.TRUE.equals(this.expiryWarning);
    }

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return this.status == RewardStatus.EXPIRED || 
               (this.daysUntilExpiry != null && this.daysUntilExpiry <= 0);
    }

    /**
     * 높은 우선순위 보상인지 확인 (점수 70점 이상)
     */
    public boolean isHighPriority() {
        return this.priorityScore != null && this.priorityScore >= 70;
    }

    /**
     * 보상 카테고리 반환
     */
    public String getRewardCategory() {
        if (this.rewardType == null) {
            return "기타";
        }

        switch (this.rewardType) {
            case POINT:
                return "포인트";
            case COUPON:
            case DISCOUNT:
                return "할인 혜택";
            case GIFTICON:
                return "기프티콘";
            case ITEM:
                return "뱃지";
            default:
                return "기타";
        }
    }
}