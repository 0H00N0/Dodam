package com.dodam.product.dto.request;

import com.dodam.product.entity.EventReward;
import com.dodam.product.entity.EventReward.RewardStatus;
import com.dodam.product.entity.EventReward.RewardType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 이벤트 보상 요청 DTO
 * 이벤트 보상 생성, 수정, 지급, 검색 요청 시 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRewardRequestDto {

    /**
     * 회원 ID (필수값)
     * 이벤트 참여자의 회원 번호입니다.
     */
    @NotNull(message = "회원 ID는 필수입니다.")
    @Min(value = 1, message = "유효한 회원 ID를 입력해주세요.")
    private Long memberId;

    /**
     * 이벤트 코드 (필수값)
     * 이벤트를 식별하는 고유 코드입니다.
     */
    @NotBlank(message = "이벤트 코드는 필수입니다.")
    @Size(min = 1, max = 50, message = "이벤트 코드는 1자 이상 50자 이하로 입력해주세요.")
    private String eventCode;

    /**
     * 이벤트 이름 (필수값)
     * 1자 이상 200자 이하로 입력해야 합니다.
     */
    @NotBlank(message = "이벤트 이름은 필수입니다.")
    @Size(min = 1, max = 200, message = "이벤트 이름은 1자 이상 200자 이하로 입력해주세요.")
    private String eventName;

    /**
     * 보상 고유 번호 (필수값)
     * 보상을 식별하는 번호입니다.
     */
    @NotNull(message = "보상 ID는 필수입니다.")
    @Min(value = 1, message = "유효한 보상 ID를 입력해주세요.")
    private Long rewardId;

    /**
     * 보상 타입 (필수값)
     * POINT, COUPON, GIFTICON, BADGE, DISCOUNT 중 하나를 입력해야 합니다.
     */
    @NotNull(message = "보상 타입은 필수입니다.")
    private RewardType rewardType;

    /**
     * 보상 금액/수량 (필수값)
     * 1 이상 1,000,000 이하로 입력해야 합니다.
     */
    @NotNull(message = "보상 금액은 필수입니다.")
    @DecimalMin(value = "1.0", message = "보상 금액은 1 이상이어야 합니다.")
    @DecimalMax(value = "1000000.0", message = "보상 금액은 1,000,000 이하로 입력해주세요.")
    @Digits(integer = 7, fraction = 2, message = "보상 금액은 소수점 2자리까지 입력 가능합니다.")
    private BigDecimal rewardAmount;

    /**
     * 보상 설명 (선택값)
     * 500자 이하로 입력 가능합니다.
     */
    @Size(max = 500, message = "보상 설명은 500자 이하로 입력해주세요.")
    private String rewardDescription;

    /**
     * 보상 지급 상태
     * PENDING, ELIGIBLE, REWARDED, EXPIRED, CANCELLED 중 하나를 입력할 수 있습니다.
     */
    private RewardStatus status;

    /**
     * 이벤트 참여 조건 충족 여부
     */
    private Boolean conditionMet;

    /**
     * 보상 지급 예정일 (선택값)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime scheduledDate;

    /**
     * 보상 만료일시 (선택값)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime expiresAt;

    /**
     * 이벤트 시작일시 (필수값)
     */
    @NotNull(message = "이벤트 시작일시는 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime eventStartDate;

    /**
     * 이벤트 종료일시 (필수값)
     */
    @NotNull(message = "이벤트 종료일시는 필수입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime eventEndDate;

    /**
     * 관련 참조 번호 (선택값)
     * 주문번호, 리뷰번호 등 관련 데이터의 ID입니다.
     */
    @Min(value = 1, message = "참조 번호는 1 이상이어야 합니다.")
    private Long referenceId;

    /**
     * 관련 참조 타입 (선택값)
     * ORDER, REVIEW, SIGNUP 등의 참조 타입입니다.
     */
    @Size(max = 50, message = "참조 타입은 50자 이하로 입력해주세요.")
    private String referenceType;

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
     * 검색 키워드 (이벤트명, 보상 설명에서 검색)
     */
    private String keyword;

    /**
     * 이벤트 코드 목록 (검색용)
     */
    private List<String> eventCodes;

    /**
     * 회원 ID 목록 (검색용)
     */
    private List<Long> memberIds;

    /**
     * 보상 타입 목록 (검색용)
     */
    private List<RewardType> rewardTypes;

    /**
     * 보상 상태 목록 (검색용)
     */
    private List<RewardStatus> statuses;

    /**
     * 최소 보상 금액 (검색용)
     */
    @DecimalMin(value = "0.0", message = "최소 보상 금액은 0 이상이어야 합니다.")
    private BigDecimal minRewardAmount;

    /**
     * 최대 보상 금액 (검색용)
     */
    @DecimalMin(value = "0.0", message = "최대 보상 금액은 0 이상이어야 합니다.")
    private BigDecimal maxRewardAmount;

    /**
     * 조건 충족된 보상만 조회 여부 (검색용)
     */
    private Boolean conditionMetOnly;

    /**
     * 지급 가능한 보상만 조회 여부 (검색용)
     */
    private Boolean grantableOnly;

    /**
     * 만료 예정 일수 (검색용)
     * N일 이내 만료 예정인 보상 조회
     */
    @Min(value = 1, message = "만료 예정 일수는 1일 이상이어야 합니다.")
    private Integer expiringInDays;

    /**
     * 진행 중인 이벤트만 조회 여부 (검색용)
     */
    private Boolean activeEventOnly;

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
     * 참조 타입 목록 (검색용)
     */
    private List<String> referenceTypes;

    /**
     * 정렬 기준 (검색용)
     * createdAt, rewardAmount, expiresAt, eventEndDate 등
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
     * @return EventReward 엔티티 객체
     */
    public EventReward toEntity() {
        return EventReward.builder()
                .memberId(this.memberId)
                .eventCode(this.eventCode)
                .eventName(this.eventName)
                .rewardId(this.rewardId)
                .rewardType(this.rewardType)
                .rewardAmount(this.rewardAmount)
                .rewardDescription(this.rewardDescription)
                .status(this.status != null ? this.status : RewardStatus.PENDING)
                .conditionMet(this.conditionMet != null ? this.conditionMet : false)
                .scheduledDate(this.scheduledDate)
                .expiresAt(this.expiresAt)
                .eventStartDate(this.eventStartDate)
                .eventEndDate(this.eventEndDate)
                .referenceId(this.referenceId)
                .referenceType(this.referenceType)
                .build();
    }

    /**
     * RequestDto를 Entity로 변환하는 메소드 (수정용)
     * 
     * @param eventRewardId 이벤트 보상 ID
     * @return EventReward 엔티티 객체
     */
    public EventReward toEntity(Long eventRewardId) {
        return EventReward.builder()
                .eventRewardId(eventRewardId)
                .memberId(this.memberId)
                .eventCode(this.eventCode)
                .eventName(this.eventName)
                .rewardId(this.rewardId)
                .rewardType(this.rewardType)
                .rewardAmount(this.rewardAmount)
                .rewardDescription(this.rewardDescription)
                .status(this.status != null ? this.status : RewardStatus.PENDING)
                .conditionMet(this.conditionMet != null ? this.conditionMet : false)
                .scheduledDate(this.scheduledDate)
                .expiresAt(this.expiresAt)
                .eventStartDate(this.eventStartDate)
                .eventEndDate(this.eventEndDate)
                .referenceId(this.referenceId)
                .referenceType(this.referenceType)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 기존 Entity를 RequestDto로 업데이트하는 메소드
     * 
     * @param eventReward 업데이트할 이벤트 보상 엔티티
     */
    public void updateEntity(EventReward eventReward) {
        if (this.eventName != null) {
            eventReward.setEventName(this.eventName);
        }
        if (this.rewardAmount != null) {
            eventReward.setRewardAmount(this.rewardAmount);
        }
        if (this.rewardDescription != null) {
            eventReward.setRewardDescription(this.rewardDescription);
        }
        if (this.status != null) {
            eventReward.setStatus(this.status);
        }
        if (this.conditionMet != null) {
            eventReward.setConditionMet(this.conditionMet);
        }
        if (this.scheduledDate != null) {
            eventReward.setScheduledDate(this.scheduledDate);
        }
        if (this.expiresAt != null) {
            eventReward.setExpiresAt(this.expiresAt);
        }
        if (this.eventStartDate != null) {
            eventReward.setEventStartDate(this.eventStartDate);
        }
        if (this.eventEndDate != null) {
            eventReward.setEventEndDate(this.eventEndDate);
        }
        if (this.referenceId != null) {
            eventReward.setReferenceId(this.referenceId);
        }
        if (this.referenceType != null) {
            eventReward.setReferenceType(this.referenceType);
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
               eventCode != null && 
               !eventCode.trim().isEmpty() && 
               eventCode.length() <= 50 &&
               eventName != null && 
               !eventName.trim().isEmpty() && 
               eventName.length() <= 200 &&
               rewardId != null && 
               rewardId > 0 &&
               rewardType != null &&
               rewardAmount != null && 
               rewardAmount.compareTo(BigDecimal.ONE) >= 0 &&
               eventStartDate != null &&
               eventEndDate != null &&
               eventStartDate.isBefore(eventEndDate);
    }

    /**
     * 검색 조건이 있는지 확인
     * 
     * @return 검색 조건 존재 여부
     */
    public boolean hasSearchConditions() {
        return keyword != null || 
               (eventCodes != null && !eventCodes.isEmpty()) ||
               (memberIds != null && !memberIds.isEmpty()) ||
               (rewardTypes != null && !rewardTypes.isEmpty()) ||
               (statuses != null && !statuses.isEmpty()) ||
               minRewardAmount != null || 
               maxRewardAmount != null || 
               conditionMetOnly != null ||
               grantableOnly != null ||
               expiringInDays != null ||
               activeEventOnly != null ||
               startDate != null || 
               endDate != null ||
               (referenceTypes != null && !referenceTypes.isEmpty());
    }

    /**
     * 보상 금액 범위 유효성 검사
     * 
     * @return 보상 금액 범위 유효성
     */
    public boolean isValidRewardAmountRange() {
        if (minRewardAmount != null && maxRewardAmount != null) {
            return minRewardAmount.compareTo(maxRewardAmount) <= 0;
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
     * 이벤트 기간 유효성 검사
     * 
     * @return 이벤트 기간 유효성
     */
    public boolean isValidEventPeriod() {
        if (eventStartDate != null && eventEndDate != null) {
            return eventStartDate.isBefore(eventEndDate);
        }
        return true;
    }

    /**
     * 조건 충족 요청인지 확인
     * 
     * @return 조건 충족 요청 여부
     */
    public boolean isConditionMetRequest() {
        return conditionMet != null && conditionMet;
    }

    /**
     * 보상 지급 요청인지 확인
     * 
     * @return 보상 지급 요청 여부
     */
    public boolean isGrantRequest() {
        return status == RewardStatus.REWARDED;
    }

    /**
     * 이벤트 보상 정보 정규화 (앞뒤 공백 제거, 기본값 설정)
     * 
     * @return 정규화된 RequestDto
     */
    public EventRewardRequestDto normalize() {
        if (this.eventCode != null) {
            this.eventCode = this.eventCode.trim().toUpperCase();
        }
        if (this.eventName != null) {
            this.eventName = this.eventName.trim();
        }
        if (this.rewardDescription != null && this.rewardDescription.trim().isEmpty()) {
            this.rewardDescription = null;
        }
        if (this.referenceType != null) {
            this.referenceType = this.referenceType.trim().toUpperCase();
        }
        if (this.keyword != null) {
            this.keyword = this.keyword.trim();
            if (this.keyword.isEmpty()) {
                this.keyword = null;
            }
        }
        if (this.status == null) {
            this.status = RewardStatus.PENDING;
        }
        if (this.conditionMet == null) {
            this.conditionMet = false;
        }
        return this;
    }

    /**
     * 생성용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param eventCode 이벤트 코드
     * @param eventName 이벤트 이름
     * @param rewardType 보상 타입
     * @param rewardAmount 보상 금액
     * @return EventRewardRequestDto 객체
     */
    public static EventRewardRequestDto createRequest(Long memberId, String eventCode, String eventName,
                                                     RewardType rewardType, BigDecimal rewardAmount) {
        return EventRewardRequestDto.builder()
                .memberId(memberId)
                .eventCode(eventCode)
                .eventName(eventName)
                .rewardId(1L) // 기본값
                .rewardType(rewardType)
                .rewardAmount(rewardAmount)
                .status(RewardStatus.PENDING)
                .conditionMet(false)
                .eventStartDate(LocalDateTime.now())
                .eventEndDate(LocalDateTime.now().plusDays(30))
                .build();
    }

    /**
     * 조건 충족 처리용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param eventCode 이벤트 코드
     * @param referenceId 참조 ID
     * @param referenceType 참조 타입
     * @return EventRewardRequestDto 객체
     */
    public static EventRewardRequestDto conditionMetRequest(Long memberId, String eventCode, 
                                                          Long referenceId, String referenceType) {
        return EventRewardRequestDto.builder()
                .memberId(memberId)
                .eventCode(eventCode)
                .conditionMet(true)
                .status(RewardStatus.APPROVED)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
    }

    /**
     * 보상 지급용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param eventCode 이벤트 코드
     * @return EventRewardRequestDto 객체
     */
    public static EventRewardRequestDto grantRequest(Long memberId, String eventCode) {
        return EventRewardRequestDto.builder()
                .memberId(memberId)
                .eventCode(eventCode)
                .status(RewardStatus.REWARDED)
                .build();
    }

    /**
     * 검색용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param status 보상 상태
     * @return EventRewardRequestDto 객체
     */
    public static EventRewardRequestDto searchRequest(Long memberId, RewardStatus status) {
        return EventRewardRequestDto.builder()
                .memberId(memberId)
                .status(status)
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
    }

    /**
     * 만료 예정 보상 조회용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param expiringInDays 만료 예정 일수
     * @return EventRewardRequestDto 객체
     */
    public static EventRewardRequestDto expiringRequest(Long memberId, Integer expiringInDays) {
        return EventRewardRequestDto.builder()
                .memberId(memberId)
                .expiringInDays(expiringInDays)
                .grantableOnly(true)
                .page(0)
                .size(50)
                .sortBy("expiresAt")
                .sortDirection("ASC")
                .build();
    }

    /**
     * Entity로부터 RequestDto 생성
     * 
     * @param eventReward 이벤트 보상 엔티티
     * @return EventRewardRequestDto 객체
     */
    public static EventRewardRequestDto fromEntity(EventReward eventReward) {
        if (eventReward == null) {
            return null;
        }

        return EventRewardRequestDto.builder()
                .memberId(eventReward.getMemberId())
                .eventCode(eventReward.getEventCode())
                .eventName(eventReward.getEventName())
                .rewardId(eventReward.getRewardId())
                .rewardType(eventReward.getRewardType())
                .rewardAmount(eventReward.getRewardAmount())
                .rewardDescription(eventReward.getRewardDescription())
                .status(eventReward.getStatus())
                .conditionMet(eventReward.getConditionMet())
                .scheduledDate(eventReward.getScheduledDate())
                .expiresAt(eventReward.getExpiresAt())
                .eventStartDate(eventReward.getEventStartDate())
                .eventEndDate(eventReward.getEventEndDate())
                .referenceId(eventReward.getReferenceId())
                .referenceType(eventReward.getReferenceType())
                .createdAt(eventReward.getCreatedAt())
                .updatedAt(eventReward.getUpdatedAt())
                .build();
    }

    @Override
    public String toString() {
        return String.format("EventRewardRequestDto(memberId=%d, eventCode=%s, rewardType=%s, rewardAmount=%s, status=%s)", 
                           memberId, eventCode, rewardType, rewardAmount, status);
    }
}