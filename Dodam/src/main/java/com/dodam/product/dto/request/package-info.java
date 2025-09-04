/**
 * API 요청 DTO 패키지
 * 
 * <p>클라이언트로부터 받는 요청 데이터를 정의합니다.</p>
 * 
 * <p>주요 Request DTO:</p>
 * <ul>
 *   <li>{@code ProductCreateRequest} - 상품 등록 요청</li>
 *   <li>{@code ProductUpdateRequest} - 상품 수정 요청</li>
 *   <li>{@code ProductSearchRequest} - 상품 검색 요청</li>
 * </ul>
 * 
 * <p>검증 특징:</p>
 * <ul>
 *   <li>javax.validation 어노테이션 활용</li>
 *   <li>커스텀 검증 어노테이션 적용</li>
 *   <li>비즈니스 규칙 검증</li>
 *   <li>국제화 메시지 지원</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.dto.request;