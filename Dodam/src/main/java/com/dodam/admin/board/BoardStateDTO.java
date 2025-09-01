package com.dodam.admin.board;

import com.dodam.admin.board.BoardState;
import lombok.Getter;

@Getter
public class BoardStateDTO {
    private final Long bsnum;
    private final String bsname;

    public BoardStateDTO(BoardState entity) {
        this.bsnum = entity.getBsnum();
        this.bsname = entity.getBsname();
    }
}
