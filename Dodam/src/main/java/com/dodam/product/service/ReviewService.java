package com.dodam.product.service;

import com.dodam.product.dto.request.ReviewRequestDto;
import com.dodam.product.dto.response.ReviewResponseDto;
import com.dodam.product.dto.statistics.ReviewStatisticsDto;
import com.dodam.product.entity.Product;
import com.dodam.product.entity.Review;
import com.dodam.product.entity.Review.ReviewStatus;
import com.dodam.product.exception.*;
import com.dodam.product.repository.ProductRepository;
import com.dodam.product.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 리뷰 서비스 클래스
 * 리뷰 관련 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    // 리뷰 수정 가능 시간 (24시간)
    private static final int EDITABLE_HOURS = 24;

    // ========================== 생성 메소드 ==========================

    /**
     * 새로운 리뷰를 생성합니다.
     * @param requestDto 리뷰 생성 요청 DTO
     * @param memberId 작성자 회원 ID
     * @return 생성된 리뷰 응답 DTO
     */
    @Transactional
    @CacheEvict(value = {"reviews", "products"}, allEntries = true)
    public ReviewResponseDto createReview(ReviewRequestDto requestDto, Long memberId) {
        log.info("리뷰 생성 시작: memberId={}, productId={}", memberId, requestDto.getProductId());
        
        // 유효성 검증
        validateReviewRequest(requestDto);
        
        // 상품 조회
        Product product = productRepository.findById(requestDto.getProductId())
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("상품", requestDto.getProductId()));
        
        // 중복 리뷰 검사
        if (reviewRepository.existsByMemberIdAndProductProductIdAndDeletedAtIsNull(memberId, requestDto.getProductId())) {
            throw new DuplicateResourceException("이미 해당 상품에 대한 리뷰를 작성하셨습니다.");
        }
        
        // Entity 생성 및 저장
        Review review = Review.builder()
                .memberId(memberId)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .rating(requestDto.getRating())
                .status(ReviewStatus.ACTIVE)
                .product(product)
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        log.info("리뷰 생성 완료: ID={}, 회원={}, 상품={}", 
                 savedReview.getReviewId(), memberId, requestDto.getProductId());
        
        return convertToResponseDto(savedReview);
    }

    // ========================== 조회 메소드 ==========================

    /**
     * ID로 리뷰를 조회합니다.
     * @param reviewId 리뷰 ID
     * @return 리뷰 응답 DTO
     */
    @Cacheable(value = "reviews", key = "#reviewId")
    public ReviewResponseDto getReviewById(Long reviewId) {
        log.debug("리뷰 조회: ID={}", reviewId);
        
        Review review = reviewRepository.findByIdWithProduct(reviewId)
                .filter(r -> !r.isDeleted() && r.isDisplayable())
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));
        
        return convertToResponseDto(review);
    }

    /**
     * 표시 가능한 리뷰를 페이징 조회합니다.
     * @param pageable 페이징 정보
     * @return 리뷰 응답 DTO 페이지
     */
    public Page<ReviewResponseDto> getDisplayableReviews(Pageable pageable) {
        log.debug("표시 가능한 리뷰 조회");
        
        return reviewRepository.findDisplayableReviews(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 상품별 리뷰를 조회합니다.
     * @param productId 상품 ID
     * @param pageable 페이징 정보
     * @return 상품별 리뷰 응답 DTO 페이지
     */
    public Page<ReviewResponseDto> getReviewsByProduct(Long productId, Pageable pageable) {
        log.debug("상품별 리뷰 조회: productId={}", productId);
        
        // 상품 존재 확인
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("상품", productId);
        }
        
        return reviewRepository.findDisplayableReviewsByProduct(productId, pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 회원별 리뷰를 조회합니다.
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 회원별 리뷰 응답 DTO 페이지
     */
    public Page<ReviewResponseDto> getReviewsByMember(Long memberId, Pageable pageable) {
        log.debug("회원별 리뷰 조회: memberId={}", memberId);
        
        return reviewRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 평점별 리뷰를 조회합니다.
     * @param rating 평점
     * @param pageable 페이징 정보
     * @return 평점별 리뷰 응답 DTO 페이지
     */
    public Page<ReviewResponseDto> getReviewsByRating(Integer rating, Pageable pageable) {
        log.debug("평점별 리뷰 조회: rating={}", rating);
        
        validateRating(rating);
        
        return reviewRepository.findByRatingAndDeletedAtIsNull(rating, pageable)
                .map(this::convertToResponseDto);
    }

    // ========================== 검색 메소드 ==========================

    /**
     * 키워드로 리뷰를 검색합니다.
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 리뷰 응답 DTO 페이지
     */
    public Page<ReviewResponseDto> searchReviews(String keyword, Pageable pageable) {
        log.debug("리뷰 검색: keyword={}", keyword);
        
        if (!StringUtils.hasText(keyword)) {
            return getDisplayableReviews(pageable);
        }
        
        return reviewRepository.searchReviews(keyword.trim(), pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 평점 범위로 리뷰를 검색합니다.
     * @param minRating 최소 평점
     * @param maxRating 최대 평점
     * @param pageable 페이징 정보
     * @return 평점 범위 내 리뷰 응답 DTO 페이지
     */
    public Page<ReviewResponseDto> getReviewsByRatingRange(Integer minRating, Integer maxRating, Pageable pageable) {
        log.debug("평점 범위별 리뷰 조회: {}~{}", minRating, maxRating);
        
        validateRatingRange(minRating, maxRating);
        
        return reviewRepository.findByRatingBetweenAndDeletedAtIsNull(minRating, maxRating, pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 최신 리뷰를 조회합니다.
     * @param pageable 페이징 정보
     * @return 최신 리뷰 응답 DTO 목록
     */
    public Slice<ReviewResponseDto> getLatestReviews(Pageable pageable) {
        log.debug("최신 리뷰 조회");
        
        return reviewRepository.findLatestReviews(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 인기 리뷰를 조회합니다 (좋아요 수 기준).
     * @param pageable 페이징 정보
     * @return 인기 리뷰 응답 DTO 목록
     */
    public Slice<ReviewResponseDto> getPopularReviews(Pageable pageable) {
        log.debug("인기 리뷰 조회");
        
        return reviewRepository.findPopularReviews(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 도움이 된 리뷰를 조회합니다.
     * @param minLikes 최소 좋아요 수
     * @param pageable 페이징 정보
     * @return 도움이 된 리뷰 응답 DTO 목록
     */
    public Slice<ReviewResponseDto> getHelpfulReviews(Long minLikes, Pageable pageable) {
        log.debug("도움이 된 리뷰 조회: minLikes={}", minLikes);
        
        return reviewRepository.findHelpfulReviews(minLikes, pageable)
                .map(this::convertToResponseDto);
    }

    // ========================== 수정 메소드 ==========================

    /**
     * 리뷰를 수정합니다.
     * @param reviewId 리뷰 ID
     * @param requestDto 리뷰 수정 요청 DTO
     * @param memberId 요청자 회원 ID
     * @return 수정된 리뷰 응답 DTO
     */
    @Transactional
    @CacheEvict(value = {"reviews", "products"}, allEntries = true)
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto requestDto, Long memberId) {
        log.info("리뷰 수정 시작: reviewId={}, memberId={}", reviewId, memberId);
        
        // 유효성 검증
        validateReviewRequest(requestDto);
        
        // 기존 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));
        
        // 권한 검증 (작성자만 수정 가능)
        if (!review.getMemberId().equals(memberId)) {
            throw new UnauthorizedException(memberId, "리뷰 수정");
        }
        
        // 수정 가능 시간 검증
        if (!review.isEditable(EDITABLE_HOURS)) {
            throw new BusinessException("리뷰 작성 후 " + EDITABLE_HOURS + "시간 이내에만 수정할 수 있습니다.");
        }
        
        // 정보 업데이트
        review.setTitle(requestDto.getTitle());
        review.setContent(requestDto.getContent());
        review.setRating(requestDto.getRating());
        
        Review updatedReview = reviewRepository.save(review);
        
        log.info("리뷰 수정 완료: reviewId={}", reviewId);
        return convertToResponseDto(updatedReview);
    }

    // ========================== 상태 관리 메소드 ==========================

    /**
     * 리뷰를 신고 처리합니다.
     * @param reviewId 리뷰 ID
     * @param reporterId 신고자 회원 ID
     */
    @Transactional
    @CacheEvict(value = "reviews", key = "#reviewId")
    public void reportReview(Long reviewId, Long reporterId) {
        log.info("리뷰 신고: reviewId={}, reporterId={}", reviewId, reporterId);
        
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));
        
        // 본인 리뷰는 신고 불가
        if (review.getMemberId().equals(reporterId)) {
            throw new BusinessException("본인이 작성한 리뷰는 신고할 수 없습니다.");
        }
        
        // 신고 처리
        review.report();
        reviewRepository.save(review);
        
        log.info("리뷰 신고 처리 완료: reviewId={}", reviewId);
    }

    /**
     * 리뷰를 숨김 처리합니다 (관리자용).
     * @param reviewId 리뷰 ID
     */
    @Transactional
    @CacheEvict(value = "reviews", key = "#reviewId")
    public void hideReview(Long reviewId) {
        log.info("리뷰 숨김 처리: reviewId={}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));
        
        review.hide();
        reviewRepository.save(review);
        
        log.info("리뷰 숨김 처리 완료: reviewId={}", reviewId);
    }

    /**
     * 리뷰를 활성화합니다 (관리자용).
     * @param reviewId 리뷰 ID
     */
    @Transactional
    @CacheEvict(value = "reviews", key = "#reviewId")
    public void activateReview(Long reviewId) {
        log.info("리뷰 활성화: reviewId={}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));
        
        review.activate();
        reviewRepository.save(review);
        
        log.info("리뷰 활성화 완료: reviewId={}", reviewId);
    }

    /**
     * 신고된 리뷰를 일괄 숨김 처리합니다.
     * @return 숨김 처리된 리뷰 수
     */
    @Transactional
    @CacheEvict(value = "reviews", allEntries = true)
    public int hideReportedReviews() {
        log.info("신고된 리뷰 일괄 숨김 처리");
        
        int hiddenCount = reviewRepository.hideReportedReviews();
        
        log.info("신고된 리뷰 일괄 숨김 처리 완료: {}개", hiddenCount);
        return hiddenCount;
    }

    // ========================== 삭제 메소드 ==========================

    /**
     * 리뷰를 소프트 삭제합니다.
     * @param reviewId 리뷰 ID
     * @param memberId 요청자 회원 ID
     */
    @Transactional
    @CacheEvict(value = {"reviews", "products"}, allEntries = true)
    public void deleteReview(Long reviewId, Long memberId) {
        log.info("리뷰 삭제 시작: reviewId={}, memberId={}", reviewId, memberId);
        
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));
        
        // 권한 검증 (작성자만 삭제 가능)
        if (!review.getMemberId().equals(memberId)) {
            throw new UnauthorizedException(memberId, "리뷰 삭제");
        }
        
        // 소프트 삭제 수행
        review.delete();
        reviewRepository.save(review);
        
        log.info("리뷰 삭제 완료: reviewId={}", reviewId);
    }

    // ========================== 통계 메소드 ==========================

    /**
     * 리뷰 통계 정보를 조회합니다.
     * @return 리뷰 통계 DTO
     */
    public ReviewStatisticsDto getReviewStatistics() {
        log.debug("리뷰 통계 조회");
        
        long totalReviews = reviewRepository.countActiveReviews();
        long activeReviews = reviewRepository.countByStatusAndDeletedAtIsNull(ReviewStatus.ACTIVE);
        long hiddenReviews = reviewRepository.countByStatusAndDeletedAtIsNull(ReviewStatus.HIDDEN);
        long reportedReviews = reviewRepository.countByStatusAndDeletedAtIsNull(ReviewStatus.REPORTED);
        
        Object[] overallStats = reviewRepository.getOverallRatingStats();
        List<Object[]> ratingStats = reviewRepository.getRatingStats();
        
        return ReviewStatisticsDto.builder()
                .totalReviews(totalReviews)
                .activeReviews(activeReviews)
                .hiddenReviews(hiddenReviews)
                .reportedReviews(reportedReviews)
                .averageRating(overallStats[0] != null ? ((Number) overallStats[0]).doubleValue() : 0.0)
                .highestRating(overallStats[1] != null ? ((Number) overallStats[1]).intValue() : 0)
                .lowestRating(overallStats[2] != null ? ((Number) overallStats[2]).intValue() : 0)
                .build();
    }

    /**
     * 상품별 평점 통계를 조회합니다.
     * @return 상품별 평점 통계 목록
     */
    public List<ReviewStatisticsDto.ProductRatingStats> getProductRatingStatistics() {
        log.debug("상품별 평점 통계 조회");
        
        List<Object[]> stats = reviewRepository.getProductRatingStats();
        
        return stats.stream()
                .map(stat -> ReviewStatisticsDto.ProductRatingStats.builder()
                        .productId(((Number) stat[0]).longValue())
                        .productName((String) stat[1])
                        .averageRating(stat[2] != null ? ((Number) stat[2]).doubleValue() : 0.0)
                        .reviewCount(((Number) stat[3]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 기간 내 작성된 리뷰 수를 조회합니다.
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 작성된 리뷰 수
     */
    public long countReviewsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("기간별 리뷰 작성 수 조회: {} ~ {}", startDate, endDate);
        
        validateDateRange(startDate, endDate);
        return reviewRepository.countReviewsCreatedBetween(startDate, endDate);
    }

    // ========================== 유효성 검증 메소드 ==========================

    /**
     * 리뷰 요청 DTO의 유효성을 검증합니다.
     * @param requestDto 검증할 요청 DTO
     */
    private void validateReviewRequest(ReviewRequestDto requestDto) {
        if (requestDto == null) {
            throw new ValidationException("리뷰 요청 데이터가 null입니다.");
        }
        
        if (!StringUtils.hasText(requestDto.getTitle())) {
            throw new ValidationException("제목", "제목은 필수입니다.");
        }
        
        if (requestDto.getTitle().length() > 200) {
            throw new ValidationException("제목", "제목은 200자를 초과할 수 없습니다.");
        }
        
        if (!StringUtils.hasText(requestDto.getContent())) {
            throw new ValidationException("내용", "내용은 필수입니다.");
        }
        
        if (requestDto.getContent().length() > 2000) {
            throw new ValidationException("내용", "내용은 2000자를 초과할 수 없습니다.");
        }
        
        validateRating(requestDto.getRating());
        
        if (requestDto.getProductId() == null) {
            throw new ValidationException("상품", "상품은 필수입니다.");
        }
    }

    /**
     * 평점의 유효성을 검증합니다.
     * @param rating 검증할 평점
     */
    private void validateRating(Integer rating) {
        if (rating == null) {
            throw new ValidationException("평점", "평점은 필수입니다.");
        }
        
        if (rating < 1 || rating > 5) {
            throw new ValidationException("평점", "평점은 1~5점 사이여야 합니다.");
        }
    }

    /**
     * 평점 범위의 유효성을 검증합니다.
     * @param minRating 최소 평점
     * @param maxRating 최대 평점
     */
    private void validateRatingRange(Integer minRating, Integer maxRating) {
        if (minRating == null || maxRating == null) {
            throw new ValidationException("최소 평점과 최대 평점은 필수입니다.");
        }
        
        validateRating(minRating);
        validateRating(maxRating);
        
        if (minRating > maxRating) {
            throw new ValidationException("최소 평점이 최대 평점보다 클 수 없습니다.");
        }
    }

    /**
     * 날짜 범위의 유효성을 검증합니다.
     * @param startDate 시작일
     * @param endDate 종료일
     */
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("시작일과 종료일은 필수입니다.");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("시작일이 종료일보다 늦을 수 없습니다.");
        }
    }

    // ========================== DTO 변환 메소드 ==========================

    /**
     * Review Entity를 ReviewResponseDto로 변환합니다.
     * @param review 변환할 Review Entity
     * @return ReviewResponseDto
     */
    private ReviewResponseDto convertToResponseDto(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .memberId(review.getMemberId())
                .title(review.getTitle())
                .content(review.getContent())
                .rating(review.getRating())
                .status(review.getStatus())
                .productId(review.getProduct().getProductId())
                .productName(review.getProduct().getProductName())
                .likeCount(review.getLikeCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    // ========================== 기타 유틸리티 메소드 ==========================

    /**
     * 리뷰 존재 여부를 확인합니다.
     * @param reviewId 리뷰 ID
     * @return 존재 여부
     */
    public boolean existsById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .map(review -> !review.isDeleted())
                .orElse(false);
    }

    /**
     * 회원이 특정 상품에 리뷰를 작성했는지 확인합니다.
     * @param memberId 회원 ID
     * @param productId 상품 ID
     * @return 리뷰 작성 여부
     */
    public boolean hasReviewForProduct(Long memberId, Long productId) {
        return reviewRepository.existsByMemberIdAndProductProductIdAndDeletedAtIsNull(memberId, productId);
    }
}