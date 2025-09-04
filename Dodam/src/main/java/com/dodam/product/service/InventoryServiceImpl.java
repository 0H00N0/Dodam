package com.dodam.product.service;

import com.dodam.product.dto.request.InventoryRequest;
import com.dodam.product.dto.request.StockMovementRequest;
import com.dodam.product.dto.response.InventoryResponse;
import com.dodam.product.entity.Inventory;
import com.dodam.product.entity.StockMovementHistory;
import com.dodam.product.exception.InsufficientStockException;
import com.dodam.product.exception.InventoryNotFoundException;
import com.dodam.product.repository.InventoryRepository;
import com.dodam.product.repository.StockMovementHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 재고 관리 서비스 구현체
 * 
 * 동시성 제어와 트랜잭션 격리를 통해 안전한 재고 관리를 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final StockMovementHistoryRepository historyRepository;
    private final EntityManager entityManager;
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    @Override
    public Optional<InventoryResponse> getInventory(Long productId) {
        log.debug("재고 조회 요청 - 상품 ID: {}", productId);
        
        return inventoryRepository.findByProductId(productId)
                .map(this::convertToResponse);
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryResponse decreaseStock(InventoryRequest request) {
        log.info("재고 감소 요청 - 상품 ID: {}, 수량: {}", request.getProductId(), request.getQuantity());
        
        return executeWithRetry(() -> {
            Inventory inventory = getInventoryWithPessimisticLock(request.getProductId());
            
            // 재고 충분성 검증
            validateStockAvailability(inventory, request.getQuantity());
            
            // 재고 감소
            inventory.decreaseAvailableStock(request.getQuantity());
            
            // 변경 이력 기록
            recordStockMovement(inventory, request, StockMovementHistory.MovementType.DECREASE);
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            log.info("재고 감소 완료 - 상품 ID: {}, 남은 재고: {}", 
                    request.getProductId(), savedInventory.getAvailableQuantity());
            
            return convertToResponse(savedInventory);
        });
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryResponse increaseStock(InventoryRequest request) {
        log.info("재고 증가 요청 - 상품 ID: {}, 수량: {}", request.getProductId(), request.getQuantity());
        
        return executeWithRetry(() -> {
            Inventory inventory = getInventoryWithPessimisticLock(request.getProductId());
            
            // 재고 증가
            inventory.increaseAvailableStock(request.getQuantity());
            
            // 변경 이력 기록
            recordStockMovement(inventory, request, StockMovementHistory.MovementType.INCREASE);
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            log.info("재고 증가 완료 - 상품 ID: {}, 현재 재고: {}", 
                    request.getProductId(), savedInventory.getAvailableQuantity());
            
            return convertToResponse(savedInventory);
        });
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryResponse reserveStock(InventoryRequest request) {
        log.info("재고 예약 요청 - 상품 ID: {}, 수량: {}", request.getProductId(), request.getQuantity());
        
        return executeWithRetry(() -> {
            Inventory inventory = getInventoryWithPessimisticLock(request.getProductId());
            
            // 재고 충분성 검증
            validateStockAvailability(inventory, request.getQuantity());
            
            // 재고 예약 (사용가능 재고 → 예약 재고로 이동)
            inventory.reserveStock(request.getQuantity());
            
            // 변경 이력 기록
            recordStockMovement(inventory, request, StockMovementHistory.MovementType.RESERVE);
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            log.info("재고 예약 완료 - 상품 ID: {}, 예약 재고: {}", 
                    request.getProductId(), savedInventory.getReservedQuantity());
            
            return convertToResponse(savedInventory);
        });
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryResponse confirmReservation(InventoryRequest request) {
        log.info("예약 확정 요청 - 상품 ID: {}, 수량: {}", request.getProductId(), request.getQuantity());
        
        return executeWithRetry(() -> {
            Inventory inventory = getInventoryWithPessimisticLock(request.getProductId());
            
            // 예약 재고 검증
            validateReservedStock(inventory, request.getQuantity());
            
            // 예약 확정 (예약 재고 감소)
            inventory.confirmReservation(request.getQuantity());
            
            // 변경 이력 기록
            recordStockMovement(inventory, request, StockMovementHistory.MovementType.CONFIRM_RESERVATION);
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            log.info("예약 확정 완료 - 상품 ID: {}, 예약 재고: {}", 
                    request.getProductId(), savedInventory.getReservedQuantity());
            
            return convertToResponse(savedInventory);
        });
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryResponse cancelReservation(InventoryRequest request) {
        log.info("예약 취소 요청 - 상품 ID: {}, 수량: {}", request.getProductId(), request.getQuantity());
        
        return executeWithRetry(() -> {
            Inventory inventory = getInventoryWithPessimisticLock(request.getProductId());
            
            // 예약 재고 검증
            validateReservedStock(inventory, request.getQuantity());
            
            // 예약 취소 (예약 재고 → 사용가능 재고로 복원)
            inventory.cancelReservation(request.getQuantity());
            
            // 변경 이력 기록
            recordStockMovement(inventory, request, StockMovementHistory.MovementType.CANCEL_RESERVATION);
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            log.info("예약 취소 완료 - 상품 ID: {}, 사용가능 재고: {}", 
                    request.getProductId(), savedInventory.getAvailableQuantity());
            
            return convertToResponse(savedInventory);
        });
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public InventoryResponse moveStock(StockMovementRequest request) {
        log.info("재고 이동 요청 - 상품 ID: {}, 출발: {}, 도착: {}, 수량: {}", 
                request.getProductId(), request.getFromWarehouseId(), 
                request.getToWarehouseId(), request.getQuantity());
        
        return executeWithRetry(() -> {
            // 재고 이동은 현재 구조에서는 단순한 재고 조정으로 처리
            Inventory inventory = getInventoryWithPessimisticLock(request.getProductId());
            validateStockAvailability(inventory, request.getQuantity());
            
            // 이동 수량만큼 조정 (현재는 단순 재고 조정으로 처리)
            inventory.decreaseAvailableStock(request.getQuantity());
            inventory.increaseAvailableStock(request.getQuantity());
            
            // 변경 이력 기록
            recordStockMovement(inventory, request, StockMovementHistory.MovementType.MOVE_OUT);
            
            Inventory savedInventory = inventoryRepository.save(inventory);
            
            log.info("재고 이동 완료 - 상품 ID: {}, 재고: {}", 
                    request.getProductId(), savedInventory.getAvailableQuantity());
            
            return convertToResponse(savedInventory);
        });
    }
    
    @Override
    public List<StockMovementHistory> getStockHistory(Long productId, int limit) {
        log.debug("재고 이력 조회 - 상품 ID: {}, 제한: {}", productId, limit);
        
        return historyRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryResponse> getLowStockProducts(int threshold) {
        log.debug("재고 부족 상품 조회 - 기준: {}", threshold);
        
        return inventoryRepository.findLowStockProducts(threshold)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryResponse> getBulkInventory(List<Long> productIds) {
        log.debug("일괄 재고 조회 - 상품 개수: {}", productIds.size());
        
        return inventoryRepository.findByProductIdIn(productIds)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateStockConsistency(Long productId) {
        log.debug("재고 일관성 검증 - 상품 ID: {}", productId);
        
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(productId);
        if (inventoryOpt.isEmpty()) {
            return true; // 재고가 없으면 일관성 문제 없음
        }
        
        Inventory inventory = inventoryOpt.get();
        
        // 총 재고량 = 사용가능 재고 + 예약 재고
        int calculatedTotal = inventory.getAvailableQuantity() + inventory.getReservedQuantity();
        boolean isConsistent = calculatedTotal == inventory.getQuantity();
        
        if (!isConsistent) {
            log.warn("재고 일관성 불일치 발견 - 상품 ID: {}, 계산값: {}, 저장값: {}", 
                    productId, calculatedTotal, inventory.getQuantity());
        }
        
        return isConsistent;
    }
    
    /**
     * 비관적 락을 사용하여 재고 엔티티를 조회합니다.
     */
    private Inventory getInventoryWithPessimisticLock(Long productId) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProductIdWithLock(productId);
        return inventoryOpt.orElseThrow(() -> 
                new InventoryNotFoundException("재고 정보를 찾을 수 없습니다. 상품 ID: " + productId));
    }
    
    /**
     * 비관적 락을 사용하여 재고 엔티티를 조회합니다 (창고 지정).
     */
    private Inventory getInventoryWithPessimisticLock(Long productId, Long warehouseId) {
        // 현재 구조에서는 창고 기능이 없으므로 기본 메서드를 사용
        return getInventoryWithPessimisticLock(productId);
    }
    
    /**
     * 재고 충분성을 검증합니다.
     */
    private void validateStockAvailability(Inventory inventory, Integer requestedQuantity) {
        if (inventory.getAvailableQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                String.format("재고가 부족합니다. 요청: %d, 사용가능: %d", 
                    requestedQuantity, inventory.getAvailableQuantity()));
        }
    }
    
    /**
     * 예약 재고 충분성을 검증합니다.
     */
    private void validateReservedStock(Inventory inventory, Integer requestedQuantity) {
        if (inventory.getReservedQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                String.format("예약 재고가 부족합니다. 요청: %d, 예약됨: %d", 
                    requestedQuantity, inventory.getReservedQuantity()));
        }
    }
    
    /**
     * 재고 변경 이력을 기록합니다.
     */
    private void recordStockMovement(Inventory inventory, InventoryRequest request, 
                                   StockMovementHistory.MovementType movementType) {
        StockMovementHistory history = StockMovementHistory.builder()
                .productId(inventory.getProduct().getProductId())
                .warehouseId(null) // 기본 창고 (창고 기능이 없으므로 null)
                .movementType(movementType)
                .quantity(request.getQuantity())
                .previousAvailableStock(inventory.getAvailableQuantity())
                .previousReservedStock(inventory.getReservedQuantity())
                .reason(request.getReason())
                .referenceNumber(request.getReferenceNumber())
                .requestedBy(request.getRequestedBy())
                .createdAt(LocalDateTime.now())
                .build();
        
        historyRepository.save(history);
    }
    
    /**
     * 재고 이동 이력을 기록합니다.
     */
    private void recordStockMovement(Inventory inventory, StockMovementRequest request, 
                                   StockMovementHistory.MovementType movementType) {
        StockMovementHistory history = StockMovementHistory.builder()
                .productId(inventory.getProduct().getProductId())
                .warehouseId(null) // 기본 창고 (창고 기능이 없으므로 null)
                .movementType(movementType)
                .quantity(request.getQuantity())
                .previousAvailableStock(inventory.getAvailableQuantity())
                .previousReservedStock(inventory.getReservedQuantity())
                .reason(request.getReason())
                .referenceNumber(request.getMovementReferenceNumber())
                .requestedBy(request.getRequestedBy())
                .createdAt(LocalDateTime.now())
                .build();
        
        historyRepository.save(history);
    }
    
    /**
     * 새로운 재고 엔티티를 생성합니다.
     */
    private Inventory createNewInventory(Long productId, Long warehouseId) {
        // Product 엔티티를 조회해서 Inventory를 생성해야 합니다
        // 이 메서드는 실제로는 Product Repository가 필요합니다
        // 지금은 간단하게 처리합니다
        Inventory inventory = new Inventory();
        inventory.setQuantity(0);
        inventory.setAvailableQuantity(0);
        inventory.setReservedQuantity(0);
        inventory.setMinStockLevel(0);
        return inventory;
    }
    
    /**
     * 엔티티를 응답 DTO로 변환합니다.
     */
    private InventoryResponse convertToResponse(Inventory inventory) {
        InventoryResponse.StockStatus status = determineStockStatus(inventory);
        
        return InventoryResponse.builder()
                .productId(inventory.getProduct().getProductId())
                .availableStock(inventory.getAvailableQuantity())
                .reservedStock(inventory.getReservedQuantity())
                .totalStock(inventory.getQuantity())
                .minimumStock(inventory.getMinStockLevel())
                .warehouseId(null) // 창고 기능이 없으므로 null
                .status(status)
                .lastUpdated(inventory.getUpdatedAt())
                .version(inventory.getVersion())
                .build();
    }
    
    /**
     * 재고 상태를 결정합니다.
     */
    private InventoryResponse.StockStatus determineStockStatus(Inventory inventory) {
        if (inventory.getAvailableQuantity() <= 0) {
            return InventoryResponse.StockStatus.OUT_OF_STOCK;
        } else if (inventory.getAvailableQuantity() <= inventory.getMinStockLevel() / 2) {
            return InventoryResponse.StockStatus.CRITICAL;
        } else if (inventory.getAvailableQuantity() <= inventory.getMinStockLevel()) {
            return InventoryResponse.StockStatus.LOW;
        } else {
            return InventoryResponse.StockStatus.NORMAL;
        }
    }
    
    /**
     * 낙관적 락 충돌 시 재시도 로직을 수행합니다.
     */
    private <T> T executeWithRetry(RetryableOperation<T> operation) {
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                return operation.execute();
            } catch (OptimisticLockingFailureException e) {
                attempts++;
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    log.error("낙관적 락 재시도 실패 - 최대 시도 횟수 초과: {}", attempts);
                    throw e;
                }
                
                log.warn("낙관적 락 충돌 발생 - 재시도 {}/{}", attempts, MAX_RETRY_ATTEMPTS);
                
                try {
                    Thread.sleep(100 * attempts); // 백오프 전략
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("재시도 중 인터럽트 발생", ie);
                }
            }
        }
        throw new RuntimeException("예상치 못한 재시도 로직 오류");
    }
    
    @FunctionalInterface
    private interface RetryableOperation<T> {
        T execute() throws OptimisticLockingFailureException;
    }
}