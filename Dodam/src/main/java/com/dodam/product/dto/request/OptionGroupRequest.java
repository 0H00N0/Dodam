package com.dodam.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 옵션 그룹 생성/수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupRequest {

    /**
     * 상품 ID
     */
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    /**
     * 옵션 그룹명 (예: "색상", "사이즈", "소재")
     */
    @NotBlank(message = "옵션 그룹명은 필수입니다")
    private String groupName;

    /**
     * 옵션 그룹 표시명 (화면에 표시되는 이름)
     */
    @NotBlank(message = "옵션 그룹 표시명은 필수입니다")
    private String displayName;

    /**
     * 옵션 그룹 순서 (화면 표시 순서)
     */
    @PositiveOrZero(message = "순서는 0 이상이어야 합니다")
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 필수 선택 여부
     */
    @Builder.Default
    private Boolean isRequired = true;

    /**
     * 다중 선택 가능 여부
     */
    @Builder.Default
    private Boolean isMultipleChoice = false;

    /**
     * 최대 선택 개수 (다중 선택인 경우)
     */
    @PositiveOrZero(message = "최대 선택 개수는 0 이상이어야 합니다")
    @Builder.Default
    private Integer maxSelections = 1;

    /**
     * 옵션 그룹 설명
     */
    private String description;

    /**
     * 옵션 그룹 활성화 여부
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 옵션 그룹 타입 (COLOR, SIZE, MATERIAL 등)
     */
    private String groupType;
}