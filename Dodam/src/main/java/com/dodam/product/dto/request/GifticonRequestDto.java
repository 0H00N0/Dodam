package com.dodam.product.dto.request;

import com.dodam.product.entity.Gifticon;
import com.dodam.product.entity.Gifticon.GifticonStatus;
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
 * 기프티콘 요청 DTO
 * 기프티콘 발행, 사용, 양도, 검색 요청 시 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GifticonRequestDto {

    /**
     * 회원 ID (필수값)
     * 기프티콘 소유자의 회원 번호입니다.
     */
    @NotNull(message = "회원 ID는 필수입니다.")
    @Min(value = 1, message = "유효한 회원 ID를 입력해주세요.")
    private Long memberId;

    /**
     * 상품명 (필수값)
     * 1자 이상 200자 이하로 입력해야 합니다.
     */
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(min = 1, max = 200, message = "상품명은 1자 이상 200자 이하로 입력해주세요.")
    private String productName;

    /**
     * 기프티콘 금액 (필수값)
     * 1,000원 이상 1,000,000원 이하로 입력해야 합니다.
     */
    @NotNull(message = "기프티콘 금액은 필수입니다.")
    @DecimalMin(value = "1000.0", message = "기프티콘 금액은 1,000원 이상이어야 합니다.")
    @DecimalMax(value = "1000000.0", message = "기프티콘 금액은 1,000,000원 이하로 입력해주세요.")
    @Digits(integer = 7, fraction = 2, message = "기프티콘 금액은 소수점 2자리까지 입력 가능합니다.")
    private BigDecimal amount;

    /**
     * 보상 금액 (선택값)
     * 기프티콘 사용 시 지급되는 포인트입니다.
     */
    @DecimalMin(value = "0.0", message = "보상 금액은 0원 이상이어야 합니다.")
    @DecimalMax(value = "100000.0", message = "보상 금액은 100,000원 이하로 입력해주세요.")
    @Digits(integer = 6, fraction = 2, message = "보상 금액은 소수점 2자리까지 입력 가능합니다.")
    private BigDecimal rewardAmount;

    /**
     * 기프티콘 코드 (발행 시 자동 생성 또는 직접 입력)
     * 50자 이하로 입력해야 합니다.
     */
    @Size(max = 50, message = "기프티콘 코드는 50자 이하로 입력해주세요.")
    private String gifticonCode;

    /**
     * 브랜드명 (선택값)
     * 100자 이하로 입력 가능합니다.
     */
    @Size(max = 100, message = "브랜드명은 100자 이하로 입력해주세요.")
    private String brandName;

    /**
     * 기프티콘 상태
     * ISSUED, USED, EXPIRED, TRANSFERRED, CANCELLED 중 하나를 입력할 수 있습니다.
     */
    private GifticonStatus status;

    /**
     * 유효기간 (필수값)
     * 현재 시점보다 미래 날짜여야 합니다.
     */
    @NotNull(message = "유효기간은 필수입니다.")
    @Future(message = "유효기간은 현재 시점보다 미래여야 합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime expiryDate;

    /**
     * 사용처 정보 (사용 시에만 입력)
     * 200자 이하로 입력 가능합니다.
     */
    @Size(max = 200, message = "사용처 정보는 200자 이하로 입력해주세요.")
    private String usedPlace;

    /**
     * 바코드 이미지 경로 (선택값)
     * 500자 이하로 입력 가능합니다.
     */
    @Size(max = 500, message = "바코드 이미지 경로는 500자 이하로 입력해주세요.")
    private String barcodeImagePath;

    /**
     * 기프티콘 이미지 경로 (선택값)
     * 500자 이하로 입력 가능합니다.
     */
    @Size(max = 500, message = "기프티콘 이미지 경로는 500자 이하로 입력해주세요.")
    private String gifticonImagePath;

    /**
     * 발행처 정보 (선택값)
     * 100자 이하로 입력 가능합니다.
     */
    @Size(max = 100, message = "발행처 정보는 100자 이하로 입력해주세요.")
    private String issuer;

    /**
     * 양도받을 회원 ID (양도 시에만 사용)
     */
    @Min(value = 1, message = "유효한 회원 ID를 입력해주세요.")
    private Long newMemberId;

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
     * 검색 키워드 (상품명, 브랜드명에서 검색)
     */
    private String keyword;

    /**
     * 최소 금액 (검색용)
     */
    @DecimalMin(value = "0.0", message = "최소 금액은 0원 이상이어야 합니다.")
    private BigDecimal minAmount;

    /**
     * 최대 금액 (검색용)
     */
    @DecimalMin(value = "0.0", message = "최대 금액은 0원 이상이어야 합니다.")
    private BigDecimal maxAmount;

    /**
     * 회원 ID 목록 (검색용)
     */
    private List<Long> memberIds;

    /**
     * 브랜드명 목록 (검색용)
     */
    private List<String> brandNames;

    /**
     * 기프티콘 상태 목록 (검색용)
     */
    private List<GifticonStatus> statuses;

    /**
     * 만료 예정 일수 (검색용)
     * N일 이내 만료 예정인 기프티콘 조회
     */
    @Min(value = 1, message = "만료 예정 일수는 1일 이상이어야 합니다.")
    private Integer expiringInDays;

    /**
     * 사용 가능한 기프티콘만 조회 여부 (검색용)
     */
    private Boolean usableOnly;

    /**
     * 양도 가능한 기프티콘만 조회 여부 (검색용)
     */
    private Boolean transferableOnly;

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
     * 정렬 기준 (검색용)
     * createdAt, amount, expiryDate 등
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
     * RequestDto를 Entity로 변환하는 메소드 (발행용)
     * 
     * @return Gifticon 엔티티 객체
     */
    public Gifticon toEntity() {
        return Gifticon.builder()
                .memberId(this.memberId)
                .productName(this.productName)
                .amount(this.amount)
                .rewardAmount(this.rewardAmount != null ? this.rewardAmount : BigDecimal.ZERO)
                .gifticonCode(this.gifticonCode)
                .brandName(this.brandName)
                .status(this.status != null ? this.status : GifticonStatus.ACTIVE)
                .expiryDate(this.expiryDate)
                .barcodeImagePath(this.barcodeImagePath)
                .gifticonImagePath(this.gifticonImagePath)
                .issuer(this.issuer)
                .build();
    }

    /**
     * RequestDto를 Entity로 변환하는 메소드 (수정용)
     * 
     * @param gifticonId 기프티콘 ID
     * @return Gifticon 엔티티 객체
     */
    public Gifticon toEntity(Long gifticonId) {
        return Gifticon.builder()
                .gifticonId(gifticonId)
                .memberId(this.memberId)
                .productName(this.productName)
                .amount(this.amount)
                .rewardAmount(this.rewardAmount != null ? this.rewardAmount : BigDecimal.ZERO)
                .gifticonCode(this.gifticonCode)
                .brandName(this.brandName)
                .status(this.status != null ? this.status : GifticonStatus.ACTIVE)
                .expiryDate(this.expiryDate)
                .usedPlace(this.usedPlace)
                .barcodeImagePath(this.barcodeImagePath)
                .gifticonImagePath(this.gifticonImagePath)
                .issuer(this.issuer)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 기존 Entity를 RequestDto로 업데이트하는 메소드
     * 
     * @param gifticon 업데이트할 기프티콘 엔티티
     */
    public void updateEntity(Gifticon gifticon) {
        if (this.productName != null) {
            gifticon.setProductName(this.productName);
        }
        if (this.amount != null) {
            gifticon.setAmount(this.amount);
        }
        if (this.rewardAmount != null) {
            gifticon.setRewardAmount(this.rewardAmount);
        }
        if (this.brandName != null) {
            gifticon.setBrandName(this.brandName);
        }
        if (this.expiryDate != null) {
            gifticon.setExpiryDate(this.expiryDate);
        }
        if (this.barcodeImagePath != null) {
            gifticon.setBarcodeImagePath(this.barcodeImagePath);
        }
        if (this.gifticonImagePath != null) {
            gifticon.setGifticonImagePath(this.gifticonImagePath);
        }
        if (this.issuer != null) {
            gifticon.setIssuer(this.issuer);
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
               productName != null && 
               !productName.trim().isEmpty() && 
               productName.length() <= 200 &&
               amount != null && 
               amount.compareTo(new BigDecimal("1000")) >= 0 &&
               expiryDate != null && 
               expiryDate.isAfter(LocalDateTime.now());
    }

    /**
     * 검색 조건이 있는지 확인
     * 
     * @return 검색 조건 존재 여부
     */
    public boolean hasSearchConditions() {
        return keyword != null || 
               minAmount != null || 
               maxAmount != null || 
               (memberIds != null && !memberIds.isEmpty()) ||
               (brandNames != null && !brandNames.isEmpty()) ||
               (statuses != null && !statuses.isEmpty()) ||
               expiringInDays != null ||
               usableOnly != null ||
               transferableOnly != null ||
               startDate != null || 
               endDate != null;
    }

    /**
     * 금액 범위 유효성 검사
     * 
     * @return 금액 범위 유효성
     */
    public boolean isValidAmountRange() {
        if (minAmount != null && maxAmount != null) {
            return minAmount.compareTo(maxAmount) <= 0;
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
     * 양도 요청인지 확인
     * 
     * @return 양도 요청 여부
     */
    public boolean isTransferRequest() {
        return newMemberId != null && newMemberId > 0;
    }

    /**
     * 사용 요청인지 확인
     * 
     * @return 사용 요청 여부
     */
    public boolean isUseRequest() {
        return usedPlace != null && !usedPlace.trim().isEmpty();
    }

    /**
     * 기프티콘 정보 정규화 (앞뒤 공백 제거, 기본값 설정)
     * 
     * @return 정규화된 RequestDto
     */
    public GifticonRequestDto normalize() {
        if (this.productName != null) {
            this.productName = this.productName.trim();
        }
        if (this.brandName != null && this.brandName.trim().isEmpty()) {
            this.brandName = null;
        }
        if (this.usedPlace != null && this.usedPlace.trim().isEmpty()) {
            this.usedPlace = null;
        }
        if (this.keyword != null) {
            this.keyword = this.keyword.trim();
            if (this.keyword.isEmpty()) {
                this.keyword = null;
            }
        }
        if (this.status == null) {
            this.status = GifticonStatus.ACTIVE;
        }
        if (this.rewardAmount == null) {
            this.rewardAmount = BigDecimal.ZERO;
        }
        return this;
    }

    /**
     * 발행용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param productName 상품명
     * @param amount 금액
     * @param expiryDate 유효기간
     * @return GifticonRequestDto 객체
     */
    public static GifticonRequestDto issueRequest(Long memberId, String productName, 
                                                 BigDecimal amount, LocalDateTime expiryDate) {
        return GifticonRequestDto.builder()
                .memberId(memberId)
                .productName(productName)
                .amount(amount)
                .expiryDate(expiryDate)
                .status(GifticonStatus.ACTIVE)
                .build();
    }

    /**
     * 사용 요청용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param usedPlace 사용처
     * @return GifticonRequestDto 객체
     */
    public static GifticonRequestDto useRequest(Long memberId, String usedPlace) {
        return GifticonRequestDto.builder()
                .memberId(memberId)
                .usedPlace(usedPlace)
                .status(GifticonStatus.USED)
                .build();
    }

    /**
     * 양도 요청용 RequestDto 생성 팩토리 메소드
     * 
     * @param currentMemberId 현재 소유자 회원 ID
     * @param newMemberId 새로운 소유자 회원 ID
     * @return GifticonRequestDto 객체
     */
    public static GifticonRequestDto transferRequest(Long currentMemberId, Long newMemberId) {
        return GifticonRequestDto.builder()
                .memberId(currentMemberId)
                .newMemberId(newMemberId)
                .status(GifticonStatus.TRANSFERRED)
                .build();
    }

    /**
     * 검색용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param usableOnly 사용 가능한 것만 조회
     * @return GifticonRequestDto 객체
     */
    public static GifticonRequestDto searchRequest(Long memberId, Boolean usableOnly) {
        return GifticonRequestDto.builder()
                .memberId(memberId)
                .usableOnly(usableOnly)
                .page(0)
                .size(20)
                .sortBy("expiryDate")
                .sortDirection("ASC")
                .build();
    }

    /**
     * 만료 예정 기프티콘 조회용 RequestDto 생성 팩토리 메소드
     * 
     * @param memberId 회원 ID
     * @param expiringInDays 만료 예정 일수
     * @return GifticonRequestDto 객체
     */
    public static GifticonRequestDto expiringRequest(Long memberId, Integer expiringInDays) {
        return GifticonRequestDto.builder()
                .memberId(memberId)
                .expiringInDays(expiringInDays)
                .usableOnly(true)
                .page(0)
                .size(50)
                .sortBy("expiryDate")
                .sortDirection("ASC")
                .build();
    }

    /**
     * Entity로부터 RequestDto 생성
     * 
     * @param gifticon 기프티콘 엔티티
     * @return GifticonRequestDto 객체
     */
    public static GifticonRequestDto fromEntity(Gifticon gifticon) {
        if (gifticon == null) {
            return null;
        }

        return GifticonRequestDto.builder()
                .memberId(gifticon.getMemberId())
                .productName(gifticon.getProductName())
                .amount(gifticon.getAmount())
                .rewardAmount(gifticon.getRewardAmount())
                .gifticonCode(gifticon.getGifticonCode())
                .brandName(gifticon.getBrandName())
                .status(gifticon.getStatus())
                .expiryDate(gifticon.getExpiryDate())
                .usedPlace(gifticon.getUsedPlace())
                .barcodeImagePath(gifticon.getBarcodeImagePath())
                .gifticonImagePath(gifticon.getGifticonImagePath())
                .issuer(gifticon.getIssuer())
                .createdAt(gifticon.getCreatedAt())
                .updatedAt(gifticon.getUpdatedAt())
                .build();
    }

    @Override
    public String toString() {
        return String.format("GifticonRequestDto(memberId=%d, productName=%s, amount=%s, status=%s)", 
                           memberId, productName, amount, status);
    }
}