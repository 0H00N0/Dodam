package com.dodam.product.service;

import com.dodam.product.dto.request.EventRewardRequestDto;
import com.dodam.product.dto.response.EventRewardResponseDto;
import com.dodam.product.dto.statistics.EventStatisticsDto;
import com.dodam.product.entity.EventReward;
import com.dodam.product.entity.EventReward.RewardStatus;
import com.dodam.product.entity.EventReward.RewardType;
import com.dodam.product.exception.*;
import com.dodam.product.repository.EventRewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

/**
 * 이벤트 보상 비즈니스 로직을 처리하는 Service 클래스
 * 이벤트 참여, 조건 충족, 보상 지급, 만료 관리 등의 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 이벤트 보상 등록 및 관리
 * - 조건 충족 확인 및 보상 승인
 * - 보상 지급 및 실패 처리
 * - 만료 관리 및 자동 처리
 * - 통계 및 성과 분석
 * - 회원별/이벤트별 보상 관리
 * 
 * @author Dodam Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Validated
public class EventRewardService {

    private final EventRewardRepository eventRewardRepository;

    // 만료 예정 알림 기준 일수 (기본: 3일)
    private static final int EXPIRY_ALERT_DAYS = 3;
    
    // 재처리 시도 최대 횟수
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // ========================== 기본 CRUD 메소드 ==========================

    /**
     * 이벤트 보상 등록
     * 
     * @param requestDto 이벤트 보상 등록 요청 정보
     * @return 등록된 이벤트 보상 정보
     * @throws ValidationException 유효성 검증 실패
     * @throws DuplicateResourceException 중복 참여 시도
     */
    @Transactional
    @CacheEvict(value = {"eventRewards", "eventStats", "memberRewards"}, allEntries = true)
    public EventRewardResponseDto registerEventReward(@Valid EventRewardRequestDto requestDto) {
        
        log.info("이벤트 보상 등록 시작: {}", requestDto);

        // 1. 요청 데이터 검증 및 정규화
        requestDto.normalize();
        if (!requestDto.isValid()) {
            throw new ValidationException("이벤트 보상 정보가 유효하지 않습니다.");
        }

        // 2. 중복 참여 검사 (참조 정보가 있는 경우)
        if (requestDto.getReferenceType() != null && requestDto.getReferenceId() != null) {
            boolean isDuplicate = eventRewardRepository.existsByMemberAndReference(
                    requestDto.getMemberId(), 
                    requestDto.getReferenceType(), 
                    requestDto.getReferenceId()
            );
            
            if (isDuplicate) {
                throw new DuplicateResourceException("이미 참여한 이벤트입니다.");
            }
        }

        // 3. 동일 이벤트 중복 참여 검사
        if (eventRewardRepository.existsByMemberIdAndEventCodeAndDeletedAtIsNull(
                requestDto.getMemberId(), requestDto.getEventCode())) {
            throw new DuplicateResourceException("이미 참여한 이벤트입니다: " + requestDto.getEventCode());
        }

        // 4. 엔티티 생성
        EventReward eventReward = requestDto.toEntity();
        eventReward.setStatus(RewardStatus.PENDING);

        // 5. 저장
        eventReward = eventRewardRepository.save(eventReward);

        log.info("이벤트 보상 등록 완료: ID={}, 이벤트={}", eventReward.getEventRewardId(), requestDto.getEventCode());
        
        return EventRewardResponseDto.fromEntity(eventReward);
    }

    /**
     * 이벤트 보상 ID로 단건 조회
     * 
     * @param eventRewardId 이벤트 보상 ID
     * @return 이벤트 보상 정보
     * @throws ResourceNotFoundException 이벤트 보상을 찾을 수 없는 경우
     */
    @Cacheable(value = "eventRewards", key = "#eventRewardId")
    public EventRewardResponseDto getEventRewardById(@NotNull @Positive Long eventRewardId) {
        
        log.debug("이벤트 보상 단건 조회: {}", eventRewardId);
        
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .filter(er -> !er.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("이벤트 보상을 찾을 수 없습니다: " + eventRewardId));
        
        return EventRewardResponseDto.fromEntity(eventReward);
    }

    /**
     * 이벤트 보상 정보 수정
     * 
     * @param eventRewardId 이벤트 보상 ID
     * @param requestDto 수정 요청 정보
     * @return 수정된 이벤트 보상 정보
     * @throws ResourceNotFoundException 이벤트 보상을 찾을 수 없는 경우
     * @throws BusinessException 비즈니스 규칙 위반
     */
    @Transactional
    @CacheEvict(value = {"eventRewards", "eventStats", "memberRewards"}, allEntries = true)
    public EventRewardResponseDto updateEventReward(@NotNull @Positive Long eventRewardId, 
                                                   @Valid EventRewardRequestDto requestDto) {
        
        log.info("이벤트 보상 수정 시작: ID={}", eventRewardId);

        // 1. 기존 이벤트 보상 조회
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .filter(er -> !er.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("이벤트 보상을 찾을 수 없습니다: " + eventRewardId));

        // 2. 수정 가능 상태인지 확인
        validateUpdatable(eventReward);

        // 3. 요청 데이터 검증
        requestDto.normalize();
        if (!requestDto.isValid()) {
            throw new ValidationException("이벤트 보상 정보가 유효하지 않습니다.");
        }

        // 4. 엔티티 업데이트
        requestDto.updateEntity(eventReward);

        // 5. 저장
        eventReward = eventRewardRepository.save(eventReward);

        log.info("이벤트 보상 수정 완료: ID={}", eventRewardId);
        
        return EventRewardResponseDto.fromEntity(eventReward);
    }

    /**
     * 이벤트 보상 삭제 (소프트 삭제)
     * 
     * @param eventRewardId 이벤트 보상 ID
     * @throws ResourceNotFoundException 이벤트 보상을 찾을 수 없는 경우
     * @throws BusinessException 삭제할 수 없는 상태
     */
    @Transactional
    @CacheEvict(value = {"eventRewards", "eventStats", "memberRewards"}, allEntries = true)
    public void deleteEventReward(@NotNull @Positive Long eventRewardId) {
        
        log.info("이벤트 보상 삭제 시작: {}", eventRewardId);

        // 1. 이벤트 보상 조회
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .orElseThrow(() -> new ResourceNotFoundException("이벤트 보상을 찾을 수 없습니다: " + eventRewardId));

        if (eventReward.isDeleted()) {
            throw new BusinessException("이미 삭제된 이벤트 보상입니다.");
        }

        // 2. 삭제 가능 여부 확인
        if (eventReward.getStatus() == RewardStatus.REWARDED) {
            throw new BusinessException("이미 지급된 보상은 삭제할 수 없습니다.");
        }

        // 3. 소프트 삭제 실행
        eventReward.delete();
        eventRewardRepository.save(eventReward);

        log.info("이벤트 보상 삭제 완료: {}", eventRewardId);
    }

    // ========================== 이벤트 보상 상태 관리 메소드 ==========================

    /**
     * 이벤트 조건 충족 처리
     * 
     * @param eventRewardId 이벤트 보상 ID
     * @param memberId 회원 ID (권한 확인용)
     * @return 조건 충족 처리된 이벤트 보상 정보
     * @throws ResourceNotFoundException 이벤트 보상을 찾을 수 없는 경우
     * @throws UnauthorizedException 권한 없는 사용자
     * @throws BusinessException 조건 충족 처리할 수 없는 상태
     */
    @Transactional
    @CacheEvict(value = {"eventRewards", "eventStats", "memberRewards"}, allEntries = true)
    public EventRewardResponseDto meetCondition(@NotNull @Positive Long eventRewardId, 
                                               @NotNull @Positive Long memberId) {
        
        log.info("이벤트 조건 충족 처리 시작: ID={}, 회원={}", eventRewardId, memberId);

        // 1. 이벤트 보상 조회
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .filter(er -> !er.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("이벤트 보상을 찾을 수 없습니다: " + eventRewardId));

        // 2. 회원 권한 확인
        if (!eventReward.isOwnedBy(memberId)) {
            throw new UnauthorizedException("이벤트 보상에 대한 권한이 없습니다.");
        }

        // 3. 조건 충족 가능 상태 확인
        if (eventReward.getStatus() != RewardStatus.PENDING) {
            throw new BusinessException("대기 상태의 보상만 조건 충족 처리할 수 있습니다.");
        }

        if (!eventReward.isEventActive()) {
            throw new BusinessException("종료된 이벤트의 보상은 처리할 수 없습니다.");
        }

        // 4. 조건 충족 처리
        eventReward.meetCondition();
        eventReward = eventRewardRepository.save(eventReward);

        log.info("이벤트 조건 충족 처리 완료: ID={}", eventRewardId);
        
        return EventRewardResponseDto.fromEntity(eventReward);
    }

    /**
     * 보상 지급 처리
     * 
     * @param eventRewardId 이벤트 보상 ID
     * @return 지급 처리된 이벤트 보상 정보
     * @throws ResourceNotFoundException 이벤트 보상을 찾을 수 없는 경우
     * @throws BusinessException 지급 처리할 수 없는 상태
     */
    @Transactional
    @CacheEvict(value = {"eventRewards", "eventStats", "memberRewards"}, allEntries = true)
    public EventRewardResponseDto grantReward(@NotNull @Positive Long eventRewardId) {
        
        log.info("보상 지급 처리 시작: {}", eventRewardId);

        // 1. 이벤트 보상 조회
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .filter(er -> !er.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("이벤트 보상을 찾을 수 없습니다: " + eventRewardId));

        // 2. 지급 가능 상태 확인
        if (!eventReward.isGrantable()) {
            if (eventReward.isExpired()) {
                throw new ExpiredException("만료된 보상입니다.");
            } else if (eventReward.getStatus() != RewardStatus.APPROVED) {
                throw new BusinessException("승인된 보상만 지급할 수 있습니다.");
            } else if (!eventReward.getConditionMet()) {
                throw new BusinessException("조건이 충족되지 않은 보상입니다.");
            } else {
                throw new BusinessException("지급할 수 없는 보상입니다.");
            }
        }

        try {
            // 3. 보상 지급 처리
            eventReward.grantReward();
            eventReward = eventRewardRepository.save(eventReward);

            log.info("보상 지급 처리 완료: ID={}, 타입={}, 금액={}", 
                    eventRewardId, eventReward.getRewardType(), eventReward.getRewardAmount());
            
            return EventRewardResponseDto.fromEntity(eventReward);
            
        } catch (Exception e) {
            // 4. 지급 실패 처리
            log.error("보상 지급 실패: ID={}, 오류={}", eventRewardId, e.getMessage());
            
            eventReward.failReward(e.getMessage());
            eventRewardRepository.save(eventReward);
            
            throw new BusinessException("보상 지급에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 보상 취소 처리
     * 
     * @param eventRewardId 이벤트 보상 ID
     * @param reason 취소 사유
     * @return 취소 처리된 이벤트 보상 정보
     * @throws ResourceNotFoundException 이벤트 보상을 찾을 수 없는 경우
     * @throws BusinessException 취소할 수 없는 상태
     */
    @Transactional
    @CacheEvict(value = {"eventRewards", "eventStats", "memberRewards"}, allEntries = true)
    public EventRewardResponseDto cancelReward(@NotNull @Positive Long eventRewardId, 
                                              @NotBlank String reason) {
        
        log.info("보상 취소 처리 시작: ID={}, 사유={}", eventRewardId, reason);

        // 1. 이벤트 보상 조회
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .filter(er -> !er.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("이벤트 보상을 찾을 수 없습니다: " + eventRewardId));

        // 2. 취소 가능 상태 확인
        if (eventReward.getStatus() == RewardStatus.REWARDED) {
            throw new BusinessException("이미 지급된 보상은 취소할 수 없습니다.");
        }

        if (eventReward.getStatus() == RewardStatus.CANCELLED) {
            throw new BusinessException("이미 취소된 보상입니다.");
        }

        // 3. 취소 처리
        eventReward.cancel(reason);
        eventReward = eventRewardRepository.save(eventReward);

        log.info("보상 취소 처리 완료: ID={}", eventRewardId);
        
        return EventRewardResponseDto.fromEntity(eventReward);
    }

    /**
     * 만료된 보상 일괄 처리
     * 
     * @return 처리된 보상 수
     */
    @Transactional
    @CacheEvict(value = {"eventRewards", "eventStats", "memberRewards"}, allEntries = true)
    public int expireOutdatedRewards() {
        
        log.info("만료된 보상 일괄 처리 시작");

        LocalDateTime now = LocalDateTime.now();
        int expiredCount = eventRewardRepository.expireOutdatedRewards(now);

        log.info("만료된 보상 일괄 처리 완료: {}개", expiredCount);
        
        return expiredCount;
    }

    // ========================== 검색 및 조회 메소드 ==========================

    /**
     * 전체 활성 이벤트 보상 목록 조회 (페이징)
     * 
     * @param pageable 페이징 정보
     * @return 이벤트 보상 목록 페이지
     */
    public Page<EventRewardResponseDto> getAllEventRewards(Pageable pageable) {
        
        log.debug("전체 이벤트 보상 목록 조회: {}", pageable);
        
        return eventRewardRepository.findByDeletedAtIsNull(pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 회원별 이벤트 보상 목록 조회
     * 
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 해당 회원의 이벤트 보상 목록
     */
    @Cacheable(value = "memberRewards", key = "#memberId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<EventRewardResponseDto> getMemberEventRewards(@NotNull @Positive Long memberId, 
                                                              Pageable pageable) {
        
        log.debug("회원별 이벤트 보상 조회: 회원={}, {}", memberId, pageable);
        
        return eventRewardRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 회원의 지급 가능한 보상 조회
     * 
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 지급 가능한 보상 목록
     */
    @Cacheable(value = "memberRewards", key = "'grantable:' + #memberId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<EventRewardResponseDto> getGrantableRewards(@NotNull @Positive Long memberId, 
                                                           Pageable pageable) {
        
        log.debug("지급 가능 보상 조회: 회원={}, {}", memberId, pageable);
        
        LocalDateTime now = LocalDateTime.now();
        
        return eventRewardRepository.findGrantableRewards(memberId, now, pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 이벤트별 보상 목록 조회
     * 
     * @param eventCode 이벤트 코드
     * @param pageable 페이징 정보
     * @return 해당 이벤트의 보상 목록
     */
    public Page<EventRewardResponseDto> getEventRewardsByEvent(@NotBlank String eventCode, 
                                                              Pageable pageable) {
        
        log.debug("이벤트별 보상 조회: 이벤트={}, {}", eventCode, pageable);
        
        return eventRewardRepository.findByEventCodeAndDeletedAtIsNull(eventCode, pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 상태별 보상 조회
     * 
     * @param status 보상 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 보상 목록
     */
    public Page<EventRewardResponseDto> getRewardsByStatus(RewardStatus status, 
                                                          Pageable pageable) {
        
        log.debug("상태별 보상 조회: 상태={}, {}", status, pageable);
        
        return eventRewardRepository.findByStatusAndDeletedAtIsNull(status, pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 보상 타입별 조회
     * 
     * @param rewardType 보상 타입
     * @param pageable 페이징 정보
     * @return 해당 타입의 보상 목록
     */
    public Page<EventRewardResponseDto> getRewardsByType(RewardType rewardType, 
                                                        Pageable pageable) {
        
        log.debug("타입별 보상 조회: 타입={}, {}", rewardType, pageable);
        
        return eventRewardRepository.findByRewardTypeAndDeletedAtIsNull(rewardType, pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 키워드 검색 (이벤트명, 보상 설명)
     * 
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색 결과 목록
     */
    public Page<EventRewardResponseDto> searchEventRewards(@NotBlank String keyword, 
                                                          Pageable pageable) {
        
        log.debug("이벤트 보상 검색: 키워드={}, {}", keyword, pageable);
        
        return eventRewardRepository.searchEventRewards(keyword, pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 고급 검색 (복합 조건)
     * 
     * @param requestDto 검색 조건
     * @param pageable 페이징 정보
     * @return 검색 결과 목록
     */
    public Page<EventRewardResponseDto> advancedSearch(@Valid EventRewardRequestDto requestDto, 
                                                      Pageable pageable) {
        
        log.debug("고급 검색: 조건={}, {}", requestDto, pageable);
        
        return eventRewardRepository.advancedSearch(
                requestDto.getMemberId(),
                requestDto.getEventCode(),
                requestDto.getStatus(),
                requestDto.getRewardType(),
                requestDto.getConditionMet(),
                requestDto.getMinAmount(),
                requestDto.getMaxAmount(),
                pageable
        ).map(EventRewardResponseDto::fromEntity);
    }

    // ========================== 만료 관리 메소드 ==========================

    /**
     * 만료 예정 보상 조회
     * 
     * @param days 몇 일 후까지 (기본: 3일)
     * @param pageable 페이징 정보
     * @return 만료 예정 보상 목록
     */
    public Page<EventRewardResponseDto> getExpiringSoonRewards(int days, Pageable pageable) {
        
        log.debug("만료 예정 보상 조회: {}일 이내, {}", days, pageable);
        
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
        
        return eventRewardRepository.findExpiringSoon(expiryDate, pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 만료 알림이 필요한 보상 목록 조회
     * 
     * @param alertDays 알림 기준 일수
     * @return 알림 대상 보상 목록
     */
    public List<EventRewardResponseDto> getRewardsNeedingAlert(int alertDays) {
        
        log.debug("만료 알림 대상 보상 조회: {}일", alertDays);
        
        LocalDateTime alertEndDate = LocalDateTime.now().plusDays(alertDays);
        
        List<EventReward> rewards = eventRewardRepository.findRewardsNeedingExpiryAlert(alertEndDate);
        
        return rewards.stream()
                .map(EventRewardResponseDto::fromEntity)
                .toList();
    }

    // ========================== 통계 및 분석 메소드 ==========================

    /**
     * 이벤트 보상 전체 통계 조회
     * 
     * @return 전체 통계 정보
     */
    @Cacheable(value = "eventStats", key = "'overall'")
    public EventStatisticsDto.OverallStats getOverallStatistics() {
        
        log.debug("전체 통계 조회");
        
        long totalCount = eventRewardRepository.countActiveRewards();
        long pendingCount = eventRewardRepository.countByStatusAndDeletedAtIsNull(RewardStatus.PENDING);
        long approvedCount = eventRewardRepository.countByStatusAndDeletedAtIsNull(RewardStatus.APPROVED);
        long rewardedCount = eventRewardRepository.countByStatusAndDeletedAtIsNull(RewardStatus.REWARDED);
        long expiredCount = eventRewardRepository.countByStatusAndDeletedAtIsNull(RewardStatus.EXPIRED);
        long cancelledCount = eventRewardRepository.countByStatusAndDeletedAtIsNull(RewardStatus.CANCELLED);
        long failedCount = eventRewardRepository.countByStatusAndDeletedAtIsNull(RewardStatus.FAILED);
        long conditionMetCount = eventRewardRepository.countByConditionMetTrueAndDeletedAtIsNull();
        
        Object[] amountStats = eventRewardRepository.getAmountStats();
        BigDecimal totalAmount = amountStats[0] != null ? (BigDecimal) amountStats[0] : BigDecimal.ZERO;
        BigDecimal avgAmount = amountStats[1] != null ? (BigDecimal) amountStats[1] : BigDecimal.ZERO;
        BigDecimal maxAmount = amountStats[2] != null ? (BigDecimal) amountStats[2] : BigDecimal.ZERO;
        BigDecimal minAmount = amountStats[3] != null ? (BigDecimal) amountStats[3] : BigDecimal.ZERO;
        
        // 만료 예정 보상 수
        LocalDateTime alertDate = LocalDateTime.now().plusDays(EXPIRY_ALERT_DAYS);
        long expiringSoonCount = eventRewardRepository.findExpiringSoon(alertDate, Pageable.unpaged()).getTotalElements();
        
        return EventStatisticsDto.OverallStats.builder()
                .totalCount(totalCount)
                .pendingCount(pendingCount)
                .approvedCount(approvedCount)
                .rewardedCount(rewardedCount)
                .expiredCount(expiredCount)
                .cancelledCount(cancelledCount)
                .failedCount(failedCount)
                .conditionMetCount(conditionMetCount)
                .expiringSoonCount(expiringSoonCount)
                .totalRewardAmount(totalAmount)
                .averageRewardAmount(avgAmount)
                .maxRewardAmount(maxAmount)
                .minRewardAmount(minAmount)
                .successRate(totalCount > 0 ? (double) rewardedCount / totalCount * 100 : 0.0)
                .conditionMetRate(totalCount > 0 ? (double) conditionMetCount / totalCount * 100 : 0.0)
                .build();
    }

    /**
     * 상태별 보상 통계 조회
     * 
     * @return 상태별 통계 목록
     */
    @Cacheable(value = "eventStats", key = "'status'")
    public List<EventStatisticsDto.StatusStats> getStatusStatistics() {
        
        log.debug("상태별 통계 조회");
        
        List<Object[]> stats = eventRewardRepository.getStatusStats();
        
        return stats.stream()
                .map(row -> EventStatisticsDto.StatusStats.builder()
                        .status((RewardStatus) row[0])
                        .count((Long) row[1])
                        .build())
                .toList();
    }

    /**
     * 보상 타입별 통계 조회
     * 
     * @return 보상 타입별 통계 목록
     */
    @Cacheable(value = "eventStats", key = "'rewardType'")
    public List<EventStatisticsDto.RewardTypeStats> getRewardTypeStatistics() {
        
        log.debug("보상 타입별 통계 조회");
        
        List<Object[]> stats = eventRewardRepository.getRewardTypeStats();
        
        return stats.stream()
                .map(row -> EventStatisticsDto.RewardTypeStats.builder()
                        .rewardType((RewardType) row[0])
                        .count((Long) row[1])
                        .totalAmount((BigDecimal) row[2])
                        .build())
                .toList();
    }

    /**
     * 이벤트별 통계 조회
     * 
     * @return 이벤트별 통계 목록
     */
    @Cacheable(value = "eventStats", key = "'event'")
    public List<EventStatisticsDto.EventStats> getEventStatistics() {
        
        log.debug("이벤트별 통계 조회");
        
        List<Object[]> stats = eventRewardRepository.getEventStats();
        
        return stats.stream()
                .map(row -> EventStatisticsDto.EventStats.builder()
                        .eventCode((String) row[0])
                        .eventName((String) row[1])
                        .totalParticipants((Long) row[2])
                        .rewardedCount((Long) row[3])
                        .successRate(((Number) row[4]).doubleValue())
                        .build())
                .toList();
    }

    /**
     * 회원별 보상 통계 조회
     * 
     * @param pageable 페이징 정보
     * @return 회원별 통계 목록
     */
    @Cacheable(value = "eventStats", key = "'member:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<EventStatisticsDto.MemberStats> getMemberStatistics(Pageable pageable) {
        
        log.debug("회원별 통계 조회: {}", pageable);
        
        Page<Object[]> stats = eventRewardRepository.getMemberRewardStats(pageable);
        
        return stats.map(row -> EventStatisticsDto.MemberStats.builder()
                .memberId((Long) row[0])
                .totalRewards((Long) row[1])
                .rewardedCount((Long) row[2])
                .totalAmount((BigDecimal) row[3])
                .build());
    }

    /**
     * 특정 이벤트 성과 분석
     * 
     * @param eventCode 이벤트 코드
     * @return 이벤트 성과 분석 결과
     */
    @Cacheable(value = "eventStats", key = "'performance:' + #eventCode")
    public EventStatisticsDto.EventPerformance getEventPerformance(@NotBlank String eventCode) {
        
        log.debug("이벤트 성과 분석: {}", eventCode);
        
        Object[] performance = eventRewardRepository.getEventPerformance(eventCode);
        
        if (performance == null || performance[0] == null) {
            return EventStatisticsDto.EventPerformance.builder()
                    .eventCode(eventCode)
                    .totalParticipants(0L)
                    .conditionMetCount(0L)
                    .rewardedCount(0L)
                    .participationRate(0.0)
                    .completionRate(0.0)
                    .successRate(0.0)
                    .build();
        }
        
        return EventStatisticsDto.EventPerformance.builder()
                .eventCode(eventCode)
                .totalParticipants(((Number) performance[0]).longValue())
                .conditionMetCount(((Number) performance[1]).longValue())
                .rewardedCount(((Number) performance[2]).longValue())
                .participationRate(((Number) performance[3]).doubleValue())
                .completionRate(((Number) performance[4]).doubleValue())
                .successRate(((Number) performance[5]).doubleValue())
                .build();
    }

    /**
     * 특정 회원의 기간별 보상 지급 총액 조회
     * 
     * @param memberId 회원 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 지급받은 총액
     */
    @Cacheable(value = "memberRewards", key = "'amount:' + #memberId + ':' + #startDate + ':' + #endDate")
    public BigDecimal getMemberRewardTotalAmount(@NotNull @Positive Long memberId, 
                                                LocalDateTime startDate, 
                                                LocalDateTime endDate) {
        
        log.debug("회원 기간별 보상 총액 조회: 회원={}, {} ~ {}", memberId, startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("시작일은 종료일보다 이전이어야 합니다.");
        }
        
        return eventRewardRepository.getMemberRewardTotalAmount(memberId, startDate, endDate);
    }

    // ========================== 관리 메소드 ==========================

    /**
     * 지급 대기 중인 보상 목록 조회 (관리자용)
     * 
     * @param pageable 페이징 정보
     * @return 지급 대기 보상 목록
     */
    public Page<EventRewardResponseDto> getPendingRewards(Pageable pageable) {
        
        log.debug("지급 대기 보상 조회: {}", pageable);
        
        return eventRewardRepository.findPendingRewards(pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    /**
     * 실패한 보상 지급 재처리 대상 조회
     * 
     * @return 재처리 대상 보상 목록
     */
    public List<EventRewardResponseDto> getFailedRewards() {
        
        log.debug("실패한 보상 조회");
        
        List<EventReward> failedRewards = eventRewardRepository.findFailedRewards();
        
        return failedRewards.stream()
                .map(EventRewardResponseDto::fromEntity)
                .toList();
    }

    /**
     * 실패한 보상 재처리 실행
     * 
     * @param eventRewardId 이벤트 보상 ID
     * @return 재처리 결과
     */
    @Transactional
    @CacheEvict(value = {"eventRewards", "eventStats", "memberRewards"}, allEntries = true)
    public EventRewardResponseDto retryFailedReward(@NotNull @Positive Long eventRewardId) {
        
        log.info("실패한 보상 재처리 시작: {}", eventRewardId);

        // 1. 실패한 보상 조회
        EventReward eventReward = eventRewardRepository.findById(eventRewardId)
                .filter(er -> !er.isDeleted() && er.getStatus() == RewardStatus.FAILED)
                .orElseThrow(() -> new ResourceNotFoundException("재처리 가능한 보상을 찾을 수 없습니다: " + eventRewardId));

        // 2. 재처리 가능 상태로 복구
        eventReward.setStatus(RewardStatus.APPROVED);
        eventReward = eventRewardRepository.save(eventReward);

        // 3. 다시 지급 시도
        return grantReward(eventRewardId);
    }

    // ========================== 유틸리티 메소드 ==========================

    /**
     * 회원의 이벤트 참여 여부 확인
     * 
     * @param memberId 회원 ID
     * @param eventCode 이벤트 코드
     * @return 참여 여부
     */
    public boolean hasParticipatedInEvent(@NotNull @Positive Long memberId, 
                                         @NotBlank String eventCode) {
        
        log.debug("이벤트 참여 여부 확인: 회원={}, 이벤트={}", memberId, eventCode);
        
        return eventRewardRepository.existsByMemberIdAndEventCodeAndDeletedAtIsNull(memberId, eventCode);
    }

    /**
     * 회원의 이벤트 보상 개수 조회
     * 
     * @param memberId 회원 ID
     * @return 보상 개수
     */
    @Cacheable(value = "memberRewards", key = "'count:' + #memberId")
    public long getMemberRewardCount(@NotNull @Positive Long memberId) {
        
        log.debug("회원 보상 개수 조회: {}", memberId);
        
        return eventRewardRepository.countByMemberIdAndDeletedAtIsNull(memberId);
    }

    /**
     * 진행 중인 이벤트 보상 조회
     * 
     * @param pageable 페이징 정보
     * @return 진행 중인 이벤트 보상 목록
     */
    public Page<EventRewardResponseDto> getActiveEventRewards(Pageable pageable) {
        
        log.debug("진행 중인 이벤트 보상 조회: {}", pageable);
        
        LocalDateTime now = LocalDateTime.now();
        
        return eventRewardRepository.findActiveEventRewards(now, pageable)
                .map(EventRewardResponseDto::fromEntity);
    }

    // ========================== 내부 헬퍼 메소드 ==========================

    /**
     * 이벤트 보상 수정 가능 여부 검증
     * 
     * @param eventReward 이벤트 보상
     * @throws BusinessException 수정할 수 없는 상태
     */
    private void validateUpdatable(EventReward eventReward) {
        if (eventReward.getStatus() == RewardStatus.REWARDED) {
            throw new BusinessException("이미 지급된 보상은 수정할 수 없습니다.");
        }
        
        if (eventReward.getStatus() == RewardStatus.CANCELLED) {
            throw new BusinessException("취소된 보상은 수정할 수 없습니다.");
        }
    }
}