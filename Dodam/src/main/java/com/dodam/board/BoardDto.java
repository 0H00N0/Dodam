package com.dodam.board;

import lombok.Data;

@Data
public class BoardDto {
    private Long id;
    private String title;
    private String content;
    private String writer;
}
