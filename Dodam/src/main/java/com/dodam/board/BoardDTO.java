package com.dodam.board;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.dodam.board.Board;

public class BoardDTO {

    @Getter
    public static class Response {
        private final Long bnum;
        private final String btitle;
        private final String mnic;
        private final LocalDateTime bdate;
        private final String categoryName;
        private final String stateName;
        private final Long bcnum;
        private final Long bsnum;

        public Response(Board entity) {
            this.bnum = entity.getBnum();
            this.btitle = entity.getBtitle();
            this.mnic = entity.getMnic();
            this.bdate = entity.getBdate();
            this.categoryName = entity.getBoardCategory() != null ? entity.getBoardCategory().getBcname() : "없음";
            this.stateName = entity.getBoardState() != null ? entity.getBoardState().getBsname() : "없음";
            this.bcnum = entity.getBoardCategory() != null ? entity.getBoardCategory().getBcnum() : null;
            this.bsnum = entity.getBoardState() != null ? entity.getBoardState().getBsnum() : null;
        }
    }

    @Getter @Setter
    public static class UpdateRequest {
        private Long bcnum;
        private Long bsnum;
    }
}