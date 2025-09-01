package com.dodam.product.dto.response;

import com.dodam.product.entity.Gifticon;
import com.dodam.product.entity.Gifticon.GifticonStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 기프티콘 응답 DTO
 * 기프티콘 조회 결과를 클라이언트에 전달할 때 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GifticonResponseDto {

    /**
     * 기프티콘 고유 번호
     */
    private Long gifticonId;

    /**
     * 회원 ID
     */
    private Long memberId;

    /**
     * 회원명 (마스킹 처리)
     */
    private String memberName;

    /**
     * 상품명
     */
    private String productName;

    /**
     * 기프티콘 금액
     */
    private BigDecimal amount;

    /**
     * 기프티콘 금액 텍스트 (포맷팅된 문자열)
     */
    private String amountText;

    /**
     * 보상 금액
     */
    private BigDecimal rewardAmount;

    /**
     * 보상 금액 텍스트
     */
    private String rewardAmountText;

    /**
     * 기프티콘 코드
     */
    private String gifticonCode;

    /**
     * 마스킹 처리된 기프티콘 코드 (앞 4자리만 표시)
     */
    private String maskedGifticonCode;

    /**
     * 브랜드명
     */
    private String brandName;

    /**
     * 기프티콘 상태
     */
    private GifticonStatus status;

    /**
     * 기프티콘 상태 텍스트
     */
    private String statusText;

    /**
     * 상태별 색상 코드 (UI용)
     */
    private String statusColor;

    /**
     * 사용 가능 여부
     */
    private Boolean usable;

    /**
     * 양도 가능 여부
     */
    private Boolean transferable;

    /**
     * 유효기간
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime expiryDate;

    /**
     * 유효기간 텍스트 (사용자 친화적)
     */
    private String expiryDateText;

    /**
     * 만료까지 남은 일수
     */
    private Long daysUntilExpiry;

    /**
     * 만료 임박 여부 (7일 이내)
     */
    private Boolean expiringSoon;

    /**
     * 사용일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime usedAt;

    /**
     * 사용일시 텍스트
     */
    private String usedAtText;

    /**
     * 사용처 정보
     */
    private String usedPlace;

    /**
     * 바코드 이미지 경로
     */
    private String barcodeImagePath;

    /**
     * 바코드 이미지 URL (전체 경로)
     */
    private String barcodeImageUrl;

    /**
     * 기프티콘 이미지 경로
     */
    private String gifticonImagePath;

    /**
     * 기프티콘 이미지 URL (전체 경로)
     */
    private String gifticonImageUrl;

    /**
     * 발행처 정보
     */
    private String issuer;

    /**
     * 기프티콘 생성일시 (발행일)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 기프티콘 수정일시
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    /**
     * 기프티콘 삭제 여부
     */
    private Boolean isDeleted;

    /**
     * 발행일 텍스트 (상대 시간)
     */
    private String issuedAtText;

    /**
     * 기프티콘 유형 (모바일, 실물 등)
     */
    private String gifticonType;

    /**
     * 사용 안내 메시지
     */
    private String usageGuide;

    /**
     * QR 코드 URL (모바일 사용시)
     */
    private String qrCodeUrl;

    /**
     * 기프티콘 카테고리 (음료, 디저트, 쇼핑 등)
     */
    private String category;

    /**
     * 할인율 (원래 가격 대비)
     */
    private Integer discountRate;

    /**
     * 인기도 점수
     */
    private Integer popularityScore;

    /**
     * Entity를 ResponseDto로 변환하는 메소드
     * 
     * @param gifticon 기프티콘 엔티티
     * @return GifticonResponseDto 객체
     */
    public static GifticonResponseDto fromEntity(Gifticon gifticon) {
        if (gifticon == null) {
            return null;
        }

        return GifticonResponseDto.builder()
                .gifticonId(gifticon.getGifticonId())
                .memberId(gifticon.getMemberId())
                .memberName(maskMemberName(gifticon.getMemberId()))
                .productName(gifticon.getProductName())
                .amount(gifticon.getAmount())
                .amountText(formatAmount(gifticon.getAmount()))
                .rewardAmount(gifticon.getRewardAmount())
                .rewardAmountText(formatAmount(gifticon.getRewardAmount()))
                .gifticonCode(gifticon.getGifticonCode())
                .maskedGifticonCode(maskGifticonCode(gifticon.getGifticonCode()))
                .brandName(gifticon.getBrandName())
                .status(gifticon.getStatus())
                .statusText(gifticon.getStatus() != null ? gifticon.getStatus().getDescription() : null)
                .statusColor(getStatusColor(gifticon.getStatus()))
                .usable(gifticon.isUsable())
                .transferable(gifticon.isTransferable())
                .expiryDate(gifticon.getExpiryDate())
                .expiryDateText(formatExpiryDate(gifticon.getExpiryDate()))
                .daysUntilExpiry(gifticon.getDaysUntilExpiry())
                .expiringSoon(gifticon.getDaysUntilExpiry() <= 7 && gifticon.getDaysUntilExpiry() > 0)
                .usedAt(gifticon.getUsedAt())
                .usedAtText(formatRelativeTime(gifticon.getUsedAt()))
                .usedPlace(gifticon.getUsedPlace())
                .barcodeImagePath(gifticon.getBarcodeImagePath())
                .barcodeImageUrl(buildImageUrl(gifticon.getBarcodeImagePath(), "barcodes"))
                .gifticonImagePath(gifticon.getGifticonImagePath())
                .gifticonImageUrl(buildImageUrl(gifticon.getGifticonImagePath(), "gifticons"))
                .issuer(gifticon.getIssuer())
                .createdAt(gifticon.getCreatedAt())
                .updatedAt(gifticon.getUpdatedAt())
                .isDeleted(gifticon.isDeleted())
                .issuedAtText(formatRelativeTime(gifticon.getCreatedAt()))
                .gifticonType(determineGifticonType(gifticon))
                .usageGuide(generateUsageGuide(gifticon))
                .qrCodeUrl(buildQrCodeUrl(gifticon.getGifticonCode()))
                .category(categorizeByBrand(gifticon.getBrandName()))
                .popularityScore(calculatePopularityScore(gifticon))
                .build();
    }

    /**
     * Entity를 ResponseDto로 변환하는 메소드 (사용자별 정보 포함)
     * 
     * @param gifticon 기프티콘 엔티티
     * @param currentMemberId 현재 로그인된 사용자 ID
     * @return GifticonResponseDto 객체
     */
    public static GifticonResponseDto fromEntity(Gifticon gifticon, Long currentMemberId) {
        GifticonResponseDto dto = fromEntity(gifticon);
        
        if (dto != null && currentMemberId != null) {
            // 현재 사용자 소유 기프티콘인 경우 실제 정보 표시
            if (gifticon.isOwnedBy(currentMemberId)) {
                dto.setMemberName("내 기프티콘");
                dto.setGifticonCode(gifticon.getGifticonCode()); // 실제 코드 표시
            }
        }
        
        return dto;
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드
     * 
     * @param gifticons 기프티콘 엔티티 목록
     * @return GifticonResponseDto 목록
     */
    public static List<GifticonResponseDto> fromEntityList(List<Gifticon> gifticons) {
        if (gifticons == null || gifticons.isEmpty()) {
            return List.of();
        }

        return gifticons.stream()
                .map(GifticonResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Entity 목록을 ResponseDto 목록으로 변환하는 메소드 (사용자별 정보 포함)
     * 
     * @param gifticons 기프티콘 엔티티 목록
     * @param currentMemberId 현재 로그인된 사용자 ID
     * @return GifticonResponseDto 목록
     */
    public static List<GifticonResponseDto> fromEntityList(List<Gifticon> gifticons, Long currentMemberId) {
        if (gifticons == null || gifticons.isEmpty()) {
            return List.of();
        }

        return gifticons.stream()
                .map(gifticon -> fromEntity(gifticon, currentMemberId))
                .collect(Collectors.toList());
    }

    /**
     * 목록 페이지용 간소화된 정보 포함 ResponseDto 생성
     * 
     * @param gifticon 기프티콘 엔티티
     * @return 목록용 GifticonResponseDto 객체
     */
    public static GifticonResponseDto forList(Gifticon gifticon) {
        if (gifticon == null) {
            return null;
        }

        return GifticonResponseDto.builder()
                .gifticonId(gifticon.getGifticonId())
                .productName(gifticon.getProductName())
                .amount(gifticon.getAmount())
                .amountText(formatAmount(gifticon.getAmount()))
                .brandName(gifticon.getBrandName())
                .status(gifticon.getStatus())
                .statusText(gifticon.getStatus() != null ? gifticon.getStatus().getDescription() : null)
                .statusColor(getStatusColor(gifticon.getStatus()))
                .usable(gifticon.isUsable())
                .expiryDate(gifticon.getExpiryDate())
                .expiryDateText(formatExpiryDate(gifticon.getExpiryDate()))
                .daysUntilExpiry(gifticon.getDaysUntilExpiry())
                .expiringSoon(gifticon.getDaysUntilExpiry() <= 7 && gifticon.getDaysUntilExpiry() > 0)
                .gifticonImageUrl(buildImageUrl(gifticon.getGifticonImagePath(), "gifticons"))
                .category(categorizeByBrand(gifticon.getBrandName()))
                .build();
    }

    /**
     * 카드 표시용 간단한 ResponseDto 생성
     * 
     * @param gifticon 기프티콘 엔티티
     * @return 카드용 GifticonResponseDto 객체
     */
    public static GifticonResponseDto forCard(Gifticon gifticon) {
        if (gifticon == null) {
            return null;
        }

        return GifticonResponseDto.builder()
                .gifticonId(gifticon.getGifticonId())
                .productName(gifticon.getProductName())
                .amountText(formatAmount(gifticon.getAmount()))
                .brandName(gifticon.getBrandName())
                .statusText(gifticon.getStatus() != null ? gifticon.getStatus().getDescription() : null)
                .statusColor(getStatusColor(gifticon.getStatus()))
                .usable(gifticon.isUsable())
                .daysUntilExpiry(gifticon.getDaysUntilExpiry())
                .expiringSoon(gifticon.getDaysUntilExpiry() <= 7 && gifticon.getDaysUntilExpiry() > 0)
                .gifticonImageUrl(buildImageUrl(gifticon.getGifticonImagePath(), "gifticons"))
                .category(categorizeByBrand(gifticon.getBrandName()))
                .build();
    }

    /**
     * 관리자용 상세 정보 포함 ResponseDto 생성
     * 
     * @param gifticon 기프티콘 엔티티
     * @return 관리자용 GifticonResponseDto 객체
     */
    public static GifticonResponseDto forAdmin(Gifticon gifticon) {
        GifticonResponseDto dto = fromEntity(gifticon);
        
        if (dto != null) {
            // 관리자용 추가 정보
            dto.setMemberName(String.format("회원 #%d", gifticon.getMemberId()));
            dto.setGifticonCode(gifticon.getGifticonCode()); // 실제 코드 표시
            
            if (gifticon.isDeleted()) {
                dto.setStatusText(String.format("삭제됨 (%s)", 
                    gifticon.getDeletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
                dto.setStatusColor("#FF6B6B"); // 빨간색
            }
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
     * 기프티콘 코드를 마스킹 처리
     * 
     * @param gifticonCode 기프티콘 코드
     * @return 마스킹된 기프티콘 코드
     */
    private static String maskGifticonCode(String gifticonCode) {
        if (gifticonCode == null || gifticonCode.length() < 8) {
            return "****";
        }
        return gifticonCode.substring(0, 4) + "****" + 
               gifticonCode.substring(Math.max(4, gifticonCode.length() - 4));
    }

    /**
     * 금액을 포맷팅된 문자열로 변환
     * 
     * @param amount 금액
     * @return 포맷팅된 금액 문자열
     */
    private static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return String.format("%,d원", amount.intValue());
    }

    /**
     * 유효기간을 사용자 친화적 텍스트로 변환
     * 
     * @param expiryDate 유효기간
     * @return 유효기간 텍스트
     */
    private static String formatExpiryDate(LocalDateTime expiryDate) {
        if (expiryDate == null) {
            return "무제한";
        }
        
        return expiryDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시까지"));
    }

    /**
     * 상태별 색상 코드 반환
     * 
     * @param status 기프티콘 상태
     * @return 색상 코드
     */
    private static String getStatusColor(GifticonStatus status) {
        if (status == null) {
            return "#9E9E9E"; // 회색
        }
        
        return switch (status) {
            case ACTIVE -> "#4CAF50"; // 녹색
            case USED -> "#2196F3"; // 파란색
            case EXPIRED -> "#FF5722"; // 주황색
            case TRANSFERRED -> "#9C27B0"; // 보라색
            case REFUNDED -> "#FF9800"; // 주황색
            case SUSPENDED -> "#F44336"; // 빨간색
        };
    }

    /**
     * 이미지 URL 생성
     * 
     * @param imagePath 이미지 경로
     * @param category 카테고리 (barcodes, gifticons)
     * @return 이미지 URL
     */
    private static String buildImageUrl(String imagePath, String category) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return String.format("/images/%s/default.jpg", category);
        }
        return String.format("/images/%s/%s", category, imagePath);
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
        long days = java.time.temporal.ChronoUnit.DAYS.between(dateTime, now);
        
        if (days == 0) {
            long hours = java.time.temporal.ChronoUnit.HOURS.between(dateTime, now);
            if (hours == 0) {
                long minutes = java.time.temporal.ChronoUnit.MINUTES.between(dateTime, now);
                return String.format("%d분 전", Math.max(1, minutes));
            }
            return String.format("%d시간 전", hours);
        } else if (days < 30) {
            return String.format("%d일 전", days);
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }

    /**
     * 기프티콘 유형 결정
     * 
     * @param gifticon 기프티콘 엔티티
     * @return 기프티콘 유형
     */
    private static String determineGifticonType(Gifticon gifticon) {
        if (gifticon.getBarcodeImagePath() != null) {
            return "모바일";
        }
        return "일반";
    }

    /**
     * 사용 안내 메시지 생성
     * 
     * @param gifticon 기프티콘 엔티티
     * @return 사용 안내 메시지
     */
    private static String generateUsageGuide(Gifticon gifticon) {
        if (!gifticon.isUsable()) {
            return "사용할 수 없는 기프티콘입니다.";
        }
        
        if (gifticon.getDaysUntilExpiry() <= 7) {
            return String.format("유효기간이 %d일 남았습니다. 빠른 사용을 권장합니다.", gifticon.getDaysUntilExpiry());
        }
        
        return "매장에서 바코드를 제시하거나 온라인에서 코드를 입력하여 사용하세요.";
    }

    /**
     * QR 코드 URL 생성
     * 
     * @param gifticonCode 기프티콘 코드
     * @return QR 코드 URL
     */
    private static String buildQrCodeUrl(String gifticonCode) {
        if (gifticonCode == null) {
            return null;
        }
        return String.format("/api/gifticons/qrcode/%s", gifticonCode);
    }

    /**
     * 브랜드명으로 카테고리 분류
     * 
     * @param brandName 브랜드명
     * @return 카테고리명
     */
    private static String categorizeByBrand(String brandName) {
        if (brandName == null) {
            return "기타";
        }
        
        String lower = brandName.toLowerCase();
        if (lower.contains("스타벅스") || lower.contains("커피") || lower.contains("카페")) {
            return "음료/카페";
        } else if (lower.contains("베스킨") || lower.contains("아이스크림") || lower.contains("디저트")) {
            return "디저트";
        } else if (lower.contains("맥도날드") || lower.contains("버거") || lower.contains("치킨")) {
            return "음식";
        } else if (lower.contains("편의점") || lower.contains("마트")) {
            return "편의점/마트";
        } else {
            return "쇼핑/기타";
        }
    }

    /**
     * 인기도 점수 계산
     * 
     * @param gifticon 기프티콘 엔티티
     * @return 인기도 점수
     */
    private static Integer calculatePopularityScore(Gifticon gifticon) {
        int score = 50; // 기본 점수
        
        // 브랜드 인지도
        if (gifticon.getBrandName() != null) {
            String brand = gifticon.getBrandName().toLowerCase();
            if (brand.contains("스타벅스") || brand.contains("맥도날드")) {
                score += 30;
            } else if (brand.contains("베스킨") || brand.contains("롯데")) {
                score += 20;
            }
        }
        
        // 금액대
        if (gifticon.getAmount() != null) {
            int amount = gifticon.getAmount().intValue();
            if (amount >= 50000) {
                score += 20;
            } else if (amount >= 20000) {
                score += 15;
            } else if (amount >= 10000) {
                score += 10;
            }
        }
        
        // 유효기간
        if (gifticon.getDaysUntilExpiry() > 30) {
            score += 10;
        }
        
        return Math.min(100, score);
    }

    /**
     * 기프티콘 요약 정보 텍스트 생성
     * 
     * @return 요약 정보 텍스트
     */
    public String getSummaryText() {
        StringBuilder summary = new StringBuilder();
        
        if (brandName != null) {
            summary.append(brandName).append(" ");
        }
        
        if (amountText != null) {
            summary.append(amountText);
        }
        
        if (daysUntilExpiry != null && daysUntilExpiry > 0) {
            summary.append(String.format(" | %d일 남음", daysUntilExpiry));
        }
        
        if (Boolean.TRUE.equals(expiringSoon)) {
            summary.append(" [임박]");
        }
        
        if (statusText != null) {
            summary.append(String.format(" | %s", statusText));
        }
        
        return summary.toString();
    }

    /**
     * 할인 정보 설정
     * 
     * @param discountRate 할인율
     */
    public void setDiscountRate(Integer discountRate) {
        this.discountRate = discountRate;
    }

    /**
     * 할인 기프티콘인지 확인
     * 
     * @return 할인 여부
     */
    public boolean isDiscounted() {
        return discountRate != null && discountRate > 0;
    }

    @Override
    public String toString() {
        return String.format("GifticonResponseDto(id=%d, product=%s, amount=%s, status=%s)", 
                           gifticonId, productName, amountText, statusText);
    }
}