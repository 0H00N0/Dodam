package com.dodam.board;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    public List<NoticeEntity> findLatest() {
        return noticeRepository.findTop5ByOrderByPinnedDescCreatedAtDesc();
    }

    public List<NoticeEntity> findAll() {
        return noticeRepository.findAll();
    }

    public NoticeEntity findById(Long id) {
        return noticeRepository.findById(id).orElse(null);
    }

    public NoticeEntity save(NoticeEntity notice) {
        return noticeRepository.save(notice);
    }

    public void deleteById(Long id) {
        noticeRepository.deleteById(id);
    }
}
