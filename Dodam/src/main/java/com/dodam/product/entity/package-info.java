/**
 * JPA 엔티티 패키지
 * 
 * <p>상품 도메인의 핵심 엔티티들을 정의합니다.</p>
 * 
 * <p>주요 엔티티:</p>
 * <ul>
 *   <li>{@code Product} - 상품 (핵심 집계 루트)</li>
 *   <li>{@code Category} - 카테고리</li>
 *   <li>{@code Brand} - 브랜드</li>
 *   <li>{@code ProductOption} - 상품 옵션</li>
 *   <li>{@code ProductDetail} - 상품 상세 정보</li>
 *   <li>{@code ProductImage} - 상품 이미지</li>
 *   <li>{@code Inventory} - 재고 정보</li>
 * </ul>
 * 
 * <p>설계 원칙:</p>
 * <ul>
 *   <li>Domain-Driven Design 적용</li>
 *   <li>연관관계 매핑 최적화</li>
 *   <li>지연 로딩 기본 활용</li>
 *   <li>비즈니스 메서드 포함</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.entity;