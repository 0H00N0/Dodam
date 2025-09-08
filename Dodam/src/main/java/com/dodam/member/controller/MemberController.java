package com.dodam.member.controller;

import com.dodam.member.dto.MemberDTO;
import com.dodam.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 프론트 규약: /member/signup  (JSON)
    @PostMapping(
            value = "/signup",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> signup(@Valid @RequestBody MemberDTO dto) {
        memberService.signup(dto); // 내부에서 중복 검사/예외 던짐
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "signup ok"));
    }

    // 프론트 규약: /member/loginForm  (JSON)
    // 추후 호환 위해 /login 도 함께 허용하고 싶으면 아래처럼 배열로 추가 가능
    // @PostMapping(value = {"/loginForm", "/login"}, ...)
    @PostMapping(
            value = "/loginForm",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> login(@RequestBody MemberDTO dto, HttpSession session) {
        var member = memberService.login(dto.getMid(), dto.getMpw()); // 실패 시 예외
        session.setAttribute("sid", member.getMid()); // React axios withCredentials=true 일 때 JSESSIONID 쿠키 저장
        return ResponseEntity.ok(Map.of(
                "message", "login ok",
                "mid", member.getMid(),
                "mname", member.getMname()
        ));
    }

    // (선택) 로그아웃
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
    
    //회원정보 수정 - 프론트엔드 API 경로에 맞춤
    @PutMapping("/api/member/me")
    public ResponseEntity<?> updateProfile(@RequestBody MemberDTO dto, HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        memberService.updateProfile(sid, dto);
        return ResponseEntity.ok().build();
    }

 // 비밀번호 변경 - 프론트엔드 API 경로 및 JSON body 방식에 맞춤
    @PutMapping("/api/member/me/password")
    public ResponseEntity<String> changePassword(
            HttpSession session,
            @RequestBody Map<String, String> passwordData) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        String currentPw = passwordData.get("currentPw");
        String newPw = passwordData.get("newPw");
        
        memberService.changePw(sid, currentPw, newPw);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // (선택) 아이디 중복 체크: /member/check-id?mid=abc
    @GetMapping(value = "/check-id", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> checkId(@RequestParam String mid) {
        boolean exists = memberService.exists(mid);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
    
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
