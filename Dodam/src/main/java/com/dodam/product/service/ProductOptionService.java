package com.dodam.product.service;

import com.dodam.product.dto.request.OptionGroupRequest;
import com.dodam.product.dto.request.ProductOptionRequest;
import com.dodam.product.dto.response.ProductOptionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 상품 옵션 서비스 인터페이스
 * 상품 옵션(색상, 사이즈 등)의 CRUD 작업 및 재고 관리를 담당
 */
public interface ProductOptionService {

    /**
     * 상품 옵션 생성
     * @param request 옵션 생성 요청 데이터
     * @return 생성된 옵션 정보
     */
    ProductOptionResponse createOption(ProductOptionRequest request);

    /**
     * 상품 옵션 수정
     * @param optionId 수정할 옵션 ID
     * @param request 옵션 수정 요청 데이터
     * @return 수정된 옵션 정보
     */
    ProductOptionResponse updateOption(Long optionId, ProductOptionRequest request);

    /**
     * 상품 옵션 삭제
     * @param optionId 삭제할 옵션 ID
     */
    void deleteOption(Long optionId);

    /**
     * 상품 옵션 조회 by ID
     * @param optionId 조회할 옵션 ID
     * @return 옵션 정보
     */
    ProductOptionResponse getOption(Long optionId);

    /**
     * 상품별 옵션 목록 조회
     * @param productId 상품 ID
     * @return 해당 상품의 옵션 목록
     */
    List<ProductOptionResponse> getOptionsByProductId(Long productId);

    /**
     * 옵션 그룹별 옵션 목록 조회
     * @param productId 상품 ID
     * @param optionGroup 옵션 그룹명 (예: "색상", "사이즈")
     * @return 해당 그룹의 옵션 목록
     */
    List<ProductOptionResponse> getOptionsByGroup(Long productId, String optionGroup);

    /**
     * 전체 상품 옵션 페이징 조회
     * @param pageable 페이징 정보
     * @return 페이징된 옵션 목록
     */
    Page<ProductOptionResponse> getAllOptions(Pageable pageable);

    /**
     * 옵션 재고 업데이트
     * @param optionId 옵션 ID
     * @param quantity 변경할 재고량 (음수 가능)
     * @return 업데이트된 옵션 정보
     */
    ProductOptionResponse updateStock(Long optionId, Integer quantity);

    /**
     * 옵션 조합 가격 계산
     * @param productId 상품 ID
     * @param optionIds 선택된 옵션 ID 목록
     * @return 계산된 총 가격 (기본 가격 + 옵션 추가 가격)
     */
    BigDecimal calculateTotalPrice(Long productId, List<Long> optionIds);

    /**
     * 옵션 조합 유효성 검증
     * @param productId 상품 ID
     * @param optionIds 선택된 옵션 ID 목록
     * @return 유효한 조합인지 여부
     */
    boolean validateOptionCombination(Long productId, List<Long> optionIds);

    /**
     * 옵션 그룹 생성
     * @param request 옵션 그룹 생성 요청
     */
    void createOptionGroup(OptionGroupRequest request);

    /**
     * 상품의 옵션 그룹 목록 조회
     * @param productId 상품 ID
     * @return 옵션 그룹 목록
     */
    List<String> getOptionGroups(Long productId);

    /**
     * 재고가 있는 옵션만 조회
     * @param productId 상품 ID
     * @return 재고가 있는 옵션 목록
     */
    List<ProductOptionResponse> getAvailableOptions(Long productId);

    /**
     * 옵션별 재고와 메인 상품 재고 동기화
     * @param productId 상품 ID
     */
    void synchronizeStock(Long productId);

    /**
     * 옵션 재고 차감
     * @param optionId 옵션 ID
     * @param quantity 차감할 수량
     * @return 재고 차감 성공 여부
     */
    boolean deductStock(Long optionId, Integer quantity);

    /**
     * 옵션 재고 복원
     * @param optionId 옵션 ID
     * @param quantity 복원할 수량
     * @return 재고 복원 성공 여부
     */
    boolean restoreStock(Long optionId, Integer quantity);
}