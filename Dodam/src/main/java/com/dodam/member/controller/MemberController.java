package com.dodam.member.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.dodam.member.dto.MemberDTO;
import com.dodam.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService service;

    @GetMapping("signup")
    public String signupForm(){ return "member/signupForm"; }

    @PostMapping("signup")
    public String signup(MemberDTO dto){
        service.signup(dto);
        return "redirect:/";
    }

    @GetMapping("login")
    public String loginForm(){ return "member/loginForm"; }

    @PostMapping("login")
    public String login(@RequestParam String mid,
                        @RequestParam String mpw,
                        HttpSession session){
        if (service.loginCheck(mid, mpw)) {
            session.setAttribute("sid", mid);
            var me = service.readByMid(mid);
            session.setAttribute("sroleCode", me.getRoleCode()); // 0/1/2/3
            session.setAttribute("srole", me.getRoleName());     // 일반 / SuperAdmin / ...
            session.setAttribute("sjoin", me.getJoinWay());      // local 등
        }
        return "redirect:/";
    }

    @GetMapping("logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("info")
    public String info(HttpSession session, Model model){
        String sid = (String) session.getAttribute("sid");
        if (sid == null) return "redirect:/member/login";
        model.addAttribute("me", service.readByMid(sid));
        return "member/info";
    }
    
    @GetMapping("session")
    @ResponseBody
    public Map<String, Object> session(HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        return Map.of(
            "authenticated", sid != null,
            "username", sid
        );
    }
}
