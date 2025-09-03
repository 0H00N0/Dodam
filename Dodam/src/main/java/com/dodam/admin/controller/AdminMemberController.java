package com.dodam.admin.controller;

import com.dodam.member.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final MemberService memberService;

    public AdminMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public String memberList(Model model) {
        model.addAttribute("members", memberService.findAll());
        return "admin/member/list";
    }

    @GetMapping("/view/{id}")
    public String memberView(@PathVariable("id") Long id, Model model) {
        model.addAttribute("member", memberService.findById(id));
        return "admin/member/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteMember(@PathVariable("id") Long id) {
        memberService.deleteById(id);
        return "redirect:/admin/members";
    }
}
