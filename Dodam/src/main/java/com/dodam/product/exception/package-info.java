/**
 * 도메인 특화 예외 패키지
 * 
 * <p>상품 도메인에서 발생하는 비즈니스 예외를 정의합니다.</p>
 * 
 * <p>주요 예외 클래스:</p>
 * <ul>
 *   <li>{@code ProductNotFoundException} - 상품 찾을 수 없음</li>
 *   <li>{@code InsufficientStockException} - 재고 부족</li>
 *   <li>{@code DuplicateSkuException} - SKU 중복</li>
 * </ul>
 * 
 * <p>예외 처리 전략:</p>
 * <ul>
 *   <li>명확한 에러 메시지</li>
 *   <li>에러 코드 체계</li>
 *   <li>국제화 지원</li>
 *   <li>로깅 및 모니터링 연동</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.exception;