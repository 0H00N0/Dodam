package com.dodam.product.repository;

import com.dodam.product.entity.ReviewLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 리뷰 좋아요 Repository 인터페이스
 * 리뷰 좋아요 정보에 대한 데이터 접근 및 쿼리를 담당합니다.
 */
@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    // ========================== 기본 검색 메소드 ==========================

    /**
     * 모든 좋아요 조회 (정렬 지원)
     * @param sort 정렬 조건
     * @return 좋아요 목록
     */
    List<ReviewLike> findAll(Sort sort);

    /**
     * 좋아요 페이징 조회
     * @param pageable 페이징 정보
     * @return 좋아요 페이지
     */
    Page<ReviewLike> findAll(Pageable pageable);

    /**
     * 활성 좋아요만 조회 (실제로 좋아요를 누른 상태)
     * @param pageable 페이징 정보
     * @return 활성 좋아요 페이지
     */
    Page<ReviewLike> findByIsLikedTrue(Pageable pageable);

    /**
     * 취소된 좋아요 조회
     * @param pageable 페이징 정보
     * @return 취소된 좋아요 페이지
     */
    Page<ReviewLike> findByIsLikedFalse(Pageable pageable);

    // ========================== 리뷰별 검색 메소드 ==========================

    /**
     * 특정 리뷰의 모든 좋아요 조회
     * @param reviewId 리뷰 ID
     * @param pageable 페이징 정보
     * @return 해당 리뷰의 좋아요 페이지
     */
    Page<ReviewLike> findByReviewReviewId(Long reviewId, Pageable pageable);

    /**
     * 특정 리뷰의 활성 좋아요 조회
     * @param reviewId 리뷰 ID
     * @param pageable 페이징 정보
     * @return 해당 리뷰의 활성 좋아요 페이지
     */
    Page<ReviewLike> findByReviewReviewIdAndIsLikedTrue(Long reviewId, Pageable pageable);

    /**
     * 특정 리뷰의 활성 좋아요 수 조회
     * @param reviewId 리뷰 ID
     * @return 활성 좋아요 수
     */
    long countByReviewReviewIdAndIsLikedTrue(Long reviewId);

    /**
     * 특정 리뷰의 전체 좋아요 수 조회 (취소된 것 포함)
     * @param reviewId 리뷰 ID
     * @return 전체 좋아요 수
     */
    long countByReviewReviewId(Long reviewId);

    // ========================== 회원별 검색 메소드 ==========================

    /**
     * 회원별 좋아요 조회
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 해당 회원의 좋아요 페이지
     */
    Page<ReviewLike> findByMemberId(Long memberId, Pageable pageable);

    /**
     * 회원별 활성 좋아요 조회
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 해당 회원의 활성 좋아요 페이지
     */
    Page<ReviewLike> findByMemberIdAndIsLikedTrue(Long memberId, Pageable pageable);

    /**
     * 회원별 활성 좋아요 수 조회
     * @param memberId 회원 ID
     * @return 활성 좋아요 수
     */
    long countByMemberIdAndIsLikedTrue(Long memberId);

    /**
     * 회원이 특정 리뷰에 누른 좋아요 조회
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return 좋아요 정보 (Optional)
     */
    Optional<ReviewLike> findByMemberIdAndReviewReviewId(Long memberId, Long reviewId);

    /**
     * 회원이 특정 리뷰에 좋아요를 눌렀는지 확인
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return 좋아요 존재 여부
     */
    boolean existsByMemberIdAndReviewReviewId(Long memberId, Long reviewId);

    /**
     * 회원이 특정 리뷰에 활성 좋아요를 눌렀는지 확인
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return 활성 좋아요 여부
     */
    boolean existsByMemberIdAndReviewReviewIdAndIsLikedTrue(Long memberId, Long reviewId);

    // ========================== 시간 기반 검색 메소드 ==========================

    /**
     * 특정 기간 내 생성된 좋아요 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 기간 내 좋아요 페이지
     */
    Page<ReviewLike> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 특정 기간 내 생성된 활성 좋아요 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 기간 내 활성 좋아요 페이지
     */
    Page<ReviewLike> findByCreatedAtBetweenAndIsLikedTrue(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 최근 좋아요 조회
     * @param pageable 페이징 정보 (size로 개수 제한)
     * @return 최근 좋아요 목록
     */
    Slice<ReviewLike> findByIsLikedTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 특정 일 이후 수정되지 않은 안정된 좋아요 조회
     * @param beforeDate 기준일 (이전에 수정된 것들)
     * @param pageable 페이징 정보
     * @return 안정된 좋아요 목록
     */
    @Query("SELECT rl FROM ReviewLike rl " +
           "WHERE rl.isLiked = true " +
           "AND (rl.updatedAt IS NULL OR rl.updatedAt < :beforeDate)")
    Page<ReviewLike> findStableLikes(@Param("beforeDate") LocalDateTime beforeDate, Pageable pageable);

    // ========================== 복합 검색 메소드 ==========================

    /**
     * 특정 회원의 특정 기간 좋아요 조회
     * @param memberId 회원 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @param isLiked 좋아요 상태
     * @param pageable 페이징 정보
     * @return 조건에 맞는 좋아요 페이지
     */
    Page<ReviewLike> findByMemberIdAndCreatedAtBetweenAndIsLiked(Long memberId, 
                                                                LocalDateTime startDate, 
                                                                LocalDateTime endDate, 
                                                                Boolean isLiked,
                                                                Pageable pageable);

    /**
     * 특정 리뷰의 특정 기간 좋아요 조회
     * @param reviewId 리뷰 ID
     * @param startDate 시작일
     * @param endDate 종료일
     * @param isLiked 좋아요 상태
     * @return 조건에 맞는 좋아요 수
     */
    long countByReviewReviewIdAndCreatedAtBetweenAndIsLiked(Long reviewId, 
                                                           LocalDateTime startDate, 
                                                           LocalDateTime endDate, 
                                                           Boolean isLiked);

    // ========================== 통계 및 집계 쿼리 ==========================

    /**
     * 전체 활성 좋아요 수 조회
     * @return 활성 좋아요 수
     */
    long countByIsLikedTrue();

    /**
     * 전체 취소된 좋아요 수 조회
     * @return 취소된 좋아요 수
     */
    long countByIsLikedFalse();

    /**
     * 리뷰별 좋아요 통계 조회 (상위 N개)
     * @param pageable 페이징 정보
     * @return [리뷰ID, 좋아요수]
     */
    @Query("SELECT rl.review.reviewId, COUNT(rl) " +
           "FROM ReviewLike rl " +
           "WHERE rl.isLiked = true " +
           "GROUP BY rl.review.reviewId " +
           "ORDER BY COUNT(rl) DESC")
    Page<Object[]> getReviewLikeStats(Pageable pageable);

    /**
     * 회원별 좋아요 통계 조회 (상위 N개)
     * @param pageable 페이징 정보
     * @return [회원ID, 총좋아요수]
     */
    @Query("SELECT rl.memberId, COUNT(rl) " +
           "FROM ReviewLike rl " +
           "WHERE rl.isLiked = true " +
           "GROUP BY rl.memberId " +
           "ORDER BY COUNT(rl) DESC")
    Page<Object[]> getMemberLikeStats(Pageable pageable);

    /**
     * 일별 좋아요 통계 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return [날짜, 좋아요수, 취소수]
     */
    @Query("SELECT DATE(rl.createdAt), " +
           "SUM(CASE WHEN rl.isLiked = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN rl.isLiked = false THEN 1 ELSE 0 END) " +
           "FROM ReviewLike rl " +
           "WHERE rl.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(rl.createdAt) " +
           "ORDER BY DATE(rl.createdAt)")
    List<Object[]> getDailyLikeStats(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * 좋아요 전환율 통계 (좋아요 대비 취소 비율)
     * @return [총좋아요수, 총취소수, 전환율]
     */
    @Query("SELECT " +
           "SUM(CASE WHEN rl.isLiked = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN rl.isLiked = false THEN 1 ELSE 0 END), " +
           "ROUND(SUM(CASE WHEN rl.isLiked = false THEN 1.0 ELSE 0 END) / COUNT(*) * 100, 2) " +
           "FROM ReviewLike rl")
    Object[] getLikeConversionStats();

    // ========================== 인기도 및 트렌드 분석 ==========================

    /**
     * 가장 많이 좋아요 받은 리뷰 조회
     * @param pageable 페이징 정보
     * @return 인기 리뷰의 좋아요 목록
     */
    @Query("SELECT rl FROM ReviewLike rl " +
           "WHERE rl.review.reviewId IN (" +
           "   SELECT rl2.review.reviewId " +
           "   FROM ReviewLike rl2 " +
           "   WHERE rl2.isLiked = true " +
           "   GROUP BY rl2.review.reviewId " +
           "   ORDER BY COUNT(rl2) DESC" +
           ") AND rl.isLiked = true " +
           "ORDER BY rl.review.reviewId, rl.createdAt DESC")
    Page<ReviewLike> findMostLikedReviewLikes(Pageable pageable);

    /**
     * 좋아요 토글이 많은 리뷰 조회 (좋아요/취소를 자주 반복한 리뷰)
     * @param pageable 페이징 정보
     * @return [리뷰ID, 토글횟수]
     */
    @Query("SELECT rl.review.reviewId, COUNT(rl) " +
           "FROM ReviewLike rl " +
           "WHERE rl.updatedAt IS NOT NULL " +
           "GROUP BY rl.review.reviewId " +
           "ORDER BY COUNT(rl) DESC")
    Page<Object[]> findMostToggledReviews(Pageable pageable);

    /**
     * 최근 활발한 좋아요 활동을 보인 회원 조회
     * @param since 기준일 (이후)
     * @param pageable 페이징 정보
     * @return [회원ID, 최근좋아요수]
     */
    @Query("SELECT rl.memberId, COUNT(rl) " +
           "FROM ReviewLike rl " +
           "WHERE rl.createdAt >= :since AND rl.isLiked = true " +
           "GROUP BY rl.memberId " +
           "ORDER BY COUNT(rl) DESC")
    Page<Object[]> findActiveMembers(@Param("since") LocalDateTime since, Pageable pageable);

    // ========================== Fetch Join 쿼리 (N+1 문제 해결) ==========================

    /**
     * 좋아요와 리뷰 정보를 fetch join으로 조회
     * @param likeId 좋아요 ID
     * @return 리뷰 정보가 포함된 좋아요
     */
    @Query("SELECT rl FROM ReviewLike rl " +
           "JOIN FETCH rl.review r " +
           "WHERE rl.likeId = :likeId")
    Optional<ReviewLike> findByIdWithReview(@Param("likeId") Long likeId);

    /**
     * 특정 리뷰의 모든 좋아요를 리뷰 정보와 함께 조회
     * @param reviewId 리뷰 ID
     * @param pageable 페이징 정보
     * @return 리뷰 정보가 포함된 좋아요 목록
     */
    @Query("SELECT rl FROM ReviewLike rl " +
           "JOIN FETCH rl.review r " +
           "WHERE rl.review.reviewId = :reviewId")
    Page<ReviewLike> findByReviewIdWithReview(@Param("reviewId") Long reviewId, Pageable pageable);

    // ========================== 수정 및 삭제 메소드 ==========================

    /**
     * 좋아요 상태 토글 (Bulk Update)
     * @param likeId 좋아요 ID
     * @param newStatus 새로운 좋아요 상태
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE ReviewLike rl SET rl.isLiked = :newStatus, rl.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE rl.likeId = :likeId")
    int toggleLikeStatus(@Param("likeId") Long likeId, @Param("newStatus") Boolean newStatus);

    /**
     * 회원의 특정 리뷰에 대한 좋아요 상태 업데이트
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @param newStatus 새로운 좋아요 상태
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE ReviewLike rl SET rl.isLiked = :newStatus, rl.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE rl.memberId = :memberId AND rl.review.reviewId = :reviewId")
    int updateMemberReviewLike(@Param("memberId") Long memberId, 
                              @Param("reviewId") Long reviewId, 
                              @Param("newStatus") Boolean newStatus);

    /**
     * 특정 리뷰의 모든 좋아요 삭제 (리뷰 삭제 시 연쇄 삭제)
     * @param reviewId 리뷰 ID
     * @return 삭제된 좋아요 수
     */
    @Modifying
    @Query("DELETE FROM ReviewLike rl WHERE rl.review.reviewId = :reviewId")
    int deleteByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 특정 회원의 모든 좋아요 삭제 (회원 탈퇴 시)
     * @param memberId 회원 ID
     * @return 삭제된 좋아요 수
     */
    @Modifying
    @Query("DELETE FROM ReviewLike rl WHERE rl.memberId = :memberId")
    int deleteByMemberId(@Param("memberId") Long memberId);

    /**
     * 오래된 취소된 좋아요 데이터 정리 (성능 최적화)
     * @param beforeDate 기준일 (이전 데이터 삭제)
     * @return 삭제된 좋아요 수
     */
    @Modifying
    @Query("DELETE FROM ReviewLike rl " +
           "WHERE rl.isLiked = false AND rl.createdAt < :beforeDate")
    int cleanupOldCancelledLikes(@Param("beforeDate") LocalDateTime beforeDate);

    // ========================== 특수 쿼리 ==========================

    /**
     * 특정 기간 내 생성된 좋아요 수 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 생성된 좋아요 수
     */
    @Query("SELECT COUNT(rl) FROM ReviewLike rl " +
           "WHERE rl.createdAt BETWEEN :startDate AND :endDate AND rl.isLiked = true")
    long countLikesCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);

    /**
     * 중복 좋아요 검사 및 정리를 위한 쿼리
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return 중복된 좋아요 목록
     */
    @Query("SELECT rl FROM ReviewLike rl " +
           "WHERE rl.memberId = :memberId AND rl.review.reviewId = :reviewId " +
           "ORDER BY rl.createdAt DESC")
    List<ReviewLike> findDuplicateLikes(@Param("memberId") Long memberId, 
                                       @Param("reviewId") Long reviewId);

    /**
     * 좋아요 품질 점수 계산 (업데이트 여부 기반)
     * @param reviewId 리뷰 ID
     * @return [좋아요ID, 품질점수]
     */
    @Query("SELECT rl.likeId, " +
           "CASE WHEN rl.updatedAt IS NULL THEN 1.0 " +
           "     ELSE 0.5 " +
           "END as qualityScore " +
           "FROM ReviewLike rl " +
           "WHERE rl.review.reviewId = :reviewId AND rl.isLiked = true " +
           "ORDER BY qualityScore DESC")
    List<Object[]> getLikeQualityScores(@Param("reviewId") Long reviewId);

    /**
     * 회원의 좋아요 패턴 분석 (좋아요를 자주 취소하는지 등)
     * @param memberId 회원 ID
     * @return [총좋아요수, 취소수, 토글비율]
     */
    @Query("SELECT COUNT(rl), " +
           "SUM(CASE WHEN rl.isLiked = false THEN 1 ELSE 0 END), " +
           "AVG(CASE WHEN rl.updatedAt IS NOT NULL THEN 1.0 ELSE 0.0 END) " +
           "FROM ReviewLike rl " +
           "WHERE rl.memberId = :memberId")
    Object[] getMemberLikePattern(@Param("memberId") Long memberId);
}