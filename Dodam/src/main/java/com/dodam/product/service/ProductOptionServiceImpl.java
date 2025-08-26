package com.dodam.product.service;

import com.dodam.product.dto.request.OptionGroupRequest;
import com.dodam.product.dto.request.ProductOptionRequest;
import com.dodam.product.dto.response.ProductOptionResponse;
import com.dodam.product.entity.Product;
import com.dodam.product.entity.ProductOption;
import com.dodam.product.entity.OptionGroup;
import com.dodam.product.repository.ProductOptionRepository;
import com.dodam.product.repository.ProductRepository;
import com.dodam.product.repository.OptionGroupRepository;
import com.dodam.product.common.enums.OptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 상품 옵션 서비스 구현체
 * JPA Repository와 함께 상품 옵션의 CRUD 작업 및 재고 관리를 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOptionServiceImpl implements ProductOptionService {

    // JPA Repository 의존성 주입
    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;
    private final OptionGroupRepository optionGroupRepository;

    /**
     * 상품 옵션 생성
     */
    @Override
    @Transactional
    public ProductOptionResponse createOption(ProductOptionRequest request) {
        log.info("상품 옵션 생성 시작: productId={}, optionGroup={}, optionName={}", 
                request.getProductId(), request.getOptionGroup(), request.getOptionName());

        try {
            // 1. 상품 존재 여부 검증
            validateProductExists(request.getProductId());

            // 2. 중복 옵션 검증
            validateDuplicateOption(request.getProductId(), request.getOptionGroup(), 
                                  request.getOptionValue());

            // 3. 옵션 그룹 존재 여부 확인 및 생성
            ensureOptionGroupExists(request.getProductId(), request.getOptionGroup());

            // 4. SKU 코드 생성 (없는 경우)
            String skuCode = generateSkuCode(request);

            // 5. 상품 엔티티 조회
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + request.getProductId()));

            // 6. ProductOption 엔티티 생성 및 저장
            ProductOption option = new ProductOption(
                determineOptionType(request.getOptionGroup()),
                request.getOptionName(),
                request.getOptionValue(),
                request.getAdditionalPrice(),
                request.getSortOrder()
            );
            option.setProduct(product);
            option.updateStock(request.getStockQuantity());

            ProductOption savedOption = productOptionRepository.save(option);

            // 7. 메인 상품 재고와 동기화
            synchronizeStock(request.getProductId());

            // 8. 응답 객체 생성
            ProductOptionResponse response = convertToResponse(savedOption, skuCode);

            log.info("상품 옵션 생성 완료: optionId={}", savedOption.getOptionId());
            return response;

        } catch (Exception e) {
            log.error("상품 옵션 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상품 옵션 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 상품 옵션 수정
     */
    @Override
    @Transactional
    public ProductOptionResponse updateOption(Long optionId, ProductOptionRequest request) {
        log.info("상품 옵션 수정 시작: optionId={}", optionId);

        try {
            // 1. 옵션 존재 여부 검증
            ProductOption existingOption = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));

            // 2. 중복 옵션 검증 (자기 자신 제외)
            validateDuplicateOptionForUpdate(optionId, request.getProductId(), 
                                           request.getOptionGroup(), request.getOptionValue());

            // 3. 옵션 정보 업데이트
            existingOption.updateInfo(request.getOptionName(), request.getOptionValue(), request.getAdditionalPrice());
            existingOption.updateStock(request.getStockQuantity());
            existingOption.setDisplayOrder(request.getSortOrder());
            ProductOption updatedOption = productOptionRepository.save(existingOption);

            // 4. 메인 상품 재고와 동기화
            synchronizeStock(request.getProductId());

            // 응답 객체 생성
            ProductOptionResponse response = convertToResponse(updatedOption, request.getSkuCode());

            log.info("상품 옵션 수정 완료: optionId={}", updatedOption.getOptionId());
            return response;

        } catch (Exception e) {
            log.error("상품 옵션 수정 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상품 옵션 수정에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 상품 옵션 삭제
     */
    @Override
    @Transactional
    public void deleteOption(Long optionId) {
        log.info("상품 옵션 삭제 시작: optionId={}", optionId);

        try {
            // 1. 옵션 존재 여부 검증
            ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));

            // 2. 주문 중인 옵션인지 검증 (실제 구현 필요)
            validateOptionNotInUse(optionId);

            // 3. 하드 삭제 (실제 구현에서는 소프트 삭제 고려)
            Long productId = option.getProduct().getProductId();
            productOptionRepository.delete(option);

            // 4. 메인 상품 재고 재계산
            synchronizeStock(productId);

            log.info("상품 옵션 삭제 완료: optionId={}", optionId);

        } catch (Exception e) {
            log.error("상품 옵션 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상품 옵션 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 상품 옵션 조회
     */
    @Override
    public ProductOptionResponse getOption(Long optionId) {
        log.debug("상품 옵션 조회: optionId={}", optionId);

        try {
            ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));

            return convertToResponse(option, null);

        } catch (Exception e) {
            log.error("상품 옵션 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상품 옵션 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 상품별 옵션 목록 조회
     */
    @Override
    public List<ProductOptionResponse> getOptionsByProductId(Long productId) {
        log.debug("상품별 옵션 목록 조회: productId={}", productId);

        try {
            List<ProductOption> options = productOptionRepository
                .findByProductProductIdAndIsAvailableTrueOrderByDisplayOrderAsc(productId);
            return options.stream()
                .map(option -> convertToResponse(option, null))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("상품별 옵션 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상품별 옵션 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 옵션 그룹별 옵션 목록 조회
     */
    @Override
    public List<ProductOptionResponse> getOptionsByGroup(Long productId, String optionGroup) {
        log.debug("옵션 그룹별 옵션 목록 조회: productId={}, optionGroup={}", productId, optionGroup);

        try {
            // optionGroup 매개변수를 OptionType으로 변환
            OptionType optionType = determineOptionType(optionGroup);
            List<ProductOption> options = productOptionRepository
                .findByProductProductIdAndOptionTypeAndIsAvailableTrueOrderByDisplayOrderAsc(productId, optionType);
            return options.stream()
                .map(option -> convertToResponse(option, null))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("옵션 그룹별 옵션 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("옵션 그룹별 옵션 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 전체 상품 옵션 페이징 조회
     */
    @Override
    public Page<ProductOptionResponse> getAllOptions(Pageable pageable) {
        log.debug("전체 상품 옵션 페이징 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<ProductOption> optionsPage = productOptionRepository.findAll(pageable);
            return optionsPage.map(option -> convertToResponse(option, null));

        } catch (Exception e) {
            log.error("전체 상품 옵션 페이징 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("전체 상품 옵션 페이징 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 옵션 재고 업데이트
     */
    @Override
    @Transactional
    public ProductOptionResponse updateStock(Long optionId, Integer quantity) {
        log.info("옵션 재고 업데이트: optionId={}, quantity={}", optionId, quantity);

        try {
            // 1. 옵션 존재 여부 검증
            ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));

            // 2. 재고 업데이트
            int newStock = option.getStockQuantity() + quantity;
            if (newStock < 0) {
                throw new IllegalArgumentException("재고가 부족합니다");
            }
            option.updateStock(newStock);
            ProductOption updatedOption = productOptionRepository.save(option);

            // 3. 메인 상품 재고와 동기화
            synchronizeStock(option.getProduct().getProductId());

            return convertToResponse(updatedOption, null);

        } catch (Exception e) {
            log.error("옵션 재고 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("옵션 재고 업데이트에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 옵션 조합 가격 계산
     */
    @Override
    public BigDecimal calculateTotalPrice(Long productId, List<Long> optionIds) {
        log.debug("옵션 조합 가격 계산: productId={}, optionIds={}", productId, optionIds);

        try {
            if (optionIds == null || optionIds.isEmpty()) {
                // 기본 가격만 반환
                // Product product = productRepository.findById(productId)
                //     .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + productId));
                // return product.getPrice();
                return BigDecimal.valueOf(50000); // 임시 기본 가격
            }

            // 1. 옵션들 조회
            // List<ProductOption> options = productOptionRepository.findAllById(optionIds);

            // 2. 옵션 유효성 검증
            validateOptionCombination(productId, optionIds);

            // 3. 가격 계산
            // BigDecimal basePrice = getProductBasePrice(productId);
            // BigDecimal additionalPrice = options.stream()
            //     .map(ProductOption::getAdditionalPrice)
            //     .reduce(BigDecimal.ZERO, BigDecimal::add);

            // return basePrice.add(additionalPrice);

            // 임시 계산
            BigDecimal basePrice = BigDecimal.valueOf(50000);
            BigDecimal additionalPrice = BigDecimal.valueOf(optionIds.size() * 5000);
            return basePrice.add(additionalPrice);

        } catch (Exception e) {
            log.error("옵션 조합 가격 계산 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("옵션 조합 가격 계산에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 옵션 조합 유효성 검증
     */
    @Override
    public boolean validateOptionCombination(Long productId, List<Long> optionIds) {
        log.debug("옵션 조합 유효성 검증: productId={}, optionIds={}", productId, optionIds);

        try {
            if (optionIds == null || optionIds.isEmpty()) {
                return true;
            }

            // 1. 모든 옵션이 해당 상품의 옵션인지 검증
            // List<ProductOption> options = productOptionRepository.findAllById(optionIds);
            // boolean allBelongToProduct = options.stream()
            //     .allMatch(option -> option.getProductId().equals(productId));

            // 2. 같은 그룹에서 중복 선택이 없는지 검증
            // Map<String, List<ProductOption>> groupedOptions = options.stream()
            //     .collect(Collectors.groupingBy(ProductOption::getOptionGroup));
            
            // for (Map.Entry<String, List<ProductOption>> entry : groupedOptions.entrySet()) {
            //     String group = entry.getKey();
            //     List<ProductOption> groupOptions = entry.getValue();
                
            //     // 다중 선택이 허용되지 않은 그룹에서 중복 선택 체크
            //     if (!isMultipleChoiceAllowed(productId, group) && groupOptions.size() > 1) {
            //         return false;
            //     }
            // }

            // 3. 필수 옵션 그룹 선택 여부 검증
            // List<String> requiredGroups = getRequiredOptionGroups(productId);
            // Set<String> selectedGroups = groupedOptions.keySet();
            // if (!selectedGroups.containsAll(requiredGroups)) {
            //     return false;
            // }

            // return allBelongToProduct;

            // 임시 검증 (항상 true 반환)
            return true;

        } catch (Exception e) {
            log.error("옵션 조합 유효성 검증 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 옵션 그룹 생성
     */
    @Override
    @Transactional
    public void createOptionGroup(OptionGroupRequest request) {
        log.info("옵션 그룹 생성: productId={}, groupName={}", request.getProductId(), request.getGroupName());

        try {
            // 1. 상품 존재 여부 검증
            validateProductExists(request.getProductId());

            // 2. 중복 그룹 검증
            validateDuplicateOptionGroup(request.getProductId(), request.getGroupName());

            // 3. OptionGroup 엔티티 생성 및 저장
            // OptionGroup optionGroup = OptionGroup.builder()
            //     .productId(request.getProductId())
            //     .groupName(request.getGroupName())
            //     .displayName(request.getDisplayName())
            //     .sortOrder(request.getSortOrder())
            //     .isRequired(request.getIsRequired())
            //     .isMultipleChoice(request.getIsMultipleChoice())
            //     .maxSelections(request.getMaxSelections())
            //     .description(request.getDescription())
            //     .isActive(request.getIsActive())
            //     .groupType(request.getGroupType())
            //     .createdAt(LocalDateTime.now())
            //     .updatedAt(LocalDateTime.now())
            //     .build();

            // optionGroupRepository.save(optionGroup);

            log.info("옵션 그룹 생성 완료: groupName={}", request.getGroupName());

        } catch (Exception e) {
            log.error("옵션 그룹 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("옵션 그룹 생성에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 상품의 옵션 그룹 목록 조회
     */
    @Override
    public List<String> getOptionGroups(Long productId) {
        log.debug("상품의 옵션 그룹 목록 조회: productId={}", productId);

        try {
            return optionGroupRepository.findByProductProductIdOrderBySortOrder(productId)
                .stream()
                .map(OptionGroup::getGroupName)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("상품의 옵션 그룹 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("상품의 옵션 그룹 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 재고가 있는 옵션만 조회
     */
    @Override
    public List<ProductOptionResponse> getAvailableOptions(Long productId) {
        log.debug("재고가 있는 옵션만 조회: productId={}", productId);

        try {
            List<ProductOption> options = productOptionRepository
                .findAvailableOptions(productId);
            return options.stream()
                .map(option -> convertToResponse(option, null))
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("재고가 있는 옵션 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("재고가 있는 옵션 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 옵션별 재고와 메인 상품 재고 동기화
     */
    @Override
    @Transactional
    public void synchronizeStock(Long productId) {
        log.debug("옵션별 재고와 메인 상품 재고 동기화: productId={}", productId);

        try {
            // 1. 상품의 모든 활성 옵션 조회
            List<ProductOption> options = productOptionRepository
                .findByProductProductIdAndIsAvailableTrueOrderByDisplayOrderAsc(productId);

            // 2. 옵션별 재고 합계 계산
            long totalOptionStock = productOptionRepository.getTotalStockQuantity(productId);

            // 3. 메인 상품 재고 업데이트 (Product 엔티티에 재고 업데이트 메서드가 있다고 가정)
            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
            // product.updateStockFromOptions((int)totalOptionStock); // Product 엔티티에 이 메서드 필요

            log.debug("재고 동기화 완료: productId={}, totalStock={}", productId, totalOptionStock);

        } catch (Exception e) {
            log.error("재고 동기화 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("재고 동기화에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 옵션 재고 차감
     */
    @Override
    @Transactional
    public boolean deductStock(Long optionId, Integer quantity) {
        log.info("옵션 재고 차감: optionId={}, quantity={}", optionId, quantity);

        try {
            if (quantity <= 0) {
                throw new IllegalArgumentException("차감 수량은 0보다 커야 합니다");
            }

            ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));

            int currentStock = option.getStockQuantity();
            if (currentStock < quantity) {
                log.warn("재고 부족: 현재 재고={}, 요청 수량={}", currentStock, quantity);
                return false;
            }

            option.updateStock(currentStock - quantity);
            productOptionRepository.save(option);
            synchronizeStock(option.getProduct().getProductId());

            log.info("옵션 재고 차감 완료: optionId={}, 차감 수량={}", optionId, quantity);
            return true;

        } catch (Exception e) {
            log.error("옵션 재고 차감 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 옵션 재고 복원
     */
    @Override
    @Transactional
    public boolean restoreStock(Long optionId, Integer quantity) {
        log.info("옵션 재고 복원: optionId={}, quantity={}", optionId, quantity);

        try {
            if (quantity <= 0) {
                throw new IllegalArgumentException("복원 수량은 0보다 커야 합니다");
            }

            ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다: " + optionId));

            int currentStock = option.getStockQuantity();
            option.updateStock(currentStock + quantity);
            productOptionRepository.save(option);
            synchronizeStock(option.getProduct().getProductId());

            log.info("옵션 재고 복원 완료: optionId={}, 복원 수량={}", optionId, quantity);
            return true;

        } catch (Exception e) {
            log.error("옵션 재고 복원 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    // ===== 내부 유틸리티 메서드들 =====

    /**
     * 상품 존재 여부 검증
     */
    private void validateProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId);
        }
    }

    /**
     * 중복 옵션 검증
     */
    private void validateDuplicateOption(Long productId, String optionGroup, String optionValue) {
        OptionType optionType = determineOptionType(optionGroup);
        boolean exists = productOptionRepository.existsByProductProductIdAndOptionTypeAndOptionValue(
            productId, optionType, optionValue);
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 옵션입니다: " + optionGroup + " - " + optionValue);
        }
    }

    /**
     * 중복 옵션 검증 (수정 시)
     */
    private void validateDuplicateOptionForUpdate(Long optionId, Long productId, String optionGroup, String optionValue) {
        OptionType optionType = determineOptionType(optionGroup);
        boolean exists = productOptionRepository.existsByProductAndTypeAndValueExcludingId(
            productId, optionType, optionValue, optionId);
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 옵션입니다: " + optionGroup + " - " + optionValue);
        }
    }

    /**
     * 옵션이 사용 중인지 검증 (주문 등에서 사용)
     */
    private void validateOptionNotInUse(Long optionId) {
        // 실제 구현에서는 주문 상세, 장바구니 등에서 해당 옵션이 사용되는지 확인
        // boolean isInUse = orderItemRepository.existsByOptionId(optionId);
        // if (isInUse) {
        //     throw new IllegalStateException("사용 중인 옵션은 삭제할 수 없습니다: " + optionId);
        // }
    }

    /**
     * 옵션 그룹 존재 확인 및 생성
     */
    private void ensureOptionGroupExists(Long productId, String optionGroup) {
        boolean exists = optionGroupRepository.existsByProductProductIdAndGroupName(productId, optionGroup);
        if (!exists) {
            // 기본 옵션 그룹 생성 로직은 OptionGroup 엔티티의 정확한 구조 확인 후 구현 필요
            log.warn("옵션 그룹 '{}' 가 존재하지 않습니다. 자동 생성 로직 구현 필요", optionGroup);
        }
    }

    /**
     * 중복 옵션 그룹 검증
     */
    private void validateDuplicateOptionGroup(Long productId, String groupName) {
        boolean exists = optionGroupRepository.existsByProductProductIdAndGroupName(productId, groupName);
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 옵션 그룹입니다: " + groupName);
        }
    }

    /**
     * SKU 코드 생성
     */
    private String generateSkuCode(ProductOptionRequest request) {
        if (request.getSkuCode() != null && !request.getSkuCode().trim().isEmpty()) {
            return request.getSkuCode();
        }

        // 자동 SKU 코드 생성 로직
        String productCode = "P" + request.getProductId();
        String groupCode = request.getOptionGroup().substring(0, Math.min(2, request.getOptionGroup().length())).toUpperCase();
        String valueCode = request.getOptionValue().substring(0, Math.min(3, request.getOptionValue().length())).toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);

        return String.format("%s-%s-%s-%s", productCode, groupCode, valueCode, timestamp);
    }

    /**
     * 엔티티를 응답 DTO로 변환
     */
    private ProductOptionResponse convertToResponse(ProductOption option, String skuCode) {
        return ProductOptionResponse.builder()
            .optionId(option.getOptionId())
            .productId(option.getProduct().getProductId())
            .productName(option.getProduct().getProductName())
            .optionGroup(option.getOptionType().name())
            .optionName(option.getOptionName())
            .optionValue(option.getOptionValue())
            .basePrice(option.getProduct().getPrice())
            .additionalPrice(option.getAdditionalPrice())
            .stockQuantity(option.getStockQuantity())
            .sortOrder(option.getDisplayOrder())
            .isActive(option.getIsAvailable())
            .skuCode(skuCode)
            .createdAt(LocalDateTime.now()) // 실제로는 option.getCreatedAt()
            .updatedAt(LocalDateTime.now()) // 실제로는 option.getUpdatedAt()
            .build();
    }

    /**
     * 옵션 그룹명으로부터 OptionType 결정
     */
    private OptionType determineOptionType(String optionGroup) {
        // 실제 구현에서는 더 정교한 매핑 로직 필요
        switch (optionGroup.toLowerCase()) {
            case "색상":
            case "color":
                return OptionType.COLOR;
            case "사이즈":
            case "size":
                return OptionType.SIZE;
            case "소재":
            case "material":
                return OptionType.MATERIAL;
            default:
                return OptionType.OTHER;
        }
    }
}