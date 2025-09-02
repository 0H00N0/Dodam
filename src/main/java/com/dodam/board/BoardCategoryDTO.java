package com.dodam.board;

import com.dodam.board.BoardCategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class BoardCategoryDTO {

    @Getter @Setter
    @NoArgsConstructor
    public static class Request {
        private String bcname;
    }

    @Getter
    public static class Response {
        private final Long bcnum;
        private final String bcname;

        public Response(BoardCategory entity) {
            this.bcnum = entity.getBcnum();
            this.bcname = entity.getBcname();
        }
    }
}
