package com.dodam.product.dto.request;

import com.dodam.product.entity.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 카테고리 요청 DTO
 * 카테고리 생성 및 수정 요청 시 사용됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDto {

    /**
     * 카테고리 이름 (필수값)
     * 1자 이상 100자 이하로 입력해야 합니다.
     */
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Size(min = 1, max = 100, message = "카테고리 이름은 1자 이상 100자 이하로 입력해주세요.")
    private String categoryName;

    /**
     * 카테고리 설명 (선택값)
     * 500자 이하로 입력 가능합니다.
     */
    @Size(max = 500, message = "카테고리 설명은 500자 이하로 입력해주세요.")
    private String description;

    /**
     * 생성일시 (수정 시에만 사용)
     * 클라이언트에서 전달받는 경우에만 설정됩니다.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    /**
     * 수정일시 (수정 시에만 사용)
     * 클라이언트에서 전달받는 경우에만 설정됩니다.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    /**
     * RequestDto를 Entity로 변환하는 메소드 (생성용)
     * 
     * @return Category 엔티티 객체
     */
    public Category toEntity() {
        return Category.builder()
                .categoryName(this.categoryName)
                .description(this.description)
                .build();
    }

    /**
     * RequestDto를 Entity로 변환하는 메소드 (수정용)
     * 
     * @param categoryId 카테고리 ID
     * @return Category 엔티티 객체
     */
    public Category toEntity(Long categoryId) {
        return Category.builder()
                .categoryId(categoryId)
                .categoryName(this.categoryName)
                .description(this.description)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * 기존 Entity를 RequestDto로 업데이트하는 메소드
     * 
     * @param category 업데이트할 카테고리 엔티티
     */
    public void updateEntity(Category category) {
        if (this.categoryName != null) {
            category.setCategoryName(this.categoryName);
        }
        if (this.description != null) {
            category.setDescription(this.description);
        }
    }

    /**
     * 유효성 검사 통과 여부 확인
     * 
     * @return 유효성 검사 통과 여부
     */
    public boolean isValid() {
        return categoryName != null && 
               !categoryName.trim().isEmpty() && 
               categoryName.length() <= 100 &&
               (description == null || description.length() <= 500);
    }

    /**
     * 카테고리 이름 정규화 (앞뒤 공백 제거)
     * 
     * @return 정규화된 RequestDto
     */
    public CategoryRequestDto normalize() {
        if (this.categoryName != null) {
            this.categoryName = this.categoryName.trim();
        }
        if (this.description != null && this.description.trim().isEmpty()) {
            this.description = null;
        }
        return this;
    }

    /**
     * 생성용 RequestDto 생성 팩토리 메소드
     * 
     * @param categoryName 카테고리 이름
     * @param description 카테고리 설명
     * @return CategoryRequestDto 객체
     */
    public static CategoryRequestDto createRequest(String categoryName, String description) {
        return CategoryRequestDto.builder()
                .categoryName(categoryName)
                .description(description)
                .build();
    }

    /**
     * 수정용 RequestDto 생성 팩토리 메소드
     * 
     * @param categoryName 카테고리 이름
     * @param description 카테고리 설명
     * @return CategoryRequestDto 객체
     */
    public static CategoryRequestDto updateRequest(String categoryName, String description) {
        return CategoryRequestDto.builder()
                .categoryName(categoryName)
                .description(description)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Entity로부터 RequestDto 생성 (수정 시 기존 값 로드용)
     * 
     * @param category 카테고리 엔티티
     * @return CategoryRequestDto 객체
     */
    public static CategoryRequestDto fromEntity(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryRequestDto.builder()
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    @Override
    public String toString() {
        return String.format("CategoryRequestDto(categoryName=%s, description=%s)", 
                           categoryName, 
                           description != null && description.length() > 50 
                               ? description.substring(0, 50) + "..." 
                               : description);
    }
}