package com.dodam.board.dto;
import java.time.LocalDateTime;

import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BoardDto { 
	private String code; 
	private String name; 
	private String description; 
	private Long id;
    private String title;
    private String content;
    private String author;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
