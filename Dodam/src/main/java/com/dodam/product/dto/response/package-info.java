/**
 * API 응답 DTO 패키지
 * 
 * <p>클라이언트에게 전송하는 응답 데이터를 정의합니다.</p>
 * 
 * <p>주요 Response DTO:</p>
 * <ul>
 *   <li>{@code ProductResponse} - 기본 상품 응답</li>
 *   <li>{@code ProductDetailResponse} - 상품 상세 응답</li>
 *   <li>{@code ProductListResponse} - 상품 목록 응답</li>
 * </ul>
 * 
 * <p>설계 특징:</p>
 * <ul>
 *   <li>불변 객체 구현</li>
 *   <li>엔티티로부터 변환 팩토리 메서드</li>
 *   <li>JSON 직렬화 최적화</li>
 *   <li>민감 정보 제외</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.dto.response;