package com.dodam.member.controller;

import com.dodam.member.dto.ChangePwDTO;
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
    
    //회원정보 수정
    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestBody MemberDTO dto, HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        memberService.updateProfile(sid, dto);
        return ResponseEntity.ok().build();
    }

 // 비밀번호 변경
    @PutMapping("/changePw")
    public ResponseEntity<?> changePw(@RequestBody ChangePwDTO dto, HttpSession session) {
        String sid = (String) session.getAttribute("sid");
        if (sid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        memberService.changePw(sid, dto);
        return ResponseEntity.ok().build();
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
    @GetMapping("/findIdByTel")
    public ResponseEntity<?> findIdByNameAndTel(
        @RequestParam("mname") String mname,
        @RequestParam("mtel") String mtel
    ) {
        String mid = memberService.findIdByNameAndTel(mname, mtel);
        return ResponseEntity.ok(Map.of("mid", mid));
    }

    // 이름+이메일로 아이디 찾기
    @GetMapping("/findIdByEmail")
    public ResponseEntity<?> findIdByNameAndEmail(
        @RequestParam("mname") String mname,
        @RequestParam("memail") String memail
    ) {
        String mid = memberService.findIdByNameAndEmail(mname, memail);
        return ResponseEntity.ok(Map.of("mid", mid));
    }
    
 // 이메일로 임시 비밀번호 발급 및 발송
    @PostMapping("/findPwByMemail")
    public ResponseEntity<?> sendTempPwByEmail(
        @RequestParam("mid") String mid,
        @RequestParam("mname") String mname,
        @RequestParam("memail") String memail
    ) {
        boolean exists = memberService.existsByMidNameEmail(mid, mname, memail);
        if (exists) {
            String tempPw = memberService.generateTempPassword();
            memberService.updatePassword(mid, tempPw); // 비밀번호 변경
            memberService.sendEmail(memail, "임시 비밀번호 안내", "임시 비밀번호: " + tempPw); // 이메일 발송
            return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 이메일로 발송되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "일치하는 회원이 없습니다."));
        }
    }

    // 전화번호로 임시 비밀번호 발급 및 문자 발송
    @PostMapping("/findPwByMtel")
    public ResponseEntity<?> sendTempPwByTel(
        @RequestParam("mid") String mid,
        @RequestParam("mname") String mname,
        @RequestParam("mtel") String mtel
    ) {
        boolean exists = memberService.existsByMidNameTel(mid, mname, mtel);
        if (exists) {
            String tempPw = memberService.generateTempPassword();
            memberService.updatePassword(mid, tempPw); // 비밀번호 변경
            memberService.sendSms(mtel, "임시 비밀번호: " + tempPw); // 문자 발송
            return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 문자로 발송되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "일치하는 회원이 없습니다."));
        }
    }
}
