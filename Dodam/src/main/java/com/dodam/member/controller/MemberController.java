package com.dodam.member.controller;

import com.dodam.member.dto.ChangePwDTO;
import com.dodam.member.dto.MemberDTO;
import com.dodam.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping(
            value = "/signup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> signup(@Valid @RequestBody MemberDTO dto) {
        memberService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "signup ok"));
    }

    // 로그인
    @PostMapping(
            value = "/loginForm",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> login(@RequestBody MemberDTO dto, HttpSession session) {
        var member = memberService.login(dto.getMid(), dto.getMpw());
        session.setAttribute("sid", member.getMid());
        return ResponseEntity.ok(Map.of(
                "message", "login ok",
                "mid", member.getMid(),
                "mname", member.getMname()
        ));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "logout ok"));
    }

    // 회원정보 조회
    @GetMapping("/api/member/me")
    public ResponseEntity<?> getProfile(HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        MemberDTO member = memberService.me(sid);
        return ResponseEntity.ok(member);
    }

    // 회원정보 수정
    @PutMapping("/api/member/me")
    public ResponseEntity<?> updateProfile(@RequestBody MemberDTO dto, HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        memberService.updateProfile(sid, dto);
        return ResponseEntity.ok().build();
    }

    // 비밀번호 변경 (plan 브랜치 방식)
    @PutMapping("/changePw")
    public ResponseEntity<?> changePw(@RequestBody ChangePwDTO dto, HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        memberService.changePw(sid, dto);
        return ResponseEntity.ok().build();
    }

    // 아이디 중복 체크
    @GetMapping(value = "/check-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> checkId(@RequestParam String mid) {
        boolean exists = memberService.exists(mid);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // 세션 기반 내 정보 조회
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> me(HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "unauthenticated"));
        }
        return ResponseEntity.ok(memberService.me(sid));
    }

    // 이름+전화번호로 아이디 찾기
    @GetMapping("/findid/tel")
    public ResponseEntity<?> findIdByNameAndTel(@RequestParam String mname, @RequestParam String mtel) {
        String mid = memberService.findIdByNameAndTel(mname, mtel);
        return ResponseEntity.ok(Map.of("mid", mid));
    }

    // 이름+이메일로 아이디 찾기
    @GetMapping("/findid/email")
    public ResponseEntity<?> findIdByNameAndEmail(@RequestParam String mname, @RequestParam String memail) {
        String mid = memberService.findIdByNameAndEmail(mname, memail);
        return ResponseEntity.ok(Map.of("mid", mid));
    }
}
