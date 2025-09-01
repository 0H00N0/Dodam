package com.dodam.admin.board.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    public List<NoticeDTO> findAllNotices() {
        // 임시 데이터 반환 (실제로는 Repository를 통해 DB에서 조회)
        List<NoticeDTO> notices = new ArrayList<>();
        
        NoticeDTO notice1 = NoticeDTO.builder()
                .id(1L)
                .title("시스템 점검 안내")
                .content("시스템 점검으로 인한 서비스 일시 중단 안내드립니다.")
                .author("관리자")
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
                
        NoticeDTO notice2 = NoticeDTO.builder()
                .id(2L)
                .title("새로운 기능 업데이트")
                .content("새로운 기능이 추가되었습니다.")
                .author("관리자")
                .isActive(true)
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(2))
                .build();
        
        notices.add(notice1);
        notices.add(notice2);
        
        return notices;
    }

    public NoticeDTO createNotice(NoticeDTO noticeDTO) {
        // 임시 구현 (실제로는 Repository를 통해 DB에 저장)
        noticeDTO.setId(System.currentTimeMillis()); // 임시 ID 생성
        noticeDTO.setCreatedAt(LocalDateTime.now());
        noticeDTO.setUpdatedAt(LocalDateTime.now());
        
        if (noticeDTO.getIsActive() == null) {
            noticeDTO.setIsActive(true);
        }
        
        return noticeDTO;
    }
}