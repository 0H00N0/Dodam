package com.dodam.admin.controller;

import com.dodam.admin.service.AdminService;
import com.dodam.member.entity.MemberEntity;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 관리자 로그인 및 관련 페이지를 처리하는 컨트롤러 클래스입니다.
 * 개발 단계에서는 Spring Security를 비활성화하고 쿠키 기반으로 동작합니다.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminService adminService;

    /**
     * 관리자 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    /**
     * 관리자 로그인 처리
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam("mtnum") int mtnum,   // 숫자 그대로 받음
                        HttpServletResponse response,
                        Model model) {
        try {
            System.out.println("로그인 시도: " + username + ", userType: " + mtnum);

            MemberEntity member = adminService.authenticate(username, password);
            if (member == null) {
                model.addAttribute("errorMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
                return "admin/login";
            }

            System.out.println("인증 성공: " + member.getMid() + ", Role: " + member.getRoleString());

            // 숫자 기반 역할 매칭
            boolean roleMatches = false;
            switch (mtnum) {
                case 1: // 슈퍼 관리자
                    roleMatches = member.isSuperAdmin();
                    break;
                case 2: // 스태프
                    roleMatches = member.isStaff();
                    break;
                case 3: // 배송 담당자
                    roleMatches = member.isDeliveryman();
                    break;
                default:
                    roleMatches = false;
            }

            System.out.println("Role 매칭 결과: " + roleMatches);

            if (!roleMatches) {
                model.addAttribute("errorMessage", "선택한 사용자 유형과 계정 권한이 일치하지 않습니다.");
                return "admin/login";
            }

            // (쿠키 저장 부분 동일)
            Cookie usernameCookie = new Cookie("username", safeCookieValue(member.getMid()));
            usernameCookie.setPath("/");
            usernameCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(usernameCookie);

            Cookie roleCookie = new Cookie("role", safeCookieValue(member.getRoleString()));
            roleCookie.setPath("/");
            roleCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(roleCookie);

            if (member.getMname() != null) {
                Cookie nameCookie = new Cookie("name", safeCookieValue(member.getMname()));
                nameCookie.setPath("/");
                nameCookie.setMaxAge(24 * 60 * 60);
                response.addCookie(nameCookie);
            }

            // 리다이렉트 경로
            String redirectUrl;
            if (member.isSuperAdmin() || member.isStaff()) {
                redirectUrl = "redirect:/admin/main";
            } else if (member.isDeliveryman()) {
                redirectUrl = "redirect:/admin/logistics";
            } else {
                redirectUrl = "redirect:/admin/dashboard";
            }

            System.out.println("리다이렉트 URL: " + redirectUrl);
            return redirectUrl;

        } catch (Exception e) {
            model.addAttribute("errorMessage", "로그인에 실패했습니다: " + e.getMessage());
            return "admin/login";
        }
    }


    /**
     * 관리자 로그아웃 처리
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // 모든 쿠키 삭제
        String[] cookieNames = {"username", "role", "name"};
        
        for (String cookieName : cookieNames) {
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(Map.of("result", true, "message", "로그아웃되었습니다."));
    }

    /**
     * 관리자 대시보드
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 쿠키에서 사용자 정보를 가져와서 모델에 추가할 수 있음
        return "admin/dashboard";
    }

    
    @GetMapping("/logistics")
    public String logistics(Model model) {
        return "admin/logistics";
    }
    
    /**
     * 쿠키 값에 사용할 수 있는 안전한 문자열로 변환
     */
    private String safeCookieValue(String value) {
        if (value == null) {
            return "";
        }
        // 쿠키에서 문제가 될 수 있는 특수문자들을 처리
        return value.replaceAll("[;,\\s]", "_");
    }

}