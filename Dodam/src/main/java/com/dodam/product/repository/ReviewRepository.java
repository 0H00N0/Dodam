package com.dodam.product.repository;

import com.dodam.product.entity.Review;
import com.dodam.product.entity.Review.ReviewStatus;
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
 * 리뷰 Repository 인터페이스
 * 리뷰 정보에 대한 데이터 접근 및 쿼리를 담당합니다.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ========================== 기본 검색 메소드 ==========================

    /**
     * 삭제되지 않은 모든 리뷰 조회 (정렬 지원)
     * @param sort 정렬 조건
     * @return 활성 리뷰 목록
     */
    List<Review> findByDeletedAtIsNull(Sort sort);

    /**
     * 삭제되지 않은 리뷰 페이징 조회
     * @param pageable 페이징 정보
     * @return 활성 리뷰 페이지
     */
    Page<Review> findByDeletedAtIsNull(Pageable pageable);

    /**
     * 리뷰 상태별 조회
     * @param status 리뷰 상태
     * @param pageable 페이징 정보
     * @return 해당 상태의 리뷰 페이지
     */
    Page<Review> findByStatusAndDeletedAtIsNull(ReviewStatus status, Pageable pageable);

    /**
     * 표시 가능한 리뷰 조회 (ACTIVE 상태, 삭제되지 않은)
     * @param pageable 페이징 정보
     * @return 표시 가능한 리뷰 페이지
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.deletedAt IS NULL AND r.status = com.dodam.product.entity.Review$ReviewStatus.ACTIVE")
    Page<Review> findDisplayableReviews(Pageable pageable);

    // ========================== 상품별 검색 메소드 ==========================

    /**
     * 상품별 활성 리뷰 조회
     * @param productId 상품 ID
     * @param pageable 페이징 정보
     * @return 해당 상품의 리뷰 페이지
     */
    Page<Review> findByProductProductIdAndDeletedAtIsNull(Long productId, Pageable pageable);

    /**
     * 상품별 표시 가능한 리뷰 조회
     * @param productId 상품 ID
     * @param pageable 페이징 정보
     * @return 해당 상품의 표시 가능한 리뷰 페이지
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product.productId = :productId " +
           "AND r.deletedAt IS NULL AND r.status = com.dodam.product.entity.Review$ReviewStatus.ACTIVE")
    Page<Review> findDisplayableReviewsByProduct(@Param("productId") Long productId, Pageable pageable);

    /**
     * 상품별 리뷰 수 조회
     * @param productId 상품 ID
     * @return 해당 상품의 리뷰 수
     */
    long countByProductProductIdAndDeletedAtIsNull(Long productId);

    /**
     * 상품별 표시 가능한 리뷰 수 조회
     * @param productId 상품 ID
     * @return 해당 상품의 표시 가능한 리뷰 수
     */
    @Query("SELECT COUNT(r) FROM Review r " +
           "WHERE r.product.productId = :productId " +
           "AND r.deletedAt IS NULL AND r.status = com.dodam.product.entity.Review$ReviewStatus.ACTIVE")
    long countDisplayableReviewsByProduct(@Param("productId") Long productId);

    // ========================== 회원별 검색 메소드 ==========================

    /**
     * 회원별 리뷰 조회
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 해당 회원의 리뷰 페이지
     */
    Page<Review> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

    /**
     * 회원별 리뷰 수 조회
     * @param memberId 회원 ID
     * @return 해당 회원의 리뷰 수
     */
    long countByMemberIdAndDeletedAtIsNull(Long memberId);

    /**
     * 회원이 특정 상품에 작성한 리뷰 조회
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 리뷰 정보 (Optional)
     */
    Optional<Review> findByMemberIdAndProductProductIdAndDeletedAtIsNull(Long memberId, Long productId);

    /**
     * 회원이 특정 상품에 리뷰를 작성했는지 확인
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 리뷰 존재 여부
     */
    boolean existsByMemberIdAndProductProductIdAndDeletedAtIsNull(Long memberId, Long productId);

    // ========================== 평점별 검색 메소드 ==========================

    /**
     * 특정 평점의 리뷰 조회
     * @param rating 평점
     * @param pageable 페이징 정보
     * @return 해당 평점의 리뷰 페이지
     */
    Page<Review> findByRatingAndDeletedAtIsNull(Integer rating, Pageable pageable);

    /**
     * 평점 범위로 리뷰 검색
     * @param minRating 최소 평점
     * @param maxRating 최대 평점
     * @param pageable 페이징 정보
     * @return 평점 범위 내 리뷰 페이지
     */
    Page<Review> findByRatingBetweenAndDeletedAtIsNull(Integer minRating, Integer maxRating, Pageable pageable);

    /**
     * 높은 평점 리뷰 조회 (4점 이상)
     * @param minRating 최소 평점
     * @param pageable 페이징 정보
     * @return 높은 평점 리뷰 페이지
     */
    Page<Review> findByRatingGreaterThanEqualAndDeletedAtIsNull(Integer minRating, Pageable pageable);

    /**
     * 낮은 평점 리뷰 조회 (2점 이하)
     * @param maxRating 최대 평점
     * @param pageable 페이징 정보
     * @return 낮은 평점 리뷰 페이지
     */
    Page<Review> findByRatingLessThanEqualAndDeletedAtIsNull(Integer maxRating, Pageable pageable);

    // ========================== 복합 검색 메소드 ==========================

    /**
     * 제목에 키워드가 포함된 리뷰 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 리뷰 페이지
     */
    Page<Review> findByTitleContainingAndDeletedAtIsNull(String keyword, Pageable pageable);

    /**
     * 내용에 키워드가 포함된 리뷰 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 리뷰 페이지
     */
    Page<Review> findByContentContainingAndDeletedAtIsNull(String keyword, Pageable pageable);

    /**
     * 제목 또는 내용에 키워드가 포함된 리뷰 검색
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 매칭된 리뷰 페이지
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.deletedAt IS NULL AND " +
           "(r.title LIKE CONCAT('%', :keyword, '%') OR r.content LIKE CONCAT('%', :keyword, '%'))")
    Page<Review> searchReviews(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 상품별 특정 평점 이상의 리뷰 조회
     * @param productId 상품 ID
     * @param minRating 최소 평점
     * @param pageable 페이징 정보
     * @return 조건에 맞는 리뷰 페이지
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.product.productId = :productId " +
           "AND r.rating >= :minRating " +
           "AND r.deletedAt IS NULL AND r.status = com.dodam.product.entity.Review$ReviewStatus.ACTIVE")
    Page<Review> findProductReviewsWithMinRating(@Param("productId") Long productId,
                                                 @Param("minRating") Integer minRating,
                                                 Pageable pageable);

    /**
     * 고급 리뷰 검색 (복합 조건)
     * @param productId 상품 ID (null 허용)
     * @param memberId 회원 ID (null 허용)
     * @param minRating 최소 평점 (null 허용)
     * @param maxRating 최대 평점 (null 허용)
     * @param status 리뷰 상태 (null 허용)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 리뷰 페이지
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.deletedAt IS NULL " +
           "AND (:productId IS NULL OR r.product.productId = :productId) " +
           "AND (:memberId IS NULL OR r.memberId = :memberId) " +
           "AND (:minRating IS NULL OR r.rating >= :minRating) " +
           "AND (:maxRating IS NULL OR r.rating <= :maxRating) " +
           "AND (:status IS NULL OR r.status = :status)")
    Page<Review> advancedSearch(@Param("productId") Long productId,
                                @Param("memberId") Long memberId,
                                @Param("minRating") Integer minRating,
                                @Param("maxRating") Integer maxRating,
                                @Param("status") ReviewStatus status,
                                Pageable pageable);

    // ========================== 통계 및 집계 쿼리 ==========================

    /**
     * 전체 활성 리뷰 수 조회
     * @return 활성 리뷰 수
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.deletedAt IS NULL")
    long countActiveReviews();

    /**
     * 상태별 리뷰 수 조회
     * @param status 리뷰 상태
     * @return 해당 상태의 리뷰 수
     */
    long countByStatusAndDeletedAtIsNull(ReviewStatus status);

    /**
     * 평점별 리뷰 통계 조회
     * @return [평점, 리뷰수]
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r " +
           "WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "GROUP BY r.rating " +
           "ORDER BY r.rating DESC")
    List<Object[]> getRatingStats();

    /**
     * 상품별 평균 평점 및 리뷰 수 조회
     * @return [상품ID, 상품명, 평균평점, 리뷰수]
     */
    @Query("SELECT p.productId, p.productName, AVG(r.rating), COUNT(r) " +
           "FROM Product p " +
           "LEFT JOIN p.reviews r ON r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "WHERE p.deletedAt IS NULL " +
           "GROUP BY p.productId, p.productName " +
           "ORDER BY AVG(r.rating) DESC")
    List<Object[]> getProductRatingStats();

    /**
     * 회원별 리뷰 통계 조회
     * @return [회원ID, 총리뷰수, 평균평점]
     */
    @Query("SELECT r.memberId, COUNT(r), AVG(r.rating) " +
           "FROM Review r " +
           "WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "GROUP BY r.memberId " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> getMemberReviewStats();

    /**
     * 전체 리뷰 평점 통계 조회
     * @return [평균평점, 최고평점, 최저평점, 총리뷰수]
     */
    @Query("SELECT AVG(r.rating), MAX(r.rating), MIN(r.rating), COUNT(r) " +
           "FROM Review r WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE'")
    Object[] getOverallRatingStats();

    // ========================== 인기/추천 리뷰 쿼리 ==========================

    /**
     * 최신 리뷰 조회
     * @param pageable 페이징 정보
     * @return 최신 리뷰 목록
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "ORDER BY r.createdAt DESC")
    Slice<Review> findLatestReviews(Pageable pageable);

    /**
     * 좋아요가 많은 인기 리뷰 조회
     * @param pageable 페이징 정보
     * @return 인기 리뷰 목록
     */
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN r.reviewLikes rl ON rl.isLiked = true " +
           "WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "GROUP BY r.reviewId " +
           "ORDER BY COUNT(rl) DESC")
    Slice<Review> findPopularReviews(Pageable pageable);

    /**
     * 도움이 된 리뷰 조회 (좋아요 3개 이상)
     * @param minLikes 최소 좋아요 수
     * @param pageable 페이징 정보
     * @return 도움이 된 리뷰 목록
     */
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN r.reviewLikes rl ON rl.isLiked = true " +
           "WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "GROUP BY r.reviewId " +
           "HAVING COUNT(rl) >= :minLikes " +
           "ORDER BY COUNT(rl) DESC")
    Slice<Review> findHelpfulReviews(@Param("minLikes") Long minLikes, Pageable pageable);

    /**
     * 상세한 리뷰 조회 (내용 길이 기준)
     * @param minLength 최소 내용 길이
     * @param pageable 페이징 정보
     * @return 상세한 리뷰 목록
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "AND LENGTH(r.content) >= :minLength " +
           "ORDER BY LENGTH(r.content) DESC")
    Slice<Review> findDetailedReviews(@Param("minLength") Integer minLength, Pageable pageable);

    // ========================== 좋아요 통계와 함께 조회 ==========================

    /**
     * 리뷰와 좋아요 통계 함께 조회
     * @param reviewId 리뷰 ID
     * @return [리뷰정보, 좋아요수]
     */
    @Query("SELECT r, COUNT(rl) FROM Review r " +
           "LEFT JOIN r.reviewLikes rl ON rl.isLiked = true " +
           "WHERE r.reviewId = :reviewId AND r.deletedAt IS NULL " +
           "GROUP BY r.reviewId")
    Object[] findReviewWithLikeStats(@Param("reviewId") Long reviewId);

    /**
     * 리뷰별 좋아요 통계 목록 조회
     * @param pageable 페이징 정보
     * @return [리뷰ID, 제목, 평점, 좋아요수]
     */
    @Query("SELECT r.reviewId, r.title, r.rating, COUNT(rl) " +
           "FROM Review r " +
           "LEFT JOIN r.reviewLikes rl ON rl.isLiked = true " +
           "WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "GROUP BY r.reviewId, r.title, r.rating " +
           "ORDER BY COUNT(rl) DESC")
    Page<Object[]> findReviewsWithLikeStats(Pageable pageable);

    // ========================== Fetch Join 쿼리 (N+1 문제 해결) ==========================

    /**
     * 리뷰와 상품 정보를 fetch join으로 조회
     * @param reviewId 리뷰 ID
     * @return 상품 정보가 포함된 리뷰
     */
    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.product p " +
           "WHERE r.reviewId = :reviewId AND r.deletedAt IS NULL")
    Optional<Review> findByIdWithProduct(@Param("reviewId") Long reviewId);

    /**
     * 리뷰와 좋아요 목록을 fetch join으로 조회
     * @param reviewId 리뷰 ID
     * @return 좋아요 목록이 포함된 리뷰
     */
    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.reviewLikes rl " +
           "WHERE r.reviewId = :reviewId AND r.deletedAt IS NULL")
    Optional<Review> findByIdWithLikes(@Param("reviewId") Long reviewId);

    // ========================== 수정 및 삭제 메소드 ==========================

    /**
     * 리뷰 상태 업데이트 (Bulk Update)
     * @param reviewId 리뷰 ID
     * @param status 새로운 상태
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Review r SET r.status = :status, r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.reviewId = :reviewId")
    int updateStatus(@Param("reviewId") Long reviewId, @Param("status") ReviewStatus status);

    /**
     * 리뷰 소프트 삭제 (Bulk Update)
     * @param reviewId 리뷰 ID
     * @param deletedAt 삭제 시간
     * @return 업데이트된 행 수
     */
    @Modifying
    @Query("UPDATE Review r SET r.deletedAt = :deletedAt, r.status = com.dodam.product.entity.Review$ReviewStatus.HIDDEN " +
           "WHERE r.reviewId = :reviewId")
    int softDeleteReview(@Param("reviewId") Long reviewId, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 신고된 리뷰 일괄 숨김 처리
     * @return 업데이트된 리뷰 수
     */
    @Modifying
    @Query("UPDATE Review r SET r.status = com.dodam.product.entity.Review$ReviewStatus.HIDDEN, r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.status = com.dodam.product.entity.Review$ReviewStatus.REPORTED AND r.deletedAt IS NULL")
    int hideReportedReviews();

    /**
     * 특정 상품의 모든 리뷰 상태 업데이트
     * @param productId 상품 ID
     * @param status 새로운 상태
     * @return 업데이트된 리뷰 수
     */
    @Modifying
    @Query("UPDATE Review r SET r.status = :status, r.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE r.product.productId = :productId AND r.deletedAt IS NULL")
    int updateAllReviewsStatus(@Param("productId") Long productId, @Param("status") ReviewStatus status);

    // ========================== 특수 쿼리 ==========================

    /**
     * 특정 기간 내 작성된 리뷰 수 조회
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 작성된 리뷰 수
     */
    @Query("SELECT COUNT(r) FROM Review r " +
           "WHERE r.createdAt BETWEEN :startDate AND :endDate " +
           "AND r.deletedAt IS NULL")
    long countReviewsCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * 회원의 최근 리뷰 조회 (상위 N개)
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 회원의 최근 리뷰 목록
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.memberId = :memberId AND r.deletedAt IS NULL " +
           "ORDER BY r.createdAt DESC")
    Slice<Review> findRecentReviewsByMember(@Param("memberId") Long memberId, Pageable pageable);

    /**
     * 수정 가능한 리뷰 조회 (작성 후 24시간 이내)
     * @param memberId 회원 ID
     * @param hoursLimit 수정 가능 시간 제한 (시간 단위)
     * @return 수정 가능한 리뷰 목록
     */
    @Query("SELECT r FROM Review r " +
           "WHERE r.memberId = :memberId AND r.deletedAt IS NULL " +
           "AND r.createdAt >= :timeLimit")
    List<Review> findEditableReviews(@Param("memberId") Long memberId,
                                    @Param("timeLimit") LocalDateTime timeLimit);

    /**
     * 리뷰 품질 점수 조회 (내용 길이 + 좋아요 수 기반)
     * @param pageable 페이징 정보
     * @return [리뷰ID, 품질점수] 목록
     */
    @Query("SELECT r.reviewId, (LENGTH(r.content) / 100.0 + COUNT(rl) * 2) as qualityScore " +
           "FROM Review r " +
           "LEFT JOIN r.reviewLikes rl ON rl.isLiked = true " +
           "WHERE r.deletedAt IS NULL AND r.status = 'ACTIVE' " +
           "GROUP BY r.reviewId " +
           "ORDER BY qualityScore DESC")
    Page<Object[]> findReviewsWithQualityScore(Pageable pageable);
}