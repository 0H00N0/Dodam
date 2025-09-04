package com.dodam.admin.board.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 관리 DTO 클래스들
 */
public class AdminBoardDTO {

    // 게시글 목록 조회용 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoardListResponse {
        private Long bnum;
        private String btitle;
        private String mid;
        private String mnic;
        private String categoryName;
        private String stateName;
        private LocalDateTime bdate;
        private LocalDateTime bedate;
    }

    // 게시글 상세 조회용 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoardDetailResponse {
        private Long bnum;
        private Long mnum;
        private Long tnum;
        private String btitle;
        private String bcontent;
        private String mid;
        private String mnic;
        private LocalDateTime bdate;
        private LocalDateTime bedate;
        private BoardCategoryResponse category;
        private BoardStateResponse state;
    }

    // 게시글 수정용 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardUpdateRequest {
        private String btitle;
        private String bcontent;
        private Long bcnum; // 카테고리 번호
        private Long bsnum; // 상태 번호
    }

    // 게시글 검색 조건 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoardSearchRequest {
        private String keyword;
        private String searchType;
        private Long bcnum;
        private Long bsnum;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        @Builder.Default
        private int page = 0;
        @Builder.Default
        private int size = 20;
        @Builder.Default
        private String sortBy = "bdate";
        @Builder.Default
        private String sortDir = "desc";
    }

    // 카테고리 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoardCategoryResponse {
        private Long bcnum;
        private String bcname;
        private Long boardCount;
    }

    // 카테고리 생성/수정 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardCategoryRequest {
        private String bcname;
    }

    // 상태 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BoardStateResponse {
        private Long bsnum;
        private String bsname;
        private Long boardCount;
    }

    // 상태 생성/수정 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoardStateRequest {
        private String bsname;
    }

    // 페이지네이션 응답 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }

    // 일괄 작업 요청 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkActionRequest {
        private List<Long> boardIds;
        private String action;
        private Long targetStateId;
        private Long targetCategoryId;
    }

    // API 응답 래퍼 DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String errorCode;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder() 
                    .success(false)
                    .message(message)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message, String errorCode) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .errorCode(errorCode)
                    .build();
        }
    }
}