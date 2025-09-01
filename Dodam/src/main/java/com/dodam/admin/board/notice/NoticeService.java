package com.dodam.admin.board.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public List<NoticeDTO> findAllNotices() {
        return noticeRepository.findAll().stream()
                .map(NoticeDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public NoticeDTO createNotice(NoticeDTO noticeDTO) {
        NoticeEntity noticeEntity = noticeDTO.toEntity();
        NoticeEntity savedEntity = noticeRepository.save(noticeEntity);
        return NoticeDTO.fromEntity(savedEntity); 
    }
}
