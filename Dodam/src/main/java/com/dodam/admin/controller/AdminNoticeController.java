package com.dodam.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.dodam.admin.board.NoticeEntity;
import com.dodam.admin.board.NoticeService;

@Controller
@RequestMapping("/admin/notice")
public class AdminNoticeController {

    private final NoticeService noticeService;

    public AdminNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping({"/list"})
    public String list(Model model) {
        List<NoticeEntity> notices = noticeService.findAll(); // 정렬은 나중에
        model.addAttribute("notices", notices); // DTO 변환 생략
        return "admin/notice/list"; // 템플릿 경로 확인
    }

    @GetMapping("/add")
    public String addNoticeForm(Model model) {
        model.addAttribute("notice", new NoticeEntity());
        return "admin/notice/form";
    }

    @PostMapping("/add")
    public String addNotice(@ModelAttribute NoticeEntity notice) {
        noticeService.save(notice);
        return "redirect:/admin/notice/list";
    }

    @GetMapping("/edit/{id}")
    public String editNoticeForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("notice", noticeService.findById(id));
        return "admin/notice/form";
    }

    @PostMapping("/edit/{id}")
    public String editNotice(@PathVariable("id") Long id, @ModelAttribute NoticeEntity notice) {
        notice.setId(id);
        noticeService.save(notice);
        return "redirect:/admin/notice/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteById(id);
        return "redirect:/admin/notice/list";
    }
}
