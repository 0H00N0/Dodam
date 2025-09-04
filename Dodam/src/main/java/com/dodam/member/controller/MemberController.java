package com.dodam.member.controller;

<<<<<<< HEAD
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
=======
import com.dodam.member.dto.MemberDTO;
import com.dodam.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("member")
>>>>>>> origin/chan
public class MemberController {

    private final MemberService service;

<<<<<<< HEAD
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
=======
    public MemberController(MemberService service) {
        this.service = service;
    }

    // === 요청/응답 유틸 ===
    private ResponseEntity<Map<String, Object>> ok(Object body) {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        if (body != null) res.put("data", body);
        return ResponseEntity.ok(res);
    }

    private ResponseEntity<Map<String, Object>> fail(int status, String message) {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", false);
        res.put("message", message);
        return ResponseEntity.status(status).body(res);
    }

    // === 로그인 요청 DTO (프론트 JSON 키와 일치) ===
    public static class LoginReq {
        public String mid;
        public String mpw;
    }

    // === 세션 상태 조회 ===
    @GetMapping("session")
    public ResponseEntity<?> session(HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) return ok(null);

        Map<String, Object> me = new HashMap<>();
        me.put("mid", sid);
        me.put("roleCode", session.getAttribute("sroleCode"));
        me.put("role", session.getAttribute("srole"));
        me.put("joinWay", session.getAttribute("sjoin"));
        return ok(me);
    }

    // === 회원가입(JSON) ===
    @PostMapping(value = "signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> signupJson(@RequestBody MemberDTO dto) {
        try {
            // MemberDTO 필드명은 프론트에서 보내는 JSON 키(mid, mpw, name, phone 등)와 일치해야 바인딩됩니다.
            service.signup(dto);
            return ok(null);
        } catch (IllegalArgumentException e) {
            return fail(400, e.getMessage());
        } catch (Exception e) {
            return fail(500, "signup failed");
        }
    }

    // === 로그인(JSON) ===
    @PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginJson(@RequestBody LoginReq req, HttpSession session) {
        if (req == null || req.mid == null || req.mpw == null) {
            return fail(400, "mid/mpw required");
        }
        boolean success = service.loginCheck(req.mid, req.mpw);
        if (!success) {
            return fail(401, "invalid credentials");
        }

        // 세션 세팅
        session.setAttribute("sid", req.mid);

        // 선택: 유저 부가정보 세션에 저장 (서비스에 구현돼 있다고 가정)
        MemberDTO me = service.readByMid(req.mid);
        if (me != null) {
            session.setAttribute("sroleCode", me.getRoleCode());
            session.setAttribute("srole", me.getRoleName());
            session.setAttribute("sjoin", me.getJoinWay());
        }

        return ok(null);
    }

    // === 로그아웃(JSON) ===
    @PostMapping("logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ok(null);
>>>>>>> origin/chan
    }
}
