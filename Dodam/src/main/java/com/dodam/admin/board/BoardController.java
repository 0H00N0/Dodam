package com.dodam.admin.board;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dodam.admin.board.notice.NoticeDTO;
import com.dodam.admin.board.notice.NoticeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notices")
public class BoardController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<List<NoticeDTO>> getAllNotices() {
        List<NoticeDTO> notices = noticeService.findAllNotices();
        return ResponseEntity.ok(notices);
    }

    @PostMapping
    public ResponseEntity<NoticeDTO> createNotice(@RequestBody NoticeDTO noticeDTO) {
        NoticeDTO createdNotice = noticeService.createNotice(noticeDTO);
        return new ResponseEntity<>(createdNotice, HttpStatus.CREATED);
    }
}

