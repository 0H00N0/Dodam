/**
 * 데이터 액세스 레이어 패키지
 * 
 * <p>Spring Data JPA를 활용한 데이터베이스 액세스를 담당합니다.</p>
 * 
 * <p>Repository 인터페이스:</p>
 * <ul>
 *   <li>{@code ProductRepository} - 상품 데이터 액세스</li>
 *   <li>{@code CategoryRepository} - 카테고리 데이터 액세스</li>
 *   <li>{@code BrandRepository} - 브랜드 데이터 액세스</li>
 *   <li>{@code ProductOptionRepository} - 상품 옵션 데이터 액세스</li>
 *   <li>{@code InventoryRepository} - 재고 데이터 액세스</li>
 * </ul>
 * 
 * <p>구현 특징:</p>
 * <ul>
 *   <li>JpaRepository 상속</li>
 *   <li>커스텀 쿼리 메서드</li>
 *   <li>N+1 문제 해결을 위한 @EntityGraph 활용</li>
 *   <li>동시성 제어를 위한 락 처리</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.repository;