package com.dodam.product.service;

import com.dodam.product.dto.request.ReviewLikeRequestDto;
import com.dodam.product.dto.response.ReviewLikeResponseDto;
import com.dodam.product.dto.statistics.ReviewLikeStatisticsDto;
import com.dodam.product.entity.Review;
import com.dodam.product.entity.ReviewLike;
import com.dodam.product.exception.*;
import com.dodam.product.repository.ReviewLikeRepository;
import com.dodam.product.repository.ReviewRepository;
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
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 리뷰 좋아요 비즈니스 로직을 처리하는 Service 클래스
 * 좋아요/취소, 통계, 분석, 검색 등의 기능을 제공합니다.
 * 
 * 주요 기능:
 * - 좋아요/취소 토글 기능
 * - 회원별/리뷰별 좋아요 관리
 * - 좋아요 통계 및 분석
 * - 인기도 및 트렌드 분석
 * - 데이터 품질 관리
 * 
 * @author Dodam Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
@Validated
public class ReviewLikeService {

    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewRepository reviewRepository;

    // 좋아요 안정성 체크 기준 시간 (시간)
    private static final int STABILITY_HOURS = 24;
    
    // 캐시 정리 주기를 위한 오래된 데이터 기준 (일)
    private static final int CLEANUP_DAYS = 90;

    // ========================== 기본 CRUD 메소드 ==========================

    /**
     * 좋아요 토글 (좋아요 ↔ 취소)
     * 회원이 리뷰에 대해 좋아요를 누르거나 취소하는 기능
     * 
     * @param memberId 회원 ID (필수)
     * @param reviewId 리뷰 ID (필수)
     * @return 토글 결과 정보 (현재 좋아요 상태)
     * @throws ResourceNotFoundException 리뷰가 존재하지 않는 경우
     * @throws ValidationException 회원 ID 또는 리뷰 ID가 유효하지 않은 경우
     */
    @Transactional
    @CacheEvict(value = {"reviewLikes", "reviewLikeStats", "popularReviews"}, allEntries = true)
    public ReviewLikeResponseDto toggleLike(@NotNull @Positive Long memberId, 
                                           @NotNull @Positive Long reviewId) {
        
        log.info("좋아요 토글 시작: 회원={}, 리뷰={}", memberId, reviewId);

        // 1. 리뷰 존재 여부 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰를 찾을 수 없습니다: " + reviewId));

        // 2. 회원의 기존 좋아요 검색
        Optional<ReviewLike> existingLike = reviewLikeRepository.findByMemberIdAndReviewReviewId(memberId, reviewId);

        ReviewLike reviewLike;
        boolean isNewLike;
        
        if (existingLike.isPresent()) {
            // 기존 좋아요가 있는 경우 토글
            reviewLike = existingLike.get();
            reviewLike.toggle();
            isNewLike = false;
            
            log.debug("기존 좋아요 토글: {} -> {}", !reviewLike.isLiked(), reviewLike.isLiked());
        } else {
            // 새로운 좋아요 생성
            reviewLike = ReviewLike.builder()
                    .memberId(memberId)
                    .review(review)
                    .isLiked(true)
                    .build();
            isNewLike = true;
            
            log.debug("새로운 좋아요 생성");
        }

        // 3. 저장
        reviewLike = reviewLikeRepository.save(reviewLike);
        
        // 4. 결과 반환
        ReviewLikeResponseDto result = ReviewLikeResponseDto.fromEntity(reviewLike);
        
        log.info("좋아요 토글 완료: 회원={}, 리뷰={}, 상태={}, 신규={}", 
                memberId, reviewId, reviewLike.isLiked(), isNewLike);
        
        return result;
    }

    /**
     * 좋아요 ID로 단건 조회
     * 
     * @param likeId 좋아요 ID
     * @return 좋아요 정보
     * @throws ResourceNotFoundException 좋아요를 찾을 수 없는 경우
     */
    @Cacheable(value = "reviewLikes", key = "#likeId")
    public ReviewLikeResponseDto getLikeById(@NotNull @Positive Long likeId) {
        
        log.debug("좋아요 단건 조회: {}", likeId);
        
        ReviewLike reviewLike = reviewLikeRepository.findByIdWithReview(likeId)
                .orElseThrow(() -> new ResourceNotFoundException("좋아요를 찾을 수 없습니다: " + likeId));
        
        return ReviewLikeResponseDto.fromEntity(reviewLike);
    }

    /**
     * 회원이 특정 리뷰에 좋아요를 눌렀는지 확인
     * 
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return 좋아요 정보 (Optional)
     */
    @Cacheable(value = "reviewLikes", key = "'member:' + #memberId + ':review:' + #reviewId")
    public Optional<ReviewLikeResponseDto> getMemberReviewLike(@NotNull @Positive Long memberId, 
                                                              @NotNull @Positive Long reviewId) {
        
        log.debug("회원 리뷰 좋아요 조회: 회원={}, 리뷰={}", memberId, reviewId);
        
        return reviewLikeRepository.findByMemberIdAndReviewReviewId(memberId, reviewId)
                .map(ReviewLikeResponseDto::fromEntity);
    }

    /**
     * 회원이 특정 리뷰에 활성 좋아요를 눌렀는지 확인
     * 
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return 활성 좋아요 여부
     */
    @Cacheable(value = "reviewLikes", key = "'active:' + #memberId + ':' + #reviewId")
    public boolean isActiveLike(@NotNull @Positive Long memberId, 
                               @NotNull @Positive Long reviewId) {
        
        log.debug("활성 좋아요 확인: 회원={}, 리뷰={}", memberId, reviewId);
        
        return reviewLikeRepository.existsByMemberIdAndReviewReviewIdAndIsLikedTrue(memberId, reviewId);
    }

    // ========================== 검색 및 조회 메소드 ==========================

    /**
     * 전체 좋아요 목록 조회 (페이징)
     * 
     * @param pageable 페이징 정보
     * @return 좋아요 목록 페이지
     */
    public Page<ReviewLikeResponseDto> getAllLikes(Pageable pageable) {
        
        log.debug("전체 좋아요 목록 조회: {}", pageable);
        
        return reviewLikeRepository.findAll(pageable)
                .map(ReviewLikeResponseDto::fromEntity);
    }

    /**
     * 활성 좋아요만 조회 (실제로 좋아요를 누른 상태)
     * 
     * @param pageable 페이징 정보
     * @return 활성 좋아요 목록 페이지
     */
    public Page<ReviewLikeResponseDto> getActiveLikes(Pageable pageable) {
        
        log.debug("활성 좋아요 목록 조회: {}", pageable);
        
        return reviewLikeRepository.findByIsLikedTrue(pageable)
                .map(ReviewLikeResponseDto::fromEntity);
    }

    /**
     * 특정 리뷰의 좋아요 목록 조회
     * 
     * @param reviewId 리뷰 ID
     * @param pageable 페이징 정보
     * @return 해당 리뷰의 좋아요 목록
     */
    public Page<ReviewLikeResponseDto> getReviewLikes(@NotNull @Positive Long reviewId, 
                                                     Pageable pageable) {
        
        log.debug("리뷰별 좋아요 조회: 리뷰={}, {}", reviewId, pageable);
        
        return reviewLikeRepository.findByReviewReviewIdAndIsLikedTrue(reviewId, pageable)
                .map(ReviewLikeResponseDto::fromEntity);
    }

    /**
     * 특정 회원의 좋아요 목록 조회
     * 
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 해당 회원의 좋아요 목록
     */
    public Page<ReviewLikeResponseDto> getMemberLikes(@NotNull @Positive Long memberId, 
                                                     Pageable pageable) {
        
        log.debug("회원별 좋아요 조회: 회원={}, {}", memberId, pageable);
        
        return reviewLikeRepository.findByMemberIdAndIsLikedTrue(memberId, pageable)
                .map(ReviewLikeResponseDto::fromEntity);
    }

    /**
     * 최근 좋아요 목록 조회
     * 
     * @param pageable 페이징 정보 (size로 개수 제한)
     * @return 최근 좋아요 목록
     */
    @Cacheable(value = "reviewLikes", key = "'recent:' + #pageable.pageSize")
    public List<ReviewLikeResponseDto> getRecentLikes(Pageable pageable) {
        
        log.debug("최근 좋아요 조회: {}", pageable);
        
        Slice<ReviewLike> recentLikes = reviewLikeRepository.findByIsLikedTrueOrderByCreatedAtDesc(pageable);
        
        return recentLikes.stream()
                .map(ReviewLikeResponseDto::fromEntity)
                .toList();
    }

    /**
     * 특정 기간 내 좋아요 조회
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @param pageable 페이징 정보
     * @return 기간 내 좋아요 목록
     */
    public Page<ReviewLikeResponseDto> getLikesByDateRange(LocalDateTime startDate, 
                                                          LocalDateTime endDate, 
                                                          Pageable pageable) {
        
        log.debug("기간별 좋아요 조회: {} ~ {}, {}", startDate, endDate, pageable);
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("시작일은 종료일보다 이전이어야 합니다.");
        }
        
        return reviewLikeRepository.findByCreatedAtBetweenAndIsLikedTrue(startDate, endDate, pageable)
                .map(ReviewLikeResponseDto::fromEntity);
    }

    // ========================== 통계 및 분석 메소드 ==========================

    /**
     * 리뷰별 좋아요 통계 조회
     * 
     * @param pageable 페이징 정보
     * @return 리뷰별 좋아요 통계 목록
     */
    @Cacheable(value = "reviewLikeStats", key = "'review:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ReviewLikeStatisticsDto.ReviewLikeStats> getReviewLikeStatistics(Pageable pageable) {
        
        log.debug("리뷰별 좋아요 통계 조회: {}", pageable);
        
        Page<Object[]> stats = reviewLikeRepository.getReviewLikeStats(pageable);
        
        return stats.map(row -> ReviewLikeStatisticsDto.ReviewLikeStats.builder()
                .reviewId((Long) row[0])
                .likeCount((Long) row[1])
                .build());
    }

    /**
     * 회원별 좋아요 통계 조회
     * 
     * @param pageable 페이징 정보
     * @return 회원별 좋아요 통계 목록
     */
    @Cacheable(value = "reviewLikeStats", key = "'member:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ReviewLikeStatisticsDto.MemberLikeStats> getMemberLikeStatistics(Pageable pageable) {
        
        log.debug("회원별 좋아요 통계 조회: {}", pageable);
        
        Page<Object[]> stats = reviewLikeRepository.getMemberLikeStats(pageable);
        
        return stats.map(row -> ReviewLikeStatisticsDto.MemberLikeStats.builder()
                .memberId((Long) row[0])
                .totalLikes((Long) row[1])
                .build());
    }

    /**
     * 일별 좋아요 통계 조회
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 일별 좋아요 통계 목록
     */
    public List<ReviewLikeStatisticsDto.DailyLikeStats> getDailyLikeStatistics(LocalDateTime startDate, 
                                                                               LocalDateTime endDate) {
        
        log.debug("일별 좋아요 통계 조회: {} ~ {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("시작일은 종료일보다 이전이어야 합니다.");
        }
        
        List<Object[]> stats = reviewLikeRepository.getDailyLikeStats(startDate, endDate);
        
        return stats.stream()
                .map(row -> ReviewLikeStatisticsDto.DailyLikeStats.builder()
                        .date((java.sql.Date) row[0])
                        .likeCount(((Number) row[1]).longValue())
                        .cancelCount(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    /**
     * 좋아요 전환율 통계 조회 (좋아요 대비 취소 비율)
     * 
     * @return 전환율 통계 정보
     */
    @Cacheable(value = "reviewLikeStats", key = "'conversion'")
    public ReviewLikeStatisticsDto.ConversionStats getLikeConversionStatistics() {
        
        log.debug("좋아요 전환율 통계 조회");
        
        Object[] stats = reviewLikeRepository.getLikeConversionStats();
        
        if (stats == null || stats[0] == null) {
            return ReviewLikeStatisticsDto.ConversionStats.builder()
                    .totalLikes(0L)
                    .totalCancels(0L)
                    .conversionRate(0.0)
                    .build();
        }
        
        return ReviewLikeStatisticsDto.ConversionStats.builder()
                .totalLikes(((Number) stats[0]).longValue())
                .totalCancels(((Number) stats[1]).longValue())
                .conversionRate(stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0)
                .build();
    }

    /**
     * 특정 리뷰의 좋아요 수 조회 (캐시 적용)
     * 
     * @param reviewId 리뷰 ID
     * @return 좋아요 수
     */
    @Cacheable(value = "reviewLikeStats", key = "'count:' + #reviewId")
    public long getReviewLikeCount(@NotNull @Positive Long reviewId) {
        
        log.debug("리뷰 좋아요 수 조회: {}", reviewId);
        
        return reviewLikeRepository.countByReviewReviewIdAndIsLikedTrue(reviewId);
    }

    /**
     * 특정 회원의 총 좋아요 수 조회
     * 
     * @param memberId 회원 ID
     * @return 총 좋아요 수
     */
    @Cacheable(value = "reviewLikeStats", key = "'memberCount:' + #memberId")
    public long getMemberLikeCount(@NotNull @Positive Long memberId) {
        
        log.debug("회원 좋아요 수 조회: {}", memberId);
        
        return reviewLikeRepository.countByMemberIdAndIsLikedTrue(memberId);
    }

    // ========================== 인기도 및 트렌드 분석 메소드 ==========================

    /**
     * 가장 많이 좋아요 받은 리뷰의 좋아요 목록 조회
     * 
     * @param pageable 페이징 정보
     * @return 인기 리뷰의 좋아요 목록
     */
    @Cacheable(value = "popularReviews", key = "'mostLiked:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<ReviewLikeResponseDto> getMostLikedReviewLikes(Pageable pageable) {
        
        log.debug("인기 리뷰 좋아요 조회: {}", pageable);
        
        return reviewLikeRepository.findMostLikedReviewLikes(pageable)
                .map(ReviewLikeResponseDto::fromEntity);
    }

    /**
     * 좋아요 토글이 많은 리뷰 조회 (논란이 많은 리뷰)
     * 
     * @param pageable 페이징 정보
     * @return 토글이 많은 리뷰 통계 목록
     */
    public Page<ReviewLikeStatisticsDto.ToggleStats> getMostToggledReviews(Pageable pageable) {
        
        log.debug("토글이 많은 리뷰 조회: {}", pageable);
        
        Page<Object[]> stats = reviewLikeRepository.findMostToggledReviews(pageable);
        
        return stats.map(row -> ReviewLikeStatisticsDto.ToggleStats.builder()
                .reviewId((Long) row[0])
                .toggleCount((Long) row[1])
                .build());
    }

    /**
     * 최근 활발한 좋아요 활동을 보인 회원 조회
     * 
     * @param since 기준일 (이후)
     * @param pageable 페이징 정보
     * @return 활발한 회원 목록
     */
    public Page<ReviewLikeStatisticsDto.ActiveMemberStats> getActiveMembers(LocalDateTime since, 
                                                                           Pageable pageable) {
        
        log.debug("활발한 회원 조회: 기준일={}, {}", since, pageable);
        
        Page<Object[]> stats = reviewLikeRepository.findActiveMembers(since, pageable);
        
        return stats.map(row -> ReviewLikeStatisticsDto.ActiveMemberStats.builder()
                .memberId((Long) row[0])
                .recentLikeCount((Long) row[1])
                .since(since)
                .build());
    }

    /**
     * 안정된 좋아요 목록 조회 (일정 시간 이상 변경되지 않은 좋아요)
     * 
     * @param pageable 페이징 정보
     * @return 안정된 좋아요 목록
     */
    public Page<ReviewLikeResponseDto> getStableLikes(Pageable pageable) {
        
        log.debug("안정된 좋아요 조회: {}", pageable);
        
        LocalDateTime stabilityDeadline = LocalDateTime.now().minusHours(STABILITY_HOURS);
        
        return reviewLikeRepository.findStableLikes(stabilityDeadline, pageable)
                .map(ReviewLikeResponseDto::fromEntity);
    }

    // ========================== 품질 관리 메소드 ==========================

    /**
     * 좋아요 품질 점수 조회 (업데이트 여부 기반)
     * 
     * @param reviewId 리뷰 ID
     * @return 좋아요 품질 점수 목록
     */
    public List<ReviewLikeStatisticsDto.QualityScore> getLikeQualityScores(@NotNull @Positive Long reviewId) {
        
        log.debug("좋아요 품질 점수 조회: {}", reviewId);
        
        List<Object[]> scores = reviewLikeRepository.getLikeQualityScores(reviewId);
        
        return scores.stream()
                .map(row -> ReviewLikeStatisticsDto.QualityScore.builder()
                        .likeId((Long) row[0])
                        .qualityScore(((Number) row[1]).doubleValue())
                        .build())
                .toList();
    }

    /**
     * 회원의 좋아요 패턴 분석
     * 
     * @param memberId 회원 ID
     * @return 좋아요 패턴 분석 결과
     */
    public ReviewLikeStatisticsDto.LikePattern getMemberLikePattern(@NotNull @Positive Long memberId) {
        
        log.debug("회원 좋아요 패턴 분석: {}", memberId);
        
        Object[] pattern = reviewLikeRepository.getMemberLikePattern(memberId);
        
        if (pattern == null || pattern[0] == null) {
            return ReviewLikeStatisticsDto.LikePattern.builder()
                    .memberId(memberId)
                    .totalLikes(0L)
                    .cancelCount(0L)
                    .toggleRatio(0.0)
                    .build();
        }
        
        return ReviewLikeStatisticsDto.LikePattern.builder()
                .memberId(memberId)
                .totalLikes(((Number) pattern[0]).longValue())
                .cancelCount(((Number) pattern[1]).longValue())
                .toggleRatio(pattern[2] != null ? ((Number) pattern[2]).doubleValue() : 0.0)
                .build();
    }

    // ========================== 데이터 관리 메소드 ==========================

    /**
     * 중복 좋아요 검사 및 정리
     * 
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return 정리된 중복 좋아요 수
     */
    @Transactional
    public int cleanupDuplicateLikes(@NotNull @Positive Long memberId, 
                                    @NotNull @Positive Long reviewId) {
        
        log.info("중복 좋아요 정리 시작: 회원={}, 리뷰={}", memberId, reviewId);
        
        List<ReviewLike> duplicates = reviewLikeRepository.findDuplicateLikes(memberId, reviewId);
        
        if (duplicates.size() <= 1) {
            log.debug("중복 좋아요 없음");
            return 0;
        }
        
        // 첫 번째(최신)를 제외하고 나머지 삭제
        List<ReviewLike> toDelete = duplicates.subList(1, duplicates.size());
        reviewLikeRepository.deleteAll(toDelete);
        
        int cleanedCount = toDelete.size();
        
        log.info("중복 좋아요 정리 완료: {}개 삭제", cleanedCount);
        
        return cleanedCount;
    }

    /**
     * 오래된 취소된 좋아요 데이터 정리 (성능 최적화)
     * 
     * @return 정리된 좋아요 수
     */
    @Transactional
    @CacheEvict(value = {"reviewLikes", "reviewLikeStats", "popularReviews"}, allEntries = true)
    public int cleanupOldCancelledLikes() {
        
        log.info("오래된 취소 좋아요 정리 시작");
        
        LocalDateTime cleanupDate = LocalDateTime.now().minusDays(CLEANUP_DAYS);
        int cleanedCount = reviewLikeRepository.cleanupOldCancelledLikes(cleanupDate);
        
        log.info("오래된 취소 좋아요 정리 완료: {}개 삭제", cleanedCount);
        
        return cleanedCount;
    }

    /**
     * 특정 리뷰의 모든 좋아요 삭제 (리뷰 삭제 시 연쇄 삭제)
     * 
     * @param reviewId 리뷰 ID
     * @return 삭제된 좋아요 수
     */
    @Transactional
    @CacheEvict(value = {"reviewLikes", "reviewLikeStats", "popularReviews"}, allEntries = true)
    public int deleteReviewLikes(@NotNull @Positive Long reviewId) {
        
        log.info("리뷰 좋아요 전체 삭제: {}", reviewId);
        
        int deletedCount = reviewLikeRepository.deleteByReviewId(reviewId);
        
        log.info("리뷰 좋아요 삭제 완료: {}개 삭제", deletedCount);
        
        return deletedCount;
    }

    /**
     * 특정 회원의 모든 좋아요 삭제 (회원 탈퇴 시)
     * 
     * @param memberId 회원 ID
     * @return 삭제된 좋아요 수
     */
    @Transactional
    @CacheEvict(value = {"reviewLikes", "reviewLikeStats", "popularReviews"}, allEntries = true)
    public int deleteMemberLikes(@NotNull @Positive Long memberId) {
        
        log.info("회원 좋아요 전체 삭제: {}", memberId);
        
        int deletedCount = reviewLikeRepository.deleteByMemberId(memberId);
        
        log.info("회원 좋아요 삭제 완료: {}개 삭제", deletedCount);
        
        return deletedCount;
    }

    // ========================== 비즈니스 로직 메소드 ==========================

    /**
     * 회원이 좋아요를 누를 수 있는지 확인
     * 
     * @param memberId 회원 ID
     * @param reviewId 리뷰 ID
     * @return 좋아요 가능 여부
     */
    public boolean canLikeReview(@NotNull @Positive Long memberId, 
                                @NotNull @Positive Long reviewId) {
        
        log.debug("좋아요 가능 여부 확인: 회원={}, 리뷰={}", memberId, reviewId);
        
        // 1. 리뷰 존재 여부 확인
        boolean reviewExists = reviewRepository.existsById(reviewId);
        if (!reviewExists) {
            return false;
        }
        
        // 2. 자신의 리뷰인지 확인 (자신의 리뷰에는 좋아요 불가)
        Optional<Review> review = reviewRepository.findById(reviewId);
        if (review.isPresent() && memberId.equals(review.get().getMemberId())) {
            return false;
        }
        
        // 3. 숨겨진 리뷰인지 확인 (숨겨진 리뷰에는 좋아요 불가)
        if (review.isPresent() && review.get().isHidden()) {
            return false;
        }
        
        return true;
    }

    /**
     * 리뷰의 인기도 점수 계산 (좋아요 수 + 가중치)
     * 
     * @param reviewId 리뷰 ID
     * @return 인기도 점수
     */
    public double calculatePopularityScore(@NotNull @Positive Long reviewId) {
        
        log.debug("리뷰 인기도 점수 계산: {}", reviewId);
        
        long likeCount = getReviewLikeCount(reviewId);
        
        if (likeCount == 0) {
            return 0.0;
        }
        
        // 품질 점수를 반영한 가중 점수 계산
        List<ReviewLikeStatisticsDto.QualityScore> qualityScores = getLikeQualityScores(reviewId);
        
        double totalWeight = qualityScores.stream()
                .mapToDouble(ReviewLikeStatisticsDto.QualityScore::getQualityScore)
                .sum();
        
        // 기본 좋아요 수 + 품질 가중치
        double popularityScore = likeCount + (totalWeight * 0.5);
        
        log.debug("인기도 점수 계산 완료: 좋아요={}, 가중치={}, 점수={}", 
                likeCount, totalWeight, popularityScore);
        
        return popularityScore;
    }

    /**
     * 특정 기간 내 좋아요 수 조회
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 기간 내 좋아요 수
     */
    public long countLikesInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        
        log.debug("기간별 좋아요 수 조회: {} ~ {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("시작일은 종료일보다 이전이어야 합니다.");
        }
        
        return reviewLikeRepository.countLikesCreatedBetween(startDate, endDate);
    }

    /**
     * 전체 통계 정보 조회 (대시보드용)
     * 
     * @return 전체 통계 정보
     */
    @Cacheable(value = "reviewLikeStats", key = "'overall'")
    public ReviewLikeStatisticsDto.OverallStats getOverallStatistics() {
        
        log.debug("전체 통계 조회");
        
        long totalActiveLikes = reviewLikeRepository.countByIsLikedTrue();
        long totalCancelledLikes = reviewLikeRepository.countByIsLikedFalse();
        
        ReviewLikeStatisticsDto.ConversionStats conversionStats = getLikeConversionStatistics();
        
        return ReviewLikeStatisticsDto.OverallStats.builder()
                .totalActiveLikes(totalActiveLikes)
                .totalCancelledLikes(totalCancelledLikes)
                .totalLikes(totalActiveLikes + totalCancelledLikes)
                .conversionRate(conversionStats.getConversionRate())
                .build();
    }
}