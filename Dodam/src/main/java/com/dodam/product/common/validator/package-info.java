/**
 * 커스텀 검증 컴포넌트 패키지
 * 
 * <p>도메인 특화 검증 로직을 위한 커스텀 어노테이션과 구현체를 포함합니다.</p>
 * 
 * <p>주요 검증 어노테이션:</p>
 * <ul>
 *   <li>{@code @ValidSku} - SKU 형식 검증</li>
 *   <li>{@code @ValidPrice} - 가격 범위 검증</li>
 *   <li>{@code @ValidProductStatus} - 상품 상태 전이 검증</li>
 * </ul>
 * 
 * <p>검증 특징:</p>
 * <ul>
 *   <li>Bean Validation 표준 준수</li>
 *   <li>비즈니스 규칙 캡슐화</li>
 *   <li>재사용 가능한 구조</li>
 *   <li>명확한 에러 메시지</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.common.validator;