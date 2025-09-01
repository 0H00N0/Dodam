package com.dodam.product.service;

import com.dodam.product.dto.request.CategoryRequestDto;
import com.dodam.product.dto.response.CategoryResponseDto;
import com.dodam.product.dto.statistics.ProductStatisticsDto;
import com.dodam.product.entity.Category;
import com.dodam.product.exception.ResourceNotFoundException;
import com.dodam.product.exception.DuplicateResourceException;
import com.dodam.product.exception.BusinessException;
import com.dodam.product.exception.ValidationException;
import com.dodam.product.repository.CategoryRepository;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 서비스 클래스
 * 카테고리 관련 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // ========================== 생성 메소드 ==========================

    /**
     * 새로운 카테고리를 생성합니다.
     * @param requestDto 카테고리 생성 요청 DTO
     * @return 생성된 카테고리 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        log.info("카테고리 생성 시작: {}", requestDto.getCategoryName());
        
        // 유효성 검증
        validateCategoryRequest(requestDto);
        
        // 카테고리명 중복 검사
        if (categoryRepository.existsByCategoryNameIgnoreCase(requestDto.getCategoryName(), null)) {
            throw new DuplicateResourceException("카테고리", requestDto.getCategoryName());
        }
        
        // Entity 생성 및 저장
        Category category = Category.builder()
                .categoryName(requestDto.getCategoryName())
                .description(requestDto.getDescription())
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        
        log.info("카테고리 생성 완료: ID={}, 이름={}", savedCategory.getCategoryId(), savedCategory.getCategoryName());
        return convertToResponseDto(savedCategory);
    }

    // ========================== 조회 메소드 ==========================

    /**
     * ID로 카테고리를 조회합니다.
     * @param categoryId 카테고리 ID
     * @return 카테고리 응답 DTO
     */
    @Cacheable(value = "categories", key = "#categoryId")
    public CategoryResponseDto getCategoryById(Long categoryId) {
        log.debug("카테고리 조회: ID={}", categoryId);
        
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> ResourceNotFoundException.forResourceId("카테고리", categoryId));
        
        return convertToResponseDto(category);
    }

    /**
     * 카테고리명으로 카테고리를 조회합니다.
     * @param categoryName 카테고리명
     * @return 카테고리 응답 DTO
     */
    public CategoryResponseDto getCategoryByName(String categoryName) {
        log.debug("카테고리 이름으로 조회: {}", categoryName);
        
        Category category = categoryRepository.findByCategoryName(categoryName)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> ResourceNotFoundException.forIdentifier("카테고리", categoryName));
        
        return convertToResponseDto(category);
    }

    /**
     * 모든 활성 카테고리를 조회합니다.
     * @param sort 정렬 조건
     * @return 카테고리 응답 DTO 목록
     */
    @Cacheable(value = "categories", key = "'all:' + #sort.toString()")
    public List<CategoryResponseDto> getAllCategories(Sort sort) {
        log.debug("모든 카테고리 조회");
        
        return categoryRepository.findByDeletedAtIsNull(sort)
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 활성 카테고리를 페이징 조회합니다.
     * @param pageable 페이징 정보
     * @return 카테고리 응답 DTO 페이지
     */
    public Page<CategoryResponseDto> getCategories(Pageable pageable) {
        log.debug("카테고리 페이징 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        return categoryRepository.findByDeletedAtIsNull(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 상품을 가진 카테고리만 조회합니다.
     * @param pageable 페이징 정보
     * @return 상품이 있는 카테고리 응답 DTO 페이지
     */
    public Page<CategoryResponseDto> getCategoriesWithProducts(Pageable pageable) {
        log.debug("상품이 있는 카테고리 조회");
        
        return categoryRepository.findCategoriesWithProducts(pageable)
                .map(this::convertToResponseDto);
    }

    // ========================== 검색 메소드 ==========================

    /**
     * 키워드로 카테고리를 검색합니다.
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 검색된 카테고리 응답 DTO 페이지
     */
    public Page<CategoryResponseDto> searchCategories(String keyword, Pageable pageable) {
        log.debug("카테고리 검색: keyword={}", keyword);
        
        if (!StringUtils.hasText(keyword)) {
            return getCategories(pageable);
        }
        
        return categoryRepository.searchActiveCategories(keyword.trim(), pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 카테고리명으로 검색합니다.
     * @param categoryName 카테고리명 키워드
     * @param pageable 페이징 정보
     * @return 검색된 카테고리 응답 DTO 페이지
     */
    public Page<CategoryResponseDto> searchByName(String categoryName, Pageable pageable) {
        log.debug("카테고리명으로 검색: {}", categoryName);
        
        if (!StringUtils.hasText(categoryName)) {
            return getCategories(pageable);
        }
        
        return categoryRepository.findByCategoryNameContainingAndDeletedAtIsNull(categoryName.trim(), pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 최근 생성된 카테고리를 조회합니다.
     * @param pageable 페이징 정보 (size로 개수 제한)
     * @return 최근 카테고리 응답 DTO 목록
     */
    public Slice<CategoryResponseDto> getRecentCategories(Pageable pageable) {
        log.debug("최근 카테고리 조회: size={}", pageable.getPageSize());
        
        return categoryRepository.findRecentCategories(pageable)
                .map(this::convertToResponseDto);
    }

    /**
     * 인기 카테고리를 조회합니다 (상품 수 기준).
     * @param pageable 페이징 정보
     * @return 인기 카테고리 응답 DTO 목록
     */
    public Slice<CategoryResponseDto> getPopularCategories(Pageable pageable) {
        log.debug("인기 카테고리 조회");
        
        return categoryRepository.findPopularCategories(pageable)
                .map(this::convertToResponseDto);
    }

    // ========================== 수정 메소드 ==========================

    /**
     * 카테고리 정보를 수정합니다.
     * @param categoryId 카테고리 ID
     * @param requestDto 카테고리 수정 요청 DTO
     * @return 수정된 카테고리 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDto updateCategory(Long categoryId, CategoryRequestDto requestDto) {
        log.info("카테고리 수정 시작: ID={}", categoryId);
        
        // 유효성 검증
        validateCategoryRequest(requestDto);
        
        // 기존 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> ResourceNotFoundException.forResourceId("카테고리", categoryId));
        
        // 카테고리명 중복 검사 (본인 제외)
        if (categoryRepository.existsByCategoryNameIgnoreCase(requestDto.getCategoryName(), categoryId)) {
            throw new DuplicateResourceException("카테고리", requestDto.getCategoryName());
        }
        
        // 정보 업데이트
        category.setCategoryName(requestDto.getCategoryName());
        category.setDescription(requestDto.getDescription());
        
        Category updatedCategory = categoryRepository.save(category);
        
        log.info("카테고리 수정 완료: ID={}, 이름={}", updatedCategory.getCategoryId(), updatedCategory.getCategoryName());
        return convertToResponseDto(updatedCategory);
    }

    // ========================== 삭제 메소드 ==========================

    /**
     * 카테고리를 소프트 삭제합니다.
     * @param categoryId 카테고리 ID
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long categoryId) {
        log.info("카테고리 삭제 시작: ID={}", categoryId);
        
        // 기존 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> ResourceNotFoundException.forResourceId("카테고리", categoryId));
        
        // 상품이 있는 카테고리는 삭제 불가
        long productCount = categoryRepository.countCategoriesWithProducts();
        if (productCount > 0) {
            throw new BusinessException("상품이 등록된 카테고리는 삭제할 수 없습니다.");
        }
        
        // 소프트 삭제 수행
        category.delete();
        categoryRepository.save(category);
        
        log.info("카테고리 삭제 완료: ID={}", categoryId);
    }

    /**
     * 카테고리를 복구합니다.
     * @param categoryId 카테고리 ID
     * @return 복구된 카테고리 응답 DTO
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDto restoreCategory(Long categoryId) {
        log.info("카테고리 복구 시작: ID={}", categoryId);
        
        // 삭제된 카테고리 조회
        Category category = categoryRepository.findById(categoryId)
                .filter(Category::isDeleted)
                .orElseThrow(() -> ResourceNotFoundException.forResourceId("삭제된 카테고리", categoryId));
        
        // 카테고리명 중복 검사 (복구 시에도 중복 불가)
        if (categoryRepository.existsByCategoryNameIgnoreCase(category.getCategoryName(), null)) {
            throw new DuplicateResourceException("카테고리", category.getCategoryName());
        }
        
        // 복구 수행
        category.restore();
        Category restoredCategory = categoryRepository.save(category);
        
        log.info("카테고리 복구 완료: ID={}", categoryId);
        return convertToResponseDto(restoredCategory);
    }

    /**
     * 빈 카테고리들을 일괄 삭제합니다.
     * @return 삭제된 카테고리 수
     */
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public int deleteEmptyCategories() {
        log.info("빈 카테고리 일괄 삭제 시작");
        
        int deletedCount = categoryRepository.softDeleteEmptyCategories(LocalDateTime.now());
        
        log.info("빈 카테고리 일괄 삭제 완료: {}개", deletedCount);
        return deletedCount;
    }

    // ========================== 통계 메소드 ==========================

    /**
     * 카테고리 통계 정보를 조회합니다.
     * @return 카테고리 통계 DTO
     */
    public ProductStatisticsDto.CategoryStats getCategoryStatistics() {
        log.debug("카테고리 통계 조회");
        
        long totalCategories = categoryRepository.countActiveCategories();
        long categoriesWithProducts = categoryRepository.countCategoriesWithProducts();
        long emptyCategories = categoryRepository.countEmptyCategories();
        
        return ProductStatisticsDto.CategoryStats.builder()
                .totalCategories(totalCategories)
                .categoriesWithProducts(categoriesWithProducts)
                .emptyCategories(emptyCategories)
                .build();
    }

    /**
     * 카테고리별 상품 수 통계를 조회합니다.
     * @return 카테고리별 상품 수 목록 [카테고리명, 상품수]
     */
    public List<ProductStatisticsDto.CategoryProductCount> getCategoryProductStats() {
        log.debug("카테고리별 상품 통계 조회");
        
        List<Object[]> stats = categoryRepository.getCategoryProductCountStats();
        
        return stats.stream()
                .map(stat -> ProductStatisticsDto.CategoryProductCount.builder()
                        .categoryName((String) stat[0])
                        .productCount(((Number) stat[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 기간 내 생성된 카테고리 수를 조회합니다.
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 생성된 카테고리 수
     */
    public long countCategoriesCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("기간별 카테고리 생성 수 조회: {} ~ {}", startDate, endDate);
        
        validateDateRange(startDate, endDate);
        return categoryRepository.countCategoriesCreatedBetween(startDate, endDate);
    }

    // ========================== 유효성 검증 메소드 ==========================

    /**
     * 카테고리 요청 DTO의 유효성을 검증합니다.
     * @param requestDto 검증할 요청 DTO
     */
    private void validateCategoryRequest(CategoryRequestDto requestDto) {
        if (requestDto == null) {
            throw new ValidationException("카테고리 요청 데이터가 null입니다.");
        }
        
        if (!StringUtils.hasText(requestDto.getCategoryName())) {
            throw new ValidationException("카테고리명", "카테고리명은 필수입니다.");
        }
        
        if (requestDto.getCategoryName().trim().length() > 100) {
            throw new ValidationException("카테고리명", "카테고리명은 100자를 초과할 수 없습니다.");
        }
        
        if (requestDto.getDescription() != null && requestDto.getDescription().length() > 500) {
            throw new ValidationException("설명", "설명은 500자를 초과할 수 없습니다.");
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
     * Category Entity를 CategoryResponseDto로 변환합니다.
     * @param category 변환할 Category Entity
     * @return CategoryResponseDto
     */
    private CategoryResponseDto convertToResponseDto(Category category) {
        return CategoryResponseDto.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    // ========================== 기타 유틸리티 메소드 ==========================

    /**
     * 카테고리 존재 여부를 확인합니다.
     * @param categoryId 카테고리 ID
     * @return 존재 여부
     */
    public boolean existsById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(category -> !category.isDeleted())
                .orElse(false);
    }

    /**
     * 카테고리명 존재 여부를 확인합니다.
     * @param categoryName 카테고리명
     * @return 존재 여부
     */
    public boolean existsByName(String categoryName) {
        return categoryRepository.existsByCategoryNameIgnoreCase(categoryName, null);
    }
}