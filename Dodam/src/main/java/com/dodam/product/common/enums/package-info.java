/**
 * 도메인 열거형 타입 패키지
 * 
 * <p>상품 도메인에서 사용하는 모든 열거형 타입을 정의합니다.</p>
 * 
 * <p>주요 열거형:</p>
 * <ul>
 *   <li>{@code ProductStatus} - 상품 상태 (DRAFT, ACTIVE, INACTIVE, DELETED)</li>
 *   <li>{@code OptionType} - 옵션 타입 (COLOR, SIZE, MATERIAL 등)</li>
 *   <li>{@code ImageType} - 이미지 타입 (THUMBNAIL, DETAIL, GALLERY)</li>
 * </ul>
 * 
 * <p>설계 원칙:</p>
 * <ul>
 *   <li>타입 안전성 보장</li>
 *   <li>비즈니스 의미 명확화</li>
 *   <li>확장 가능한 구조</li>
 *   <li>국제화 지원</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.common.enums;