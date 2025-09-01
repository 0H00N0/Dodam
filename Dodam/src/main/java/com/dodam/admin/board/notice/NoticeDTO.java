package com.dodam.admin.board.notice;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class NoticeDTO {
    private Long bnum;
    private String bsub;
    private String bcontent;
    private String mnic;
    private LocalDateTime bdate;

    public static NoticeDTO fromEntity(NoticeEntity entity) {
        NoticeDTO dto = new NoticeDTO();
        dto.setBnum(entity.getBnum());
        dto.setBsub(entity.getBsub());
        dto.setBcontent(entity.getBcontent());
        dto.setMnic(entity.getMnic());
        dto.setBdate(entity.getBdate());
        return dto;
    }

    public NoticeEntity toEntity() {
        NoticeEntity entity = new NoticeEntity();
        entity.setBsub(this.bsub);
        entity.setBcontent(this.bcontent);
        entity.setMid("admin");
        entity.setMnic("관리자");
        return entity;
    }
}
