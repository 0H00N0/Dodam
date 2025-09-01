package com.dodam.product.service;

import com.dodam.product.dto.request.GifticonRequestDto;
import com.dodam.product.dto.response.GifticonResponseDto;
import com.dodam.product.dto.statistics.GifticonStatisticsDto;
import com.dodam.product.entity.Gifticon;
import com.dodam.product.entity.Gifticon.GifticonStatus;
import com.dodam.product.entity.Gifticon.TransactionType;
import com.dodam.product.exception.*;
import com.dodam.product.repository.GifticonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 기프티콘 비즈니스 로직을 처리하는 Service 클래스
 * 기프티콘 발행, 사용, 양도, 만료 관리 등의 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 기프티콘 발행 및 관리
 * - 사용, 양도, 만료 처리
 * - 회원별/브랜드별 기프티콘 관리
 * - 만료 예정 알림 및 자동 만료 처리
 * - 통계 및 사용 패턴 분석
 * - 검색 및 필터링
 * 
 * @author Dodam Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Validated
public class GifticonService {

    private final GifticonRepository gifticonRepository;

    // 만료 예정 알림 기준 일수 (기본: 7일)
    private static final int EXPIRY_ALERT_DAYS = 7;
    
    // 기프티콘 코드 생성 접두사
    private static final String GIFTICON_CODE_PREFIX = "GIFT";

    // ========================== 기본 CRUD 메소드 ==========================

    /**
     * 기프티콘 발행 (생성)
     * 
     * @param requestDto 기프티콘 생성 요청 정보
     * @return 생성된 기프티콘 정보
     * @throws ValidationException 유효성 검증 실패
     * @throws DuplicateResourceException 기프티콘 코드 중복
     */
    @Transactional
    @CacheEvict(value = {"gifticons", "gifticonStats", "memberGifticons"}, allEntries = true)
    public GifticonResponseDto issueGifticon(@Valid GifticonRequestDto requestDto) {
        
        log.info("기프티콘 발행 시작: {}", requestDto);

        // 1. 요청 데이터 검증 및 정규화
        requestDto.normalize();
        if (!requestDto.isValid()) {
            throw new ValidationException("기프티콘 정보가 유효하지 않습니다.");
        }

        // 2. 기프티콘 코드 생성 (중복 확인)
        String gifticonCode = generateUniqueGifticonCode();

        // 3. 유효기간 검증
        validateExpiryDate(requestDto.getExpiryDate());

        // 4. 엔티티 생성
        Gifticon gifticon = requestDto.toEntity();
        gifticon.setGifticonCode(gifticonCode);
        gifticon.setTransactionType(TransactionType.ISSUED);
        gifticon.setStatus(GifticonStatus.ACTIVE);

        // 5. 저장
        gifticon = gifticonRepository.save(gifticon);

        log.info("기프티콘 발행 완료: ID={}, 코드={}", gifticon.getGifticonId(), gifticonCode);
        
        return GifticonResponseDto.fromEntity(gifticon);
    }

    /**
     * 기프티콘 ID로 단건 조회
     * 
     * @param gifticonId 기프티콘 ID
     * @return 기프티콘 정보
     * @throws ResourceNotFoundException 기프티콘을 찾을 수 없는 경우
     */
    @Cacheable(value = "gifticons", key = "#gifticonId")
    public GifticonResponseDto getGifticonById(@NotNull @Positive Long gifticonId) {
        
        log.debug("기프티콘 단건 조회: {}", gifticonId);
        
        Gifticon gifticon = gifticonRepository.findById(gifticonId)
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("기프티콘을 찾을 수 없습니다: " + gifticonId));
        
        return GifticonResponseDto.fromEntity(gifticon);
    }

    /**
     * 기프티콘 코드로 조회
     * 
     * @param gifticonCode 기프티콘 코드
     * @return 기프티콘 정보
     * @throws ResourceNotFoundException 기프티콘을 찾을 수 없는 경우
     */
    @Cacheable(value = "gifticons", key = "'code:' + #gifticonCode")
    public GifticonResponseDto getGifticonByCode(@NotBlank String gifticonCode) {
        
        log.debug("기프티콘 코드 조회: {}", gifticonCode);
        
        Gifticon gifticon = gifticonRepository.findByGifticonCode(gifticonCode)
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("기프티콘을 찾을 수 없습니다: " + gifticonCode));
        
        return GifticonResponseDto.fromEntity(gifticon);
    }

    /**
     * 기프티콘 정보 수정
     * 
     * @param gifticonId 기프티콘 ID
     * @param requestDto 수정 요청 정보
     * @return 수정된 기프티콘 정보
     * @throws ResourceNotFoundException 기프티콘을 찾을 수 없는 경우
     * @throws BusinessException 비즈니스 규칙 위반
     */
    @Transactional
    @CacheEvict(value = {"gifticons", "gifticonStats", "memberGifticons"}, allEntries = true)
    public GifticonResponseDto updateGifticon(@NotNull @Positive Long gifticonId, 
                                             @Valid GifticonRequestDto requestDto) {
        
        log.info("기프티콘 수정 시작: ID={}", gifticonId);

        // 1. 기존 기프티콘 조회
        Gifticon gifticon = gifticonRepository.findById(gifticonId)
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("기프티콘을 찾을 수 없습니다: " + gifticonId));

        // 2. 수정 가능 상태인지 확인
        validateUpdatable(gifticon);

        // 3. 요청 데이터 검증
        requestDto.normalize();
        if (!requestDto.isValid()) {
            throw new ValidationException("기프티콘 정보가 유효하지 않습니다.");
        }

        // 4. 유효기간 검증 (변경되는 경우)
        if (requestDto.getExpiryDate() != null && !requestDto.getExpiryDate().equals(gifticon.getExpiryDate())) {
            validateExpiryDate(requestDto.getExpiryDate());
        }

        // 5. 엔티티 업데이트
        requestDto.updateEntity(gifticon);

        // 6. 저장
        gifticon = gifticonRepository.save(gifticon);

        log.info("기프티콘 수정 완료: ID={}", gifticonId);
        
        return GifticonResponseDto.fromEntity(gifticon);
    }

    /**
     * 기프티콘 삭제 (소프트 삭제)
     * 
     * @param gifticonId 기프티콘 ID
     * @throws ResourceNotFoundException 기프티콘을 찾을 수 없는 경우
     * @throws BusinessException 삭제할 수 없는 상태
     */
    @Transactional
    @CacheEvict(value = {"gifticons", "gifticonStats", "memberGifticons"}, allEntries = true)
    public void deleteGifticon(@NotNull @Positive Long gifticonId) {
        
        log.info("기프티콘 삭제 시작: {}", gifticonId);

        // 1. 기프티콘 조회
        Gifticon gifticon = gifticonRepository.findById(gifticonId)
                .orElseThrow(() -> new ResourceNotFoundException("기프티콘을 찾을 수 없습니다: " + gifticonId));

        if (gifticon.isDeleted()) {
            throw new BusinessException("이미 삭제된 기프티콘입니다.");
        }

        // 2. 삭제 가능 여부 확인
        if (gifticon.getStatus() == GifticonStatus.USED) {
            throw new BusinessException("사용된 기프티콘은 삭제할 수 없습니다.");
        }

        // 3. 소프트 삭제 실행
        gifticon.delete();
        gifticonRepository.save(gifticon);

        log.info("기프티콘 삭제 완료: {}", gifticonId);
    }

    // ========================== 기프티콘 상태 관리 메소드 ==========================

    /**
     * 기프티콘 사용 처리
     * 
     * @param gifticonId 기프티콘 ID
     * @param memberId 사용하는 회원 ID
     * @param usedPlace 사용처
     * @return 사용 처리된 기프티콘 정보
     * @throws ResourceNotFoundException 기프티콘을 찾을 수 없는 경우
     * @throws BusinessException 사용할 수 없는 상태
     * @throws UnauthorizedException 권한 없는 사용자
     */
    @Transactional
    @CacheEvict(value = {"gifticons", "gifticonStats", "memberGifticons"}, allEntries = true)
    public GifticonResponseDto useGifticon(@NotNull @Positive Long gifticonId, 
                                          @NotNull @Positive Long memberId,
                                          @NotBlank String usedPlace) {
        
        log.info("기프티콘 사용 시작: ID={}, 회원={}, 사용처={}", gifticonId, memberId, usedPlace);

        // 1. 기프티콘 조회
        Gifticon gifticon = gifticonRepository.findById(gifticonId)
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("기프티콘을 찾을 수 없습니다: " + gifticonId));

        // 2. 사용자 권한 확인
        if (!gifticon.isOwnedBy(memberId)) {
            throw new UnauthorizedException("기프티콘 사용 권한이 없습니다.");
        }

        // 3. 사용 가능 상태 확인
        if (!gifticon.isUsable()) {
            if (gifticon.isExpired()) {
                throw new ExpiredException("만료된 기프티콘입니다.");
            } else if (gifticon.getStatus() == GifticonStatus.USED) {
                throw new BusinessException("이미 사용된 기프티콘입니다.");
            } else {
                throw new BusinessException("사용할 수 없는 기프티콘입니다.");
            }
        }

        // 4. 사용 처리
        gifticon.use(usedPlace);
        gifticon = gifticonRepository.save(gifticon);

        log.info("기프티콘 사용 완료: ID={}, 사용처={}", gifticonId, usedPlace);
        
        return GifticonResponseDto.fromEntity(gifticon);
    }

    /**
     * 기프티콘 양도 처리
     * 
     * @param gifticonId 기프티콘 ID
     * @param fromMemberId 양도하는 회원 ID
     * @param toMemberId 양도받는 회원 ID
     * @return 양도 처리된 기프티콘 정보
     * @throws ResourceNotFoundException 기프티콘을 찾을 수 없는 경우
     * @throws BusinessException 양도할 수 없는 상태
     * @throws UnauthorizedException 권한 없는 사용자
     * @throws ValidationException 양도받는 회원이 같은 경우
     */
    @Transactional
    @CacheEvict(value = {"gifticons", "gifticonStats", "memberGifticons"}, allEntries = true)
    public GifticonResponseDto transferGifticon(@NotNull @Positive Long gifticonId, 
                                               @NotNull @Positive Long fromMemberId,
                                               @NotNull @Positive Long toMemberId) {
        
        log.info("기프티콘 양도 시작: ID={}, {}→{}", gifticonId, fromMemberId, toMemberId);

        // 1. 유효성 검증
        if (fromMemberId.equals(toMemberId)) {
            throw new ValidationException("자신에게 양도할 수 없습니다.");
        }

        // 2. 기프티콘 조회
        Gifticon gifticon = gifticonRepository.findById(gifticonId)
                .filter(g -> !g.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("기프티콘을 찾을 수 없습니다: " + gifticonId));

        // 3. 양도자 권한 확인
        if (!gifticon.isOwnedBy(fromMemberId)) {
            throw new UnauthorizedException("기프티콘 양도 권한이 없습니다.");
        }

        // 4. 양도 가능 상태 확인
        if (!gifticon.isTransferable()) {
            if (gifticon.isExpired()) {
                throw new ExpiredException("만료된 기프티콘은 양도할 수 없습니다.");
            } else {
                throw new BusinessException("양도할 수 없는 기프티콘입니다.");
            }
        }

        // 5. 양도 처리
        gifticon.transfer(toMemberId);
        gifticon = gifticonRepository.save(gifticon);

        log.info("기프티콘 양도 완료: ID={}, {}→{}", gifticonId, fromMemberId, toMemberId);
        
        return GifticonResponseDto.fromEntity(gifticon);
    }

    /**
     * 만료된 기프티콘 일괄 처리
     * 
     * @return 처리된 기프티콘 수
     */
    @Transactional
    @CacheEvict(value = {"gifticons", "gifticonStats", "memberGifticons"}, allEntries = true)
    public int expireOutdatedGifticons() {
        
        log.info("만료된 기프티콘 일괄 처리 시작");

        LocalDateTime now = LocalDateTime.now();
        int expiredCount = gifticonRepository.expireOutdatedGifticons(now);

        log.info("만료된 기프티콘 일괄 처리 완료: {}개", expiredCount);
        
        return expiredCount;
    }

    // ========================== 검색 및 조회 메소드 ==========================

    /**
     * 전체 활성 기프티콘 목록 조회 (페이징)
     * 
     * @param pageable 페이징 정보
     * @return 기프티콘 목록 페이지
     */
    public Page<GifticonResponseDto> getAllGifticons(Pageable pageable) {
        
        log.debug("전체 기프티콘 목록 조회: {}", pageable);
        
        return gifticonRepository.findByDeletedAtIsNull(pageable)
                .map(GifticonResponseDto::fromEntity);
    }

    /**
     * 회원별 기프티콘 목록 조회
     * 
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 해당 회원의 기프티콘 목록
     */
    @Cacheable(value = "memberGifticons", key = "#memberId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<GifticonResponseDto> getMemberGifticons(@NotNull @Positive Long memberId, 
                                                        Pageable pageable) {
        
        log.debug("회원별 기프티콘 조회: 회원={}, {}", memberId, pageable);
        
        return gifticonRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable)
                .map(GifticonResponseDto::fromEntity);
    }

    /**
     * 회원의 사용 가능한 기프티콘 조회
     * 
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 사용 가능한 기프티콘 목록
     */
    @Cacheable(value = "memberGifticons", key = "'usable:' + #memberId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<GifticonResponseDto> getUsableGifticons(@NotNull @Positive Long memberId, 
                                                        Pageable pageable) {
        
        log.debug("사용 가능 기프티콘 조회: 회원={}, {}", memberId, pageable);
        
        LocalDateTime now = LocalDateTime.now();
        
        return gifticonRepository.findUsableGifticons(memberId, now, pageable)
                .map(GifticonResponseDto::fromEntity);
    }

    /**
     * 상태별 기프티콘 조회
     * 
     * @param status 기프티콘 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 기프티콘 목록
     */
    public Page<GifticonResponseDto> getGifticonsByStatus(GifticonStatus status, 
                                                         Pageable pageable) {
        
        log.debug("상태별 기프티콘 조회: 상태={}, {}", status, pageable);
        
        return gifticonRepository.findByStatusAndDeletedAtIsNull(status, pageable)
                .map(GifticonResponseDto::fromEntity);
    }

    /**
     * 브랜드별 기프티콘 조회
     * 
     * @param brandName 브랜드명
     * @param pageable 페이징 정보
     * @return 해당 브랜드의 기프티콘 목록
     */
    public Page<GifticonResponseDto> getGifticonsByBrand(@NotBlank String brandName, 
                                                        Pageable pageable) {
        
        log.debug("브랜드별 기프티콘 조회: 브랜드={}, {}", brandName, pageable);
        
        return gifticonRepository.findByBrandNameAndDeletedAtIsNull(brandName, pageable)
                .map(GifticonResponseDto::fromEntity);
    }

    /**
     * 키워드 검색 (브랜드명, 상품명)
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색 결과 목록
     */
    public Page<GifticonResponseDto> searchGifticons(@NotBlank String keyword, 
                                                    Pageable pageable) {
        
        log.debug("기프티콘 검색: 키워드={}, {}", keyword, pageable);
        
        return gifticonRepository.searchGifticons(keyword, pageable)
                .map(GifticonResponseDto::fromEntity);
    }

    /**
     * 고급 검색 (복합 조건)
     * 
     * @param requestDto 검색 조건
     * @param pageable 페이징 정보
     * @return 검색 결과 목록
     */
    public Page<GifticonResponseDto> advancedSearch(@Valid GifticonRequestDto requestDto, 
                                                   Pageable pageable) {
        
        log.debug("고급 검색: 조건={}, {}", requestDto, pageable);
        
        return gifticonRepository.advancedSearch(
                requestDto.getMemberId(),
                requestDto.getStatus(),
                requestDto.getTransactionType(),
                requestDto.getMinAmount(),
                requestDto.getMaxAmount(),
                requestDto.getBrandName(),
                pageable
        ).map(GifticonResponseDto::fromEntity);
    }

    // ========================== 만료 관리 메소드 ==========================

    /**
     * 만료 예정 기프티콘 조회
     * 
     * @param days 몇 일 후까지 (기본: 7일)
     * @param pageable 페이징 정보
     * @return 만료 예정 기프티콘 목록
     */
    public Page<GifticonResponseDto> getExpiringSoonGifticons(int days, Pageable pageable) {
        
        log.debug("만료 예정 기프티콘 조회: {}일 이내, {}", days, pageable);
        
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
        
        return gifticonRepository.findExpiringSoon(expiryDate, pageable)
                .map(GifticonResponseDto::fromEntity);
    }

    /**
     * 회원의 만료 예정 기프티콘 조회
     * 
     * @param memberId 회원 ID
     * @param days 몇 일 후까지 (기본: 7일)
     * @return 만료 예정 기프티콘 목록
     */
    @Cacheable(value = "memberGifticons", key = "'expiring:' + #memberId + ':' + #days")
    public List<GifticonResponseDto> getMemberExpiringSoon(@NotNull @Positive Long memberId, 
                                                          int days) {
        
        log.debug("회원 만료 예정 기프티콘 조회: 회원={}, {}일", memberId, days);
        
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
        
        List<Gifticon> gifticons = gifticonRepository.findMemberExpiringSoon(memberId, expiryDate);
        
        return gifticons.stream()
                .map(GifticonResponseDto::fromEntity)
                .toList();
    }

    /**
     * 만료 알림이 필요한 기프티콘 목록 조회
     * 
     * @param alertDays 알림 기준 일수
     * @return 알림 대상 기프티콘 목록
     */
    public List<GifticonResponseDto> getGifticonsNeedingAlert(int alertDays) {
        
        log.debug("만료 알림 대상 기프티콘 조회: {}일", alertDays);
        
        LocalDateTime alertEndDate = LocalDateTime.now().plusDays(alertDays);
        
        List<Gifticon> gifticons = gifticonRepository.findGifticonsNeedingExpiryAlert(alertEndDate);
        
        return gifticons.stream()
                .map(GifticonResponseDto::fromEntity)
                .toList();
    }

    // ========================== 통계 및 분석 메소드 ==========================

    /**
     * 기프티콘 전체 통계 조회
     * 
     * @return 전체 통계 정보
     */
    @Cacheable(value = "gifticonStats", key = "'overall'")
    public GifticonStatisticsDto.OverallStats getOverallStatistics() {
        
        log.debug("전체 통계 조회");
        
        long totalCount = gifticonRepository.countActiveGifticons();
        long activeCount = gifticonRepository.countByStatusAndDeletedAtIsNull(GifticonStatus.ACTIVE);
        long usedCount = gifticonRepository.countByStatusAndDeletedAtIsNull(GifticonStatus.USED);
        long expiredCount = gifticonRepository.countByStatusAndDeletedAtIsNull(GifticonStatus.EXPIRED);
        
        Object[] amountStats = gifticonRepository.getAmountStats();
        BigDecimal totalAmount = amountStats[0] != null ? (BigDecimal) amountStats[0] : BigDecimal.ZERO;
        BigDecimal avgAmount = amountStats[1] != null ? (BigDecimal) amountStats[1] : BigDecimal.ZERO;
        BigDecimal maxAmount = amountStats[2] != null ? (BigDecimal) amountStats[2] : BigDecimal.ZERO;
        BigDecimal minAmount = amountStats[3] != null ? (BigDecimal) amountStats[3] : BigDecimal.ZERO;
        
        // 만료 예정 기프티콘 수
        LocalDateTime alertDate = LocalDateTime.now().plusDays(EXPIRY_ALERT_DAYS);
        long expiringSoonCount = gifticonRepository.countExpiringSoon(alertDate);
        
        return GifticonStatisticsDto.OverallStats.builder()
                .totalCount(totalCount)
                .activeCount(activeCount)
                .usedCount(usedCount)
                .expiredCount(expiredCount)
                .expiringSoonCount(expiringSoonCount)
                .totalAmount(totalAmount)
                .averageAmount(avgAmount)
                .maxAmount(maxAmount)
                .minAmount(minAmount)
                .usageRate(totalCount > 0 ? (double) usedCount / totalCount * 100 : 0.0)
                .build();
    }

    /**
     * 상태별 기프티콘 통계 조회
     * 
     * @return 상태별 통계 목록
     */
    @Cacheable(value = "gifticonStats", key = "'status'")
    public List<GifticonStatisticsDto.StatusStats> getStatusStatistics() {
        
        log.debug("상태별 통계 조회");
        
        List<Object[]> stats = gifticonRepository.getStatusStats();
        
        return stats.stream()
                .map(row -> GifticonStatisticsDto.StatusStats.builder()
                        .status(((GifticonStatus) row[0]).name())  // enum을 String으로 변환
                        .count((Long) row[1])
                        .build())
                .toList();
    }

    /**
     * 브랜드별 기프티콘 통계 조회
     * 
     * @return 브랜드별 통계 목록
     */
    @Cacheable(value = "gifticonStats", key = "'brand'")
    public List<GifticonStatisticsDto.BrandStats> getBrandStatistics() {
        
        log.debug("브랜드별 통계 조회");
        
        List<Object[]> stats = gifticonRepository.getBrandStats();
        
        return stats.stream()
                .map(row -> GifticonStatisticsDto.BrandStats.builder()
                        .brandName((String) row[0])
                        .count((Long) row[1])
                        .totalAmount((BigDecimal) row[2])
                        .build())
                .toList();
    }

    /**
     * 회원별 기프티콘 통계 조회
     * 
     * @param pageable 페이징 정보
     * @return 회원별 통계 목록
     */
    @Cacheable(value = "gifticonStats", key = "'member:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<GifticonStatisticsDto.MemberStats> getMemberStatistics(Pageable pageable) {
        
        log.debug("회원별 통계 조회: {}", pageable);
        
        Page<Object[]> stats = gifticonRepository.getMemberGifticonStats(pageable);
        
        return stats.map(row -> GifticonStatisticsDto.MemberStats.builder()
                .memberId((Long) row[0])
                .count((Long) row[1])
                .totalAmount((BigDecimal) row[2])
                .build());
    }

    /**
     * 사용처별 통계 조회
     * 
     * @return 사용처별 통계 목록
     */
    @Cacheable(value = "gifticonStats", key = "'usedPlace'")
    public List<GifticonStatisticsDto.UsedPlaceStats> getUsedPlaceStatistics() {
        
        log.debug("사용처별 통계 조회");
        
        List<Object[]> stats = gifticonRepository.getUsedPlaceStats();
        
        return stats.stream()
                .map(row -> GifticonStatisticsDto.UsedPlaceStats.builder()
                        .usedPlace((String) row[0])
                        .count((Long) row[1])
                        .build())
                .toList();
    }

    /**
     * 브랜드별 사용률 통계 조회
     * 
     * @return 브랜드별 사용률 통계 목록
     */
    @Cacheable(value = "gifticonStats", key = "'brandUsage'")
    public List<GifticonStatisticsDto.BrandUsageStats> getBrandUsageStatistics() {
        
        log.debug("브랜드별 사용률 통계 조회");
        
        List<Object[]> stats = gifticonRepository.getBrandUsageRateStats();
        
        return stats.stream()
                .map(row -> GifticonStatisticsDto.BrandUsageStats.builder()
                        .brandName((String) row[0])
                        .totalCount((Long) row[1])
                        .usedCount((Long) row[2])
                        .usageRate(((Number) row[3]).doubleValue())
                        .build())
                .toList();
    }

    /**
     * 특정 회원의 기프티콘 가치 조회
     * 
     * @param memberId 회원 ID
     * @return 기프티콘 가치 정보
     */
    @Cacheable(value = "memberGifticons", key = "'value:' + #memberId")
    public GifticonStatisticsDto.MemberValueStats getMemberGifticonValue(@NotNull @Positive Long memberId) {
        
        log.debug("회원 기프티콘 가치 조회: {}", memberId);
        
        Object[] stats = gifticonRepository.getMemberGifticonValue(memberId);
        
        BigDecimal activeAmount = stats[0] != null ? (BigDecimal) stats[0] : BigDecimal.ZERO;
        BigDecimal usedAmount = stats[1] != null ? (BigDecimal) stats[1] : BigDecimal.ZERO;
        
        return GifticonStatisticsDto.MemberValueStats.builder()
                .memberId(memberId)
                .activeAmount(activeAmount)
                .usedAmount(usedAmount)
                .totalAmount(activeAmount.add(usedAmount))
                .build();
    }

    // ========================== 유틸리티 메소드 ==========================

    /**
     * 회원의 기프티콘 개수 조회
     * 
     * @param memberId 회원 ID
     * @return 기프티콘 개수
     */
    @Cacheable(value = "memberGifticons", key = "'count:' + #memberId")
    public long getMemberGifticonCount(@NotNull @Positive Long memberId) {
        
        log.debug("회원 기프티콘 개수 조회: {}", memberId);
        
        return gifticonRepository.countByMemberIdAndDeletedAtIsNull(memberId);
    }

    /**
     * 최근 사용된 기프티콘 조회
     * 
     * @param pageable 페이징 정보
     * @return 최근 사용 기프티콘 목록
     */
    public List<GifticonResponseDto> getRecentlyUsedGifticons(Pageable pageable) {
        
        log.debug("최근 사용 기프티콘 조회: {}", pageable);
        
        Slice<Gifticon> recentGifticons = gifticonRepository.findRecentlyUsedGifticons(pageable);
        
        return recentGifticons.stream()
                .map(GifticonResponseDto::fromEntity)
                .toList();
    }

    // ========================== 비즈니스 로직 메소드 ==========================

    /**
     * 기프티콘 코드 유효성 검사
     * 
     * @param gifticonCode 기프티콘 코드
     * @return 유효 여부
     */
    public boolean isValidGifticonCode(@NotBlank String gifticonCode) {
        
        log.debug("기프티콘 코드 유효성 검사: {}", gifticonCode);
        
        return gifticonRepository.existsByGifticonCode(gifticonCode);
    }

    /**
     * 기프티콘 사용 가능 여부 확인
     * 
     * @param gifticonId 기프티콘 ID
     * @param memberId 회원 ID
     * @return 사용 가능 여부
     */
    public boolean canUseGifticon(@NotNull @Positive Long gifticonId, 
                                 @NotNull @Positive Long memberId) {
        
        log.debug("기프티콘 사용 가능 확인: ID={}, 회원={}", gifticonId, memberId);
        
        Optional<Gifticon> gifticon = gifticonRepository.findById(gifticonId);
        
        if (gifticon.isEmpty() || gifticon.get().isDeleted()) {
            return false;
        }
        
        Gifticon g = gifticon.get();
        return g.isOwnedBy(memberId) && g.isUsable();
    }

    // ========================== 내부 헬퍼 메소드 ==========================

    /**
     * 고유한 기프티콘 코드 생성
     * 
     * @return 고유한 기프티콘 코드
     */
    private String generateUniqueGifticonCode() {
        
        String code;
        int attempts = 0;
        final int maxAttempts = 10;
        
        do {
            code = GIFTICON_CODE_PREFIX + "-" + System.currentTimeMillis() + "-" + 
                   UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new BusinessException("기프티콘 코드 생성에 실패했습니다.");
            }
        } while (gifticonRepository.existsByGifticonCode(code));
        
        return code;
    }

    /**
     * 유효기간 검증
     * 
     * @param expiryDate 유효기간
     * @throws ValidationException 유효기간이 과거인 경우
     */
    private void validateExpiryDate(LocalDateTime expiryDate) {
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            throw new ValidationException("유효기간은 현재 시간 이후여야 합니다.");
        }
    }

    /**
     * 기프티콘 수정 가능 여부 검증
     * 
     * @param gifticon 기프티콘
     * @throws BusinessException 수정할 수 없는 상태
     */
    private void validateUpdatable(Gifticon gifticon) {
        if (gifticon.getStatus() == GifticonStatus.USED) {
            throw new BusinessException("사용된 기프티콘은 수정할 수 없습니다.");
        }
        
        if (gifticon.getStatus() == GifticonStatus.EXPIRED) {
            throw new BusinessException("만료된 기프티콘은 수정할 수 없습니다.");
        }
    }
}