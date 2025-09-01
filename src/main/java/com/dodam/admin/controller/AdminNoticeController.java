package com.dodam.admin.controller;

import com.dodam.board.NoticeEntity;
import com.dodam.board.NoticeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/notices")
public class AdminNoticeController {

    private final NoticeService noticeService;

    public AdminNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public String noticeList(Model model) {
        model.addAttribute("notices", noticeService.findAll());
        return "admin/notice/list";
    }

    @GetMapping("/add")
    public String addNoticeForm(Model model) {
        model.addAttribute("notice", new NoticeEntity());
        return "admin/notice/form";
    }

    @PostMapping("/add")
    public String addNotice(@ModelAttribute NoticeEntity notice) {
        noticeService.save(notice);
        return "redirect:/admin/notices";
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
        return "redirect:/admin/notices";
    }

    @GetMapping("/delete/{id}")
    public String deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteById(id);
        return "redirect:/admin/notices";
    }
}
