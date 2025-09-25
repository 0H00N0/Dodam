package com.dodam.board.dto.notice;
import lombok.*; import java.time.*;

import com.dodam.board.entity.NoticeEntity.NoticeEntityBuilder;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builderpublic NoticeEntityBuilder boardCode(Class<? extends Long> class1) {
		// TODO Auto-generated method stub
		return null;
	}
public class NoticeResponse {
    private Long id; private String boardCode; private String title; private String content;
    private boolean pinned; private int views; private String status; private LocalDateTime createdAt; private LocalDateTime updatedAt;
	public static NoticeEntityBuilder builder() {
		// TODO Auto-generated method stub
		return null;
	}
}
