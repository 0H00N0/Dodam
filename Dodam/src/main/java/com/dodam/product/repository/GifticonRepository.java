package com.dodam.product.repository;

import com.dodam.product.entity.Gifticon;
import com.dodam.product.entity.Gifticon.GifticonStatus;
import com.dodam.product.entity.Gifticon.TransactionType;
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
 * 기프티콘 Repository 인터페이스
 * 기프티콘 정보에 대한 데이터 접근 및 쿼리를 담당합니다.
 */
@Repository
public interface GifticonRepository extends JpaRepository<Gifticon, Long> {

    // ========================== 기본 검색 메소드 ==========================

    /**
     * 기프티콘 코드로 기프티콘 조회
     * @param gifticonCode 기프티콘 코드
     * @return 기프티콘 정보 (Optional)
     */
    Optional<Gifticon> findByGifticonCode(String gifticonCode);

    /**
     * 기프티콘 코드 존재 여부 확인
     * @param gifticonCode 기프티콘 코드
     * @return 존재 여부
     */
    boolean existsByGifticonCode(String gifticonCode);

    /**
     * 삭제되지 않은 모든 기프티콘 조회 (정렬 지원)
     * @param sort 정렬 조건
     * @return 활성 기프티콘 목록
     */
    List<Gifticon> findByDeletedAtIsNull(Sort sort);

    /**
     * 삭제되지 않은 기프티콘 페이징 조회
     * @param pageable 페이징 정보
     * @return 활성 기프티콘 페이지
     */
    Page<Gifticon> findByDeletedAtIsNull(Pageable pageable);

    /**
     * 삭제된 기프티콘 조회
     * @param pageable 페이징 정보
     * @return 삭제된 기프티콘 페이지
     */
    Page<Gifticon> findByDeletedAtIsNotNull(Pageable pageable);

    // ========================== 회원별 검색 메소드 ==========================

    /**
     * 회원별 기프티콘 조회
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 해당 회원의 기프티콘 페이지
     */
    Page<Gifticon> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    /**
     * 회원별 기프티콘 수 조회
     * @param memberId 회원 ID
     * @return 해당 회원의 기프티콘 수
     */
    long countByMemberIdAndDeletedAtIsNull(Long memberId);

    /**
     * 회원별 특정 상태의 기프티콘 조회
     * @param memberId 회원 ID
     * @param status 기프티콘 상태
     * @param pageable 페이징 정보
     * @return 해당 조건의 기프티콘 페이지
     */
    Page<Gifticon> findByMemberIdAndStatusAndDeletedAtIsNull(Long memberId, GifticonStatus status, Pageable pageable);

    /**
     * 회원의 사용 가능한 기프티콘 조회
     * @param memberId 회원 ID
     * @param now 현재 시간
     * @param pageable 페이징 정보
     * @return 사용 가능한 기프티콘 페이지
     */
    @Query("SELECT g FROM Gifticon g " +
           "WHERE g.memberId = :memberId " +
           "AND g.status = com.dodam.product.entity.Gifticon$GifticonStatus.ACTIVE " +
           "AND g.expiryDate > :now " +
           "AND g.deletedAt IS NULL")
    Page<Gifticon> findUsableGifticons(@Param("memberId") Long memberId, 
                                      @Param("now") LocalDateTime now, 
                                      Pageable pageable);

    // ========================== 상태별 검색 메소드 ==========================

    /**
     * 상태별 기프티콘 조회
     * @param status 기프티콘 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 기프티콘 페이지
     */
    Page<Gifticon> findByStatusAndDeletedAtIsNull(GifticonStatus status, Pageable pageable);

    /**
     * 상태별 기프티콘 수 조회
     * @param status 기프티콘 상태
     * @return 해당 상태의 기프티콘 수
     */
    long countByStatusAndDeletedAtIsNull(GifticonStatus status);

    /**
     * 활성 상태 기프티콘 조회
     * @param pageable 페이징 정보
     * @return 활성 기프티콘 페이지
     */
    @Query("SELECT g FROM Gifticon g " +
           "WHERE g.status = 'ACTIVE' AND g.deletedAt IS NULL")
    Page<Gifticon> findActiveGifticons(Pageable pageable);

    /**
     * 사용 완료된 기프티콘 조회
     * @param pageable 페이징 정보
     * @return 사용 완료 기프티콘 페이지
     */
    Page<Gifticon> findByStatusAndDeletedAtIsNullOrderByUsedAtDesc(GifticonStatus status, Pageable pageable);

    // ========================== 거래 타입별 검색 메소드 ==========================

    /**
     * 거래 타입별 기프티콘 조회
     * @param transactionType 거래 타입
     * @param pageable 페이징 정보
     * @return 해당 거래 타입의 기프티콘 페이지
     */
    Page<Gifticon> findByTransactionTypeAndDeletedAtIsNull(TransactionType transactionType, Pageable pageable);

    /**
     * 거래 타입별 기프티콘 수 조회
     * @param transactionType 거래 타입
     * @return 해당 거래 타입의 기프티콘 수
     */
    long countByTransactionTypeAndDeletedAtIsNull(TransactionType transactionType);

    // ========================== 유효기간별 검색 메소드 ==========================

    /**
     * 만료된 기프티콘 조회
     * @param now 현재 시간
     * @param pageable 페이징 정보
     * @return 만료된 기프티콘 페이지
     */
    Page<Gifticon> findByExpiryDateBeforeAndDeletedAtIsNull(LocalDateTime now, Pageable pageable);

    /**
     * 만료 예정 기프티콘 조회 (N일 이내)
     * @param expiryDate 만료 기준일
     * @param pageable 페이징 정보
     * @return 만료 예정 기프티콘 페이지
     */
    @Query("SELECT g FROM Gifticon g " +
           "WHERE g.expiryDate BETWEEN CURRENT_TIMESTAMP AND :expiryDate " +
           "AND g.status = com.dodam.product.entity.Gifticon$GifticonStatus.ACTIVE " +
           "AND g.deletedAt IS NULL")
    Page<Gifticon> findExpiringSoon(@Param("expiryDate") LocalDateTime expiryDate, Pageable pageable);

    /**
     * 특정 기간 내 만료되는 기프티콘 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 기간 내 만료 기프티콘 페이지
     */
    Page<Gifticon> findByExpiryDateBetweenAndDeletedAtIsNull(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 회원별 만료 예정 기프티콘 조회
     * @param memberId 회원 ID
     * @param expiryDate 만료 기준일
     * @return 만료 예정 기프티콘 목록
     */
    @Query("SELECT g FROM Gifticon g " +
           "WHERE g.memberId = :memberId " +
           "AND g.expiryDate BETWEEN CURRENT_TIMESTAMP AND :expiryDate " +
           "AND g.status = com.dodam.product.entity.Gifticon$GifticonStatus.ACTIVE " +
           "AND g.deletedAt IS NULL " +
           "ORDER BY g.expiryDate ASC")
    List<Gifticon> findMemberExpiringSoon(@Param("memberId") Long memberId, 
                                         @Param("expiryDate") LocalDateTime expiryDate);

    // ========================== 금액별 검색 메소드 ==========================

    /**
     * 금액 범위로 기프티콘 검색
     * @param minAmount 최소 금액
     * @param maxAmount 최대 금액
     * @param pageable 페이징 정보
     * @return 금액 범위 내 기프티콘 페이지
     */
    Page<Gifticon> findByAmountBetweenAndDeletedAtIsNull(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * 특정 금액 이상의 기프티콘 검색
     * @param minAmount 최소 금액
     * @param pageable 페이징 정보
     * @return 금액 이상 기프티콘 페이지
     */
    Page<Gifticon> findByAmountGreaterThanEqualAndDeletedAtIsNull(BigDecimal minAmount, Pageable pageable);

    /**
     * 특정 금액 이하의 기프티콘 검색
     * @param maxAmount 최대 금액
     * @param pageable 페이징 정보
     * @return 금액 이하 기프티콘 페이지
     */
    Page<Gifticon> findByAmountLessThanEqualAndDeletedAtIsNull(BigDecimal maxAmount, Pageable pageable);

    // ========================== 브랜드별 검색 메소드 ==========================

    /**
     * 브랜드별 기프티콘 조회
     * @param brandName 브랜드명
     * @param pageable 페이징 정보
     * @return 해당 브랜드의 기프티콘 페이지
     */
    Page<Gifticon> findByBrandNameAndDeletedAtIsNull(String brandName, Pageable pageable);

    /**
     * 브랜드명에 키워드가 포함된 기프티콘 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 기프티콘 페이지
     */
    Page<Gifticon> findByBrandNameContainingAndDeletedAtIsNull(String keyword, Pageable pageable);

    /**
     * 상품명에 키워드가 포함된 기프티콘 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 기프티콘 페이지
     */
    Page<Gifticon> findByProductNameContainingAndDeletedAtIsNull(String keyword, Pageable pageable);

    // ========================== 복합 검색 메소드 ==========================

    /**
     * 브랜드명 또는 상품명에 키워드가 포함된 기프티콘 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 기프티콘 페이지
     */
    @Query("SELECT g FROM Gifticon g " +
           "WHERE g.deletedAt IS NULL AND " +
           "(g.brandName LIKE CONCAT('%', :keyword, '%') OR g.productName LIKE CONCAT('%', :keyword, '%'))")
    Page<Gifticon> searchGifticons(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 고급 기프티콘 검색 (복합 조건)
     * @param memberId 회원 ID (null 허용)
     * @param status 기프티콘 상태 (null 허용)
     * @param transactionType 거래 타입 (null 허용)
     * @param minAmount 최소 금액 (null 허용)
     * @param maxAmount 최대 금액 (null 허용)
     * @param brandName 브랜드명 (null 허용)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 기프티콘 페이지
     */
    @Query("SELECT g FROM Gifticon g " +
           "WHERE g.deletedAt IS NULL " +
           "AND (:memberId IS NULL OR g.memberId = :memberId) " +
           "AND (:status IS NULL OR g.status = :status) " +
           "AND (:transactionType IS NULL OR g.transactionType = :transactionType) " +
           "AND (:minAmount IS NULL OR g.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR g.amount <= :maxAmount) " +
           "AND (:brandName IS NULL OR g.brandName = :brandName)")
    Page<Gifticon> advancedSearch(@Param("memberId") Long memberId,
                                  @Param("status") GifticonStatus status,
                                  @Param("transactionType") TransactionType transactionType,
                                  @Param("minAmount") BigDecimal minAmount,
                                  @Param("maxAmount") BigDecimal maxAmount,
                                  @Param("brandName") String brandName,
                                  Pageable pageable);

    // ========================== 통계 및 집계 쿼리 ==========================

    /**
     * 전체 활성 기프티콘 수 조회
     * @return 활성 기프티콘 수
     */
    @Query("SELECT COUNT(g) FROM Gifticon g WHERE g.deletedAt IS NULL")
    long countActiveGifticons();

    /**
     * 상태별 기프티콘 통계 조회
     * @return [상태, 기프티콘수]
     */
    @Query("SELECT g.status, COUNT(g) FROM Gifticon g " +
           "WHERE g.deletedAt IS NULL " +
           "GROUP BY g.status " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getStatusStats();

    /**
     * 거래 타입별 기프티콘 통계 조회
     * @return [거래타입, 기프티콘수]
     */
    @Query("SELECT g.transactionType, COUNT(g) FROM Gifticon g " +
           "WHERE g.deletedAt IS NULL " +
           "GROUP BY g.transactionType " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getTransactionTypeStats();

    /**
     * 브랜드별 기프티콘 통계 조회
     * @return [브랜드명, 기프티콘수, 총금액]
     */
    @Query("SELECT g.brandName, COUNT(g), SUM(g.amount) " +
           "FROM Gifticon g " +
           "WHERE g.deletedAt IS NULL " +
           "GROUP BY g.brandName " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getBrandStats();

    /**
     * 회원별 기프티콘 통계 조회 (상위 N명)
     * @param pageable 페이징 정보
     * @return [회원ID, 기프티콘수, 총금액]
     */
    @Query("SELECT g.memberId, COUNT(g), SUM(g.amount) " +
           "FROM Gifticon g " +
           "WHERE g.deletedAt IS NULL " +
           "GROUP BY g.memberId " +
           "ORDER BY COUNT(g) DESC")
    Page<Object[]> getMemberGifticonStats(Pageable pageable);

    /**
     * 금액별 통계 조회
     * @return [총금액, 평균금액, 최고금액, 최저금액]
     */
    @Query("SELECT SUM(g.amount), AVG(g.amount), MAX(g.amount), MIN(g.amount) " +
           "FROM Gifticon g WHERE g.deletedAt IS NULL")
    Object[] getAmountStats();

    // ========================== 만료 관련 통계 쿼리 ==========================

    /**
     * 만료 예정 기프티콘 수 조회 (특정 날짜까지)
     * @param endDate 종료 날짜
     * @return 만료 예정 기프티콘 수
     */
    @Query("SELECT COUNT(g) FROM Gifticon g " +
           "WHERE g.expiryDate BETWEEN CURRENT_TIMESTAMP AND :endDate " +
           "AND g.status = com.dodam.product.entity.Gifticon$GifticonStatus.ACTIVE " +
           "AND g.deletedAt IS NULL")
    long countExpiringSoon(@Param("endDate") LocalDateTime endDate);

    /**
     * 만료된 기프티콘 수 조회
     * @return 만료된 기프티콘 수
     */
    @Query("SELECT COUNT(g) FROM Gifticon g " +
           "WHERE g.expiryDate < CURRENT_TIMESTAMP " +
           "AND g.deletedAt IS NULL")
    long countExpiredGifticons();

    /**
     * 일별 만료 통계 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return [날짜, 만료수]
     */
    @Query("SELECT DATE(g.expiryDate), COUNT(g) " +
           "FROM Gifticon g " +
           "WHERE g.expiryDate BETWEEN :startDate AND :endDate " +
           "AND g.deletedAt IS NULL " +
           "GROUP BY DATE(g.expiryDate) " +
           "ORDER BY DATE(g.expiryDate)")
    List<Object[]> getDailyExpiryStats(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    // ========================== 사용 패턴 분석 쿼리 ==========================

    /**
     * 최근 사용된 기프티콘 조회
     * @param pageable 페이징 정보
     * @return 최근 사용 기프티콘 목록
     */
    @Query("SELECT g FROM Gifticon g " +
           "WHERE g.status = 'USED' AND g.usedAt IS NOT NULL " +
           "AND g.deletedAt IS NULL " +
           "ORDER BY g.usedAt DESC")
    Slice<Gifticon> findRecentlyUsedGifticons(Pageable pageable);

    /**
     * 사용처별 통계 조회
     * @return [사용처, 사용수]
     */
    @Query("SELECT g.usedPlace, COUNT(g) " +
           "FROM Gifticon g " +
           "WHERE g.status = 'USED' AND g.usedPlace IS NOT NULL " +
           "AND g.deletedAt IS NULL " +
           "GROUP BY g.usedPlace " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getUsedPlaceStats();

    /**
     * 월별 사용 통계 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return [연월, 사용수, 총금액]
     */
    @Query("SELECT SUBSTRING(CAST(g.usedAt AS string), 1, 7), COUNT(g), SUM(g.amount) " +
           "FROM Gifticon g " +
           "WHERE g.usedAt BETWEEN :startDate AND :endDate " +
           "AND g.status = com.dodam.product.entity.Gifticon$GifticonStatus.USED " +
           "AND g.deletedAt IS NULL " +
           "GROUP BY SUBSTRING(CAST(g.usedAt AS string), 1, 7) " +
           "ORDER BY SUBSTRING(CAST(g.usedAt AS string), 1, 7)")
    List<Object[]> getMonthlyUsageStats(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    // ========================== 수정 및 삭제 메소드 ==========================

    /**
     * 기프티콘 상태 업데이트 (Bulk Update)
     * @param gifticonId 기프티콘 ID
     * @param status 새로운 상태
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Gifticon g SET g.status = :status, g.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE g.gifticonId = :gifticonId")
    int updateStatus(@Param("gifticonId") Long gifticonId, @Param("status") GifticonStatus status);

    /**
     * 기프티콘 사용 처리 (Bulk Update)
     * @param gifticonId 기프티콘 ID
     * @param usedPlace 사용처
     * @param usedAt 사용 시간
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Gifticon g SET g.status = com.dodam.product.entity.Gifticon$GifticonStatus.USED, g.transactionType = com.dodam.product.entity.Gifticon$TransactionType.USED, " +
           "g.usedPlace = :usedPlace, g.usedAt = :usedAt, g.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE g.gifticonId = :gifticonId")
    int markAsUsed(@Param("gifticonId") Long gifticonId, 
                   @Param("usedPlace") String usedPlace, 
                   @Param("usedAt") LocalDateTime usedAt);

    /**
     * 기프티콘 양도 처리 (Bulk Update)
     * @param gifticonId 기프티콘 ID
     * @param newMemberId 새로운 소유자 ID
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Gifticon g SET g.memberId = :newMemberId, g.status = com.dodam.product.entity.Gifticon$GifticonStatus.TRANSFERRED, " +
           "g.transactionType = com.dodam.product.entity.Gifticon$TransactionType.TRANSFERRED, g.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE g.gifticonId = :gifticonId")
    int transferGifticon(@Param("gifticonId") Long gifticonId, @Param("newMemberId") Long newMemberId);

    /**
     * 만료된 기프티콘 일괄 만료 처리
     * @param now 현재 시간
     * @return 업데이트된 기프티콘 수
     */
    @Modifying
    @Query("UPDATE Gifticon g SET g.status = com.dodam.product.entity.Gifticon$GifticonStatus.EXPIRED, g.transactionType = com.dodam.product.entity.Gifticon$TransactionType.EXPIRED, " +
           "g.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE g.expiryDate < :now AND g.status = com.dodam.product.entity.Gifticon$GifticonStatus.ACTIVE AND g.deletedAt IS NULL")
    int expireOutdatedGifticons(@Param("now") LocalDateTime now);

    /**
     * 기프티콘 소프트 삭제 (Bulk Update)
     * @param gifticonId 기프티콘 ID
     * @param deletedAt 삭제 시간
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Gifticon g SET g.deletedAt = :deletedAt, g.status = com.dodam.product.entity.Gifticon$GifticonStatus.SUSPENDED " +
           "WHERE g.gifticonId = :gifticonId")
    int softDeleteGifticon(@Param("gifticonId") Long gifticonId, @Param("deletedAt") LocalDateTime deletedAt);

    // ========================== 특수 쿼리 ==========================

    /**
     * 특정 기간 내 생성된 기프티콘 수 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 생성된 기프티콘 수
     */
    @Query("SELECT COUNT(g) FROM Gifticon g " +
           "WHERE g.createdAt BETWEEN :startDate AND :endDate")
    long countGifticonsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 기프티콘 코드 중복 검사 (대소문자 무시)
     * @param gifticonCode 검사할 기프티콘 코드
     * @param excludeId 제외할 기프티콘 ID (수정 시 사용)
     * @return 중복 여부
     */
    @Query("SELECT COUNT(g) > 0 FROM Gifticon g " +
           "WHERE LOWER(g.gifticonCode) = LOWER(:gifticonCode) " +
           "AND (:excludeId IS NULL OR g.gifticonId != :excludeId)")
    boolean existsByGifticonCodeIgnoreCase(@Param("gifticonCode") String gifticonCode, 
                                          @Param("excludeId") Long excludeId);

    /**
     * 회원의 총 기프티콘 가치 조회
     * @param memberId 회원 ID
     * @return [활성기프티콘총액, 사용된기프티콘총액]
     */
    @Query("SELECT " +
           "SUM(CASE WHEN g.status = 'ACTIVE' THEN g.amount ELSE 0 END), " +
           "SUM(CASE WHEN g.status = 'USED' THEN g.amount ELSE 0 END) " +
           "FROM Gifticon g " +
           "WHERE g.memberId = :memberId AND g.deletedAt IS NULL")
    Object[] getMemberGifticonValue(@Param("memberId") Long memberId);

    /**
     * 기프티콘 사용률 통계 조회 (브랜드별)
     * @return [브랜드명, 총발급수, 사용수, 사용률]
     */
    @Query("SELECT g.brandName, COUNT(g), " +
           "SUM(CASE WHEN g.status = 'USED' THEN 1 ELSE 0 END), " +
           "ROUND(SUM(CASE WHEN g.status = 'USED' THEN 1.0 ELSE 0 END) / COUNT(g) * 100, 2) " +
           "FROM Gifticon g " +
           "WHERE g.deletedAt IS NULL " +
           "GROUP BY g.brandName " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> getBrandUsageRateStats();

    /**
     * 임박한 만료 알림이 필요한 기프티콘 조회
     * @param alertEndDate 알림 종료 날짜
     * @return 알림 대상 기프티콘 목록
     */
    @Query("SELECT g FROM Gifticon g " +
           "WHERE g.status = com.dodam.product.entity.Gifticon$GifticonStatus.ACTIVE " +
           "AND g.expiryDate BETWEEN CURRENT_TIMESTAMP AND :alertEndDate " +
           "AND g.deletedAt IS NULL " +
           "ORDER BY g.expiryDate ASC")
    List<Gifticon> findGifticonsNeedingExpiryAlert(@Param("alertEndDate") LocalDateTime alertEndDate);
}