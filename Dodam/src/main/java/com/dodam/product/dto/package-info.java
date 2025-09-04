/**
 * 데이터 전송 객체(DTO) 패키지
 * 
 * <p>API 요청/응답 및 내부 데이터 전송을 위한 DTO 클래스들을 포함합니다.</p>
 * 
 * <p>하위 패키지:</p>
 * <ul>
 *   <li>{@code request} - API 요청 DTO</li>
 *   <li>{@code response} - API 응답 DTO</li>
 * </ul>
 * 
 * <p>설계 특징:</p>
 * <ul>
 *   <li>Bean Validation 적용</li>
 *   <li>불변 객체 구현</li>
 *   <li>명확한 계층 분리</li>
 *   <li>도메인 객체로부터의 변환 메서드</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.dodam.product.dto;