package com.dodam.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 형식 클래스
 * 
 * <p>페이징된 데이터를 일관된 형태로 응답하기 위한 클래스입니다.</p>
 * 
 * @param <T> 페이징 데이터의 타입
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "페이징 응답")
public class PageResponse<T> {
    
    @Schema(description = "현재 페이지 데이터")
    private final List<T> content;
    
    @Schema(description = "페이징 정보")
    private final PageInfo page;
    
    /**
     * 생성자
     */
    private PageResponse(List<T> content, PageInfo page) {
        this.content = content;
        this.page = page;
    }
    
    /**
     * Spring Data Page 객체로부터 PageResponse를 생성합니다.
     * 
     * @param <T> 페이징 데이터 타입
     * @param page Spring Data Page 객체
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            PageInfo.of(page)
        );
    }
    
    /**
     * 직접 데이터를 지정하여 PageResponse를 생성합니다.
     * 
     * @param <T> 페이징 데이터 타입
     * @param content 페이지 데이터
     * @param page 페이징 정보
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> of(List<T> content, PageInfo page) {
        return new PageResponse<>(content, page);
    }
    
    /**
     * 페이징 정보 클래스
     */
    @Schema(description = "페이징 정보")
    public static class PageInfo {
        
        @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
        private final int number;
        
        @Schema(description = "페이지 크기", example = "20")
        private final int size;
        
        @Schema(description = "현재 페이지 요소 수", example = "15")
        private final int numberOfElements;
        
        @Schema(description = "전체 페이지 수", example = "5")
        private final int totalPages;
        
        @Schema(description = "전체 요소 수", example = "95")
        private final long totalElements;
        
        @Schema(description = "첫 번째 페이지 여부", example = "true")
        private final boolean first;
        
        @Schema(description = "마지막 페이지 여부", example = "false")
        private final boolean last;
        
        @Schema(description = "비어있는 페이지 여부", example = "false")
        private final boolean empty;
        
        /**
         * 생성자
         */
        private PageInfo(int number, int size, int numberOfElements, int totalPages, 
                        long totalElements, boolean first, boolean last, boolean empty) {
            this.number = number;
            this.size = size;
            this.numberOfElements = numberOfElements;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.first = first;
            this.last = last;
            this.empty = empty;
        }
        
        /**
         * Spring Data Page 객체로부터 PageInfo를 생성합니다.
         * 
         * @param page Spring Data Page 객체
         * @return PageInfo 객체
         */
        public static PageInfo of(Page<?> page) {
            return new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
            );
        }
        
        /**
         * 직접 정보를 지정하여 PageInfo를 생성합니다.
         */
        public static PageInfo of(int number, int size, int numberOfElements, int totalPages,
                                 long totalElements, boolean first, boolean last, boolean empty) {
            return new PageInfo(number, size, numberOfElements, totalPages,
                              totalElements, first, last, empty);
        }
        
        // === Getters ===
        
        public int getNumber() {
            return number;
        }
        
        public int getSize() {
            return size;
        }
        
        public int getNumberOfElements() {
            return numberOfElements;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public boolean isFirst() {
            return first;
        }
        
        public boolean isLast() {
            return last;
        }
        
        public boolean isEmpty() {
            return empty;
        }
    }
    
    // === Getters ===
    
    public List<T> getContent() {
        return content;
    }
    
    public PageInfo getPage() {
        return page;
    }
}