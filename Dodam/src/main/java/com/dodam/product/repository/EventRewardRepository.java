package com.dodam.product.repository;

import com.dodam.product.entity.EventReward;
import com.dodam.product.entity.EventReward.RewardStatus;
import com.dodam.product.entity.EventReward.RewardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 이벤트 보상 Repository 인터페이스
 * 이벤트 보상 정보에 대한 데이터 접근 및 쿼리를 담당합니다.
 */
@Repository
public interface EventRewardRepository extends JpaRepository<EventReward, Long> {

    // ========================== 기본 검색 메소드 ==========================

    /**
     * 삭제되지 않은 모든 이벤트 보상 조회 (정렬 지원)
     * @param sort 정렬 조건
     * @return 활성 이벤트 보상 목록
     */
    List<EventReward> findByDeletedAtIsNull(Sort sort);

    /**
     * 삭제되지 않은 이벤트 보상 페이징 조회
     * @param pageable 페이징 정보
     * @return 활성 이벤트 보상 페이지
     */
    Page<EventReward> findByDeletedAtIsNull(Pageable pageable);

    /**
     * 삭제된 이벤트 보상 조회
     * @param pageable 페이징 정보
     * @return 삭제된 이벤트 보상 페이지
     */
    Page<EventReward> findByDeletedAtIsNotNull(Pageable pageable);

    // ========================== 회원별 검색 메소드 ==========================

    /**
     * 회원별 이벤트 보상 조회
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 해당 회원의 이벤트 보상 페이지
     */
    Page<EventReward> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    /**
     * 회원별 이벤트 보상 수 조회
     * @param memberId 회원 ID
     * @return 해당 회원의 이벤트 보상 수
     */
    long countByMemberIdAndDeletedAtIsNull(Long memberId);

    /**
     * 회원별 특정 상태의 이벤트 보상 조회
     * @param memberId 회원 ID
     * @param status 보상 상태
     * @param pageable 페이징 정보
     * @return 해당 조건의 이벤트 보상 페이지
     */
    Page<EventReward> findByMemberIdAndStatusAndDeletedAtIsNull(Long memberId, RewardStatus status, Pageable pageable);

    /**
     * 회원의 지급 가능한 보상 조회
     * @param memberId 회원 ID
     * @param now 현재 시간
     * @param pageable 페이징 정보
     * @return 지급 가능한 보상 페이지
     */
    @Query("SELECT er FROM EventReward er " +
           "WHERE er.memberId = :memberId " +
           "AND er.status = com.dodam.product.entity.EventReward$RewardStatus.APPROVED " +
           "AND er.conditionMet = true " +
           "AND (er.expiresAt IS NULL OR er.expiresAt > :now) " +
           "AND er.deletedAt IS NULL")
    Page<EventReward> findGrantableRewards(@Param("memberId") Long memberId, 
                                          @Param("now") LocalDateTime now, 
                                          Pageable pageable);

    /**
     * 회원의 특정 이벤트 보상 조회
     * @param memberId 회원 ID
     * @param eventCode 이벤트 코드
     * @return 해당 이벤트의 보상 정보 (Optional)
     */
    Optional<EventReward> findByMemberIdAndEventCodeAndDeletedAtIsNull(Long memberId, String eventCode);

    /**
     * 회원이 특정 이벤트에 참여했는지 확인
     * @param memberId 회원 ID
     * @param eventCode 이벤트 코드
     * @return 참여 여부
     */
    boolean existsByMemberIdAndEventCodeAndDeletedAtIsNull(Long memberId, String eventCode);

    // ========================== 이벤트별 검색 메소드 ==========================

    /**
     * 이벤트별 보상 조회
     * @param eventCode 이벤트 코드
     * @param pageable 페이징 정보
     * @return 해당 이벤트의 보상 페이지
     */
    Page<EventReward> findByEventCodeAndDeletedAtIsNull(String eventCode, Pageable pageable);

    /**
     * 이벤트별 보상 수 조회
     * @param eventCode 이벤트 코드
     * @return 해당 이벤트의 보상 수
     */
    long countByEventCodeAndDeletedAtIsNull(String eventCode);

    /**
     * 이벤트별 특정 상태의 보상 조회
     * @param eventCode 이벤트 코드
     * @param status 보상 상태
     * @param pageable 페이징 정보
     * @return 해당 조건의 보상 페이지
     */
    Page<EventReward> findByEventCodeAndStatusAndDeletedAtIsNull(String eventCode, RewardStatus status, Pageable pageable);

    /**
     * 진행 중인 이벤트의 보상 조회
     * @param now 현재 시간
     * @param pageable 페이징 정보
     * @return 진행 중인 이벤트 보상 페이지
     */
    @Query("SELECT er FROM EventReward er " +
           "WHERE (:now BETWEEN er.eventStartDate AND er.eventEndDate) " +
           "AND er.deletedAt IS NULL")
    Page<EventReward> findActiveEventRewards(@Param("now") LocalDateTime now, Pageable pageable);

    // ========================== 보상 상태별 검색 메소드 ==========================

    /**
     * 상태별 보상 조회
     * @param status 보상 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 보상 페이지
     */
    Page<EventReward> findByStatusAndDeletedAtIsNull(RewardStatus status, Pageable pageable);

    /**
     * 상태별 보상 수 조회
     * @param status 보상 상태
     * @return 해당 상태의 보상 수
     */
    long countByStatusAndDeletedAtIsNull(RewardStatus status);

    /**
     * 대기 중인 보상 조회
     * @param pageable 페이징 정보
     * @return 대기 중인 보상 페이지
     */
    Page<EventReward> findByStatusAndDeletedAtIsNullOrderByCreatedAtAsc(RewardStatus status, Pageable pageable);

    /**
     * 지급 완료된 보상 조회
     * @param pageable 페이징 정보
     * @return 지급 완료 보상 페이지
     */
    Page<EventReward> findByStatusAndDeletedAtIsNullOrderByRewardedAtDesc(RewardStatus status, Pageable pageable);

    // ========================== 보상 타입별 검색 메소드 ==========================

    /**
     * 보상 타입별 보상 조회
     * @param rewardType 보상 타입
     * @param pageable 페이징 정보
     * @return 해당 타입의 보상 페이지
     */
    Page<EventReward> findByRewardTypeAndDeletedAtIsNull(RewardType rewardType, Pageable pageable);

    /**
     * 보상 타입별 보상 수 조회
     * @param rewardType 보상 타입
     * @return 해당 타입의 보상 수
     */
    long countByRewardTypeAndDeletedAtIsNull(RewardType rewardType);

    // ========================== 조건 충족 여부별 검색 메소드 ==========================

    /**
     * 조건 충족된 보상 조회
     * @param pageable 페이징 정보
     * @return 조건 충족 보상 페이지
     */
    Page<EventReward> findByConditionMetTrueAndDeletedAtIsNull(Pageable pageable);

    /**
     * 조건 미충족 보상 조회
     * @param pageable 페이징 정보
     * @return 조건 미충족 보상 페이지
     */
    Page<EventReward> findByConditionMetFalseAndDeletedAtIsNull(Pageable pageable);

    /**
     * 조건 충족된 보상 수 조회
     * @return 조건 충족 보상 수
     */
    long countByConditionMetTrueAndDeletedAtIsNull();

    // ========================== 기간별 검색 메소드 ==========================

    /**
     * 특정 기간 내 생성된 보상 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 기간 내 보상 페이지
     */
    Page<EventReward> findByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 특정 기간 내 지급된 보상 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 기간 내 지급된 보상 페이지
     */
    Page<EventReward> findByRewardedAtBetweenAndDeletedAtIsNull(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 만료된 보상 조회
     * @param now 현재 시간
     * @param pageable 페이징 정보
     * @return 만료된 보상 페이지
     */
    Page<EventReward> findByExpiresAtBeforeAndDeletedAtIsNull(LocalDateTime now, Pageable pageable);

    /**
     * 만료 예정 보상 조회 (N일 이내)
     * @param expiryDate 만료 기준일
     * @param pageable 페이징 정보
     * @return 만료 예정 보상 페이지
     */
    @Query("SELECT er FROM EventReward er " +
           "WHERE er.expiresAt BETWEEN CURRENT_TIMESTAMP AND :expiryDate " +
           "AND er.status IN ('APPROVED', 'PENDING') " +
           "AND er.deletedAt IS NULL")
    Page<EventReward> findExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate, Pageable pageable);

    // ========================== 금액별 검색 메소드 ==========================

    /**
     * 금액 범위로 보상 검색
     * @param minAmount 최소 금액
     * @param maxAmount 최대 금액
     * @param pageable 페이징 정보
     * @return 금액 범위 내 보상 페이지
     */
    Page<EventReward> findByRewardAmountBetweenAndDeletedAtIsNull(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * 특정 금액 이상의 보상 검색
     * @param minAmount 최소 금액
     * @param pageable 페이징 정보
     * @return 금액 이상 보상 페이지
     */
    Page<EventReward> findByRewardAmountGreaterThanEqualAndDeletedAtIsNull(BigDecimal minAmount, Pageable pageable);

    // ========================== 복합 검색 메소드 ==========================

    /**
     * 고급 보상 검색 (복합 조건)
     * @param memberId 회원 ID (null 허용)
     * @param eventCode 이벤트 코드 (null 허용)
     * @param status 보상 상태 (null 허용)
     * @param rewardType 보상 타입 (null 허용)
     * @param conditionMet 조건 충족 여부 (null 허용)
     * @param minAmount 최소 금액 (null 허용)
     * @param maxAmount 최대 금액 (null 허용)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 보상 페이지
     */
    @Query("SELECT er FROM EventReward er " +
           "WHERE er.deletedAt IS NULL " +
           "AND (:memberId IS NULL OR er.memberId = :memberId) " +
           "AND (:eventCode IS NULL OR er.eventCode = :eventCode) " +
           "AND (:status IS NULL OR er.status = :status) " +
           "AND (:rewardType IS NULL OR er.rewardType = :rewardType) " +
           "AND (:conditionMet IS NULL OR er.conditionMet = :conditionMet) " +
           "AND (:minAmount IS NULL OR er.rewardAmount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR er.rewardAmount <= :maxAmount)")
    Page<EventReward> advancedSearch(@Param("memberId") Long memberId,
                                     @Param("eventCode") String eventCode,
                                     @Param("status") RewardStatus status,
                                     @Param("rewardType") RewardType rewardType,
                                     @Param("conditionMet") Boolean conditionMet,
                                     @Param("minAmount") BigDecimal minAmount,
                                     @Param("maxAmount") BigDecimal maxAmount,
                                     Pageable pageable);

    /**
     * 이벤트명 또는 보상 설명에 키워드가 포함된 보상 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 보상 페이지
     */
    @Query("SELECT er FROM EventReward er " +
           "WHERE er.deletedAt IS NULL AND " +
           "(er.eventName LIKE CONCAT('%', :keyword, '%') OR er.rewardDescription LIKE CONCAT('%', :keyword, '%'))")
    Page<EventReward> searchEventRewards(@Param("keyword") String keyword, Pageable pageable);

    // ========================== 통계 및 집계 쿼리 ==========================

    /**
     * 전체 활성 보상 수 조회
     * @return 활성 보상 수
     */
    @Query("SELECT COUNT(er) FROM EventReward er WHERE er.deletedAt IS NULL")
    long countActiveRewards();

    /**
     * 상태별 보상 통계 조회
     * @return [상태, 보상수]
     */
    @Query("SELECT er.status, COUNT(er) FROM EventReward er " +
           "WHERE er.deletedAt IS NULL " +
           "GROUP BY er.status " +
           "ORDER BY COUNT(er) DESC")
    List<Object[]> getStatusStats();

    /**
     * 보상 타입별 통계 조회
     * @return [보상타입, 보상수, 총금액]
     */
    @Query("SELECT er.rewardType, COUNT(er), SUM(er.rewardAmount) " +
           "FROM EventReward er " +
           "WHERE er.deletedAt IS NULL " +
           "GROUP BY er.rewardType " +
           "ORDER BY COUNT(er) DESC")
    List<Object[]> getRewardTypeStats();

    /**
     * 이벤트별 보상 통계 조회
     * @return [이벤트코드, 이벤트명, 총참여자, 지급완료수, 지급률]
     */
    @Query("SELECT er.eventCode, er.eventName, COUNT(er), " +
           "SUM(CASE WHEN er.status = com.dodam.product.entity.EventReward$RewardStatus.REWARDED THEN 1 ELSE 0 END), " +
           "ROUND(SUM(CASE WHEN er.status = com.dodam.product.entity.EventReward$RewardStatus.REWARDED THEN 1.0 ELSE 0 END) / COUNT(er) * 100, 2) " +
           "FROM EventReward er " +
           "WHERE er.deletedAt IS NULL " +
           "GROUP BY er.eventCode, er.eventName " +
           "ORDER BY COUNT(er) DESC")
    List<Object[]> getEventStats();

    /**
     * 회원별 보상 통계 조회 (상위 N명)
     * @param pageable 페이징 정보
     * @return [회원ID, 총보상수, 지급받은보상수, 총지급금액]
     */
    @Query("SELECT er.memberId, COUNT(er), " +
           "SUM(CASE WHEN er.status = com.dodam.product.entity.EventReward$RewardStatus.REWARDED THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN er.status = 'REWARDED' THEN er.rewardAmount ELSE 0 END) " +
           "FROM EventReward er " +
           "WHERE er.deletedAt IS NULL " +
           "GROUP BY er.memberId " +
           "ORDER BY COUNT(er) DESC")
    Page<Object[]> getMemberRewardStats(Pageable pageable);

    /**
     * 금액별 통계 조회
     * @return [총지급액, 평균지급액, 최대지급액, 최소지급액]
     */
    @Query("SELECT SUM(er.rewardAmount), AVG(er.rewardAmount), MAX(er.rewardAmount), MIN(er.rewardAmount) " +
           "FROM EventReward er " +
           "WHERE er.status = com.dodam.product.entity.EventReward$RewardStatus.REWARDED AND er.deletedAt IS NULL")
    Object[] getAmountStats();

    // ========================== 성과 분석 쿼리 ==========================

    /**
     * 이벤트 성과 분석 (참여율, 완료율, 지급률)
     * @param eventCode 이벤트 코드
     * @return [총참여자, 조건충족자, 지급완료자, 참여율, 완료율, 지급률]
     */
    @Query("SELECT COUNT(er), " +
           "SUM(CASE WHEN er.conditionMet = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN er.status = com.dodam.product.entity.EventReward$RewardStatus.REWARDED THEN 1 ELSE 0 END), " +
           "100.0, " +
           "ROUND(SUM(CASE WHEN er.conditionMet = true THEN 1.0 ELSE 0 END) / COUNT(er) * 100, 2), " +
           "ROUND(SUM(CASE WHEN er.status = com.dodam.product.entity.EventReward$RewardStatus.REWARDED THEN 1.0 ELSE 0 END) / COUNT(er) * 100, 2) " +
           "FROM EventReward er " +
           "WHERE er.eventCode = :eventCode AND er.deletedAt IS NULL")
    Object[] getEventPerformance(@Param("eventCode") String eventCode);

    /**
     * 월별 보상 지급 통계 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return [연월, 지급수, 총지급액]
     */
    @Query("SELECT SUBSTRING(CAST(er.rewardedAt AS string), 1, 7), COUNT(er), SUM(er.rewardAmount) " +
           "FROM EventReward er " +
           "WHERE er.rewardedAt BETWEEN :startDate AND :endDate " +
           "AND er.status = com.dodam.product.entity.EventReward$RewardStatus.REWARDED " +
           "AND er.deletedAt IS NULL " +
           "GROUP BY SUBSTRING(CAST(er.rewardedAt AS string), 1, 7) " +
           "ORDER BY SUBSTRING(CAST(er.rewardedAt AS string), 1, 7)")
    List<Object[]> getMonthlyRewardStats(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * 일별 보상 지급 통계 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return [날짜, 지급수, 총지급액]
     */
    @Query("SELECT DATE(er.rewardedAt), COUNT(er), SUM(er.rewardAmount) " +
           "FROM EventReward er " +
           "WHERE er.rewardedAt BETWEEN :startDate AND :endDate " +
           "AND er.status = 'REWARDED' " +
           "AND er.deletedAt IS NULL " +
           "GROUP BY DATE(er.rewardedAt) " +
           "ORDER BY DATE(er.rewardedAt)")
    List<Object[]> getDailyRewardStats(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    // ========================== 수정 및 삭제 메소드 ==========================

    /**
     * 보상 상태 업데이트 (Bulk Update)
     * @param eventRewardId 보상 ID
     * @param status 새로운 상태
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE EventReward er SET er.status = :status, er.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE er.eventRewardId = :eventRewardId")
    int updateStatus(@Param("eventRewardId") Long eventRewardId, @Param("status") RewardStatus status);

    /**
     * 조건 충족 처리 (Bulk Update)
     * @param eventRewardId 보상 ID
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE EventReward er SET er.conditionMet = true, er.status = com.dodam.product.entity.EventReward$RewardStatus.APPROVED, " +
           "er.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE er.eventRewardId = :eventRewardId")
    int meetCondition(@Param("eventRewardId") Long eventRewardId);

    /**
     * 보상 지급 처리 (Bulk Update)
     * @param eventRewardId 보상 ID
     * @param rewardedAt 지급 시간
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE EventReward er SET er.status = com.dodam.product.entity.EventReward$RewardStatus.REWARDED, er.rewardedAt = :rewardedAt, " +
           "er.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE er.eventRewardId = :eventRewardId")
    int grantReward(@Param("eventRewardId") Long eventRewardId, @Param("rewardedAt") LocalDateTime rewardedAt);

    /**
     * 만료된 보상 일괄 만료 처리
     * @param now 현재 시간
     * @return 업데이트된 보상 수
     */
    @Modifying
    @Query("UPDATE EventReward er SET er.status = com.dodam.product.entity.EventReward$RewardStatus.EXPIRED, er.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE er.expiresAt < :now " +
           "AND er.status IN (com.dodam.product.entity.EventReward$RewardStatus.PENDING, com.dodam.product.entity.EventReward$RewardStatus.APPROVED) " +
           "AND er.deletedAt IS NULL")
    int expireOutdatedRewards(@Param("now") LocalDateTime now);

    /**
     * 특정 이벤트의 모든 보상 상태 업데이트
     * @param eventCode 이벤트 코드
     * @param status 새로운 상태
     * @return 업데이트된 보상 수
     */
    @Modifying
    @Query("UPDATE EventReward er SET er.status = :status, er.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE er.eventCode = :eventCode AND er.deletedAt IS NULL")
    int updateAllEventRewardsStatus(@Param("eventCode") String eventCode, @Param("status") RewardStatus status);

    /**
     * 보상 소프트 삭제 (Bulk Update)
     * @param eventRewardId 보상 ID
     * @param deletedAt 삭제 시간
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE EventReward er SET er.deletedAt = :deletedAt, er.status = com.dodam.product.entity.EventReward$RewardStatus.CANCELLED " +
           "WHERE er.eventRewardId = :eventRewardId")
    int softDeleteReward(@Param("eventRewardId") Long eventRewardId, @Param("deletedAt") LocalDateTime deletedAt);

    // ========================== 특수 쿼리 ==========================

    /**
     * 특정 기간 내 생성된 보상 수 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 생성된 보상 수
     */
    @Query("SELECT COUNT(er) FROM EventReward er " +
           "WHERE er.createdAt BETWEEN :startDate AND :endDate")
    long countRewardsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 회원의 특정 기간 보상 지급 총액 조회
     * @param memberId 회원 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 지급받은 총액
     */
    @Query("SELECT COALESCE(SUM(er.rewardAmount), 0) FROM EventReward er " +
           "WHERE er.memberId = :memberId " +
           "AND er.rewardedAt BETWEEN :startDate AND :endDate " +
           "AND er.status = 'REWARDED' " +
           "AND er.deletedAt IS NULL")
    BigDecimal getMemberRewardTotalAmount(@Param("memberId") Long memberId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * 지급 대기 중인 보상 목록 조회 (관리자용)
     * @param pageable 페이징 정보
     * @return 지급 대기 보상 목록
     */
    @Query("SELECT er FROM EventReward er " +
           "WHERE er.status = 'APPROVED' " +
           "AND er.conditionMet = true " +
           "AND (er.expiresAt IS NULL OR er.expiresAt > CURRENT_TIMESTAMP) " +
           "AND er.deletedAt IS NULL " +
           "ORDER BY er.scheduledDate ASC, er.createdAt ASC")
    Page<EventReward> findPendingRewards(Pageable pageable);

    /**
     * 실패한 보상 지급 재처리 대상 조회
     * @return 재처리 대상 보상 목록
     */
    @Query("SELECT er FROM EventReward er " +
           "WHERE er.status = com.dodam.product.entity.EventReward$RewardStatus.FAILED " +
           "AND er.deletedAt IS NULL " +
           "ORDER BY er.updatedAt ASC")
    List<EventReward> findFailedRewards();

    /**
     * 이벤트 참여 중복 검사를 위한 쿼리
     * @param memberId 회원 ID
     * @param referenceType 참조 타입
     * @param referenceId 참조 ID
     * @return 중복 참여 여부
     */
    @Query("SELECT COUNT(er) > 0 FROM EventReward er " +
           "WHERE er.memberId = :memberId " +
           "AND er.referenceType = :referenceType " +
           "AND er.referenceId = :referenceId " +
           "AND er.deletedAt IS NULL")
    boolean existsByMemberAndReference(@Param("memberId") Long memberId, 
                                      @Param("referenceType") String referenceType, 
                                      @Param("referenceId") Long referenceId);

    /**
     * 알림이 필요한 만료 예정 보상 조회
     * @param alertEndDate 알림 종료 날짜
     * @return 알림 대상 보상 목록
     */
    @Query("SELECT er FROM EventReward er " +
           "WHERE er.status IN (com.dodam.product.entity.EventReward$RewardStatus.APPROVED, com.dodam.product.entity.EventReward$RewardStatus.PENDING) " +
           "AND er.expiresAt BETWEEN CURRENT_TIMESTAMP AND :alertEndDate " +
           "AND er.deletedAt IS NULL " +
           "ORDER BY er.expiresAt ASC")
    List<EventReward> findRewardsNeedingExpiryAlert(@Param("alertEndDate") LocalDateTime alertEndDate);
}