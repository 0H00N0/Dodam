package com.dodam.admin.dto;

import com.dodam.board.entity.BoardCategoryEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class BoardManagementDTO {

    /**
     * 게시판 카테고리 생성을 위한 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateBoardCategoryRequest {
        private String categoryName;

        // DTO를 Entity로 변환하는 메서드
        public BoardCategoryEntity toEntity() {
            BoardCategoryEntity entity = new BoardCategoryEntity();
            entity.setBcname(this.categoryName);
            return entity;
        }
    }

    /**
     * 게시판 카테고리 정보 응답 DTO
     */
    @Getter
    @Builder
    public static class BoardCategoryResponse {
        private Long id;
        private String name;

        // Entity를 DTO로 변환하는 정적 팩토리 메서드
        public static BoardCategoryResponse fromEntity(BoardCategoryEntity entity) {
            return BoardCategoryResponse.builder()
                    .id(entity.getBcnum())
                    .name(entity.getBcname())
                    .build();
        }
    }
}
