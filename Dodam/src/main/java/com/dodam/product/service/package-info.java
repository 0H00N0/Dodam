/**
 * 비즈니스 로직 서비스 패키지
 * 
 * <p>상품 도메인의 핵심 비즈니스 로직을 처리합니다.</p>
 * 
 * <p>주요 서비스:</p>
 * <ul>
 *   <li>{@code ProductService} - 상품 비즈니스 로직</li>
 *   <li>{@code ProductOptionService} - 상품 옵션 관리</li>
 *   <li>{@code InventoryService} - 재고 관리 (동시성 제어 포함)</li>
 * </ul>
 * 
 * <p>설계 원칙:</p>
 * <ul>
 *   <li>트랜잭션 경계 관리</li>
 *   <li>비즈니스 규칙 캡슐화</li>
 *   <li>도메인 객체와의 협력</li>
 *   <li>예외 처리 및 검증</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.service;