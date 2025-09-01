package com.dodam.admin.controller;

import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.service.AdminService;

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
    public String login(@RequestParam(value = "username") String username,
                       @RequestParam(value = "password") String password,
                       @RequestParam(value = "userType") String userType,
                       HttpServletResponse response,
                       Model model) {
        try {
            System.out.println("로그인 시도: " + username + ", userType: " + userType);
            AdminEntity admin = adminService.authenticate(username, password);
            System.out.println("인증 성공: " + admin.getUsername() + ", Role: " + admin.getRole());

            // userType과 실제 DB role이 일치하는지 검증
            boolean roleMatches = false;
            if ("ADMIN".equals(userType)) {
                roleMatches = (admin.getRole() == com.dodam.admin.entity.AdminRole.ADMIN || 
                              admin.getRole() == com.dodam.admin.entity.AdminRole.SUPER_ADMIN);
            } else if ("DELIVERYMAN".equals(userType)) {
                roleMatches = (admin.getRole() == com.dodam.admin.entity.AdminRole.DELIVERYMAN);
            }
            
            System.out.println("Role 매칭 결과: " + roleMatches);
            
            if (!roleMatches) {
                model.addAttribute("errorMessage", "선택한 사용자 유형과 계정 권한이 일치하지 않습니다.");
                return "admin/login";
            }

            // 쿠키에 사용자 정보 저장
            Cookie usernameCookie = new Cookie("username", admin.getUsername());
            usernameCookie.setPath("/");
            usernameCookie.setMaxAge(24 * 60 * 60); // 24시간 유효
            response.addCookie(usernameCookie);

            Cookie roleCookie = new Cookie("role", admin.getRole().name());
            roleCookie.setPath("/");
            roleCookie.setMaxAge(24 * 60 * 60); // 24시간 유효
            response.addCookie(roleCookie);

            if (admin.getName() != null) {
                Cookie nameCookie = new Cookie("name", admin.getName());
                nameCookie.setPath("/");
                nameCookie.setMaxAge(24 * 60 * 60); // 24시간 유효
                response.addCookie(nameCookie);
            }

            // 역할에 따른 리다이렉트
            String redirectUrl;
            switch (admin.getRole()) {
                case ADMIN:
                case SUPER_ADMIN:
                    redirectUrl = "redirect:/admin/main";
                    break;
                case DELIVERYMAN:
                    redirectUrl = "redirect:/admin/logistics";
                    break;
                case STAFF:
                    redirectUrl = "redirect:/admin/dashboard";
                    break;
                default:
                    redirectUrl = "redirect:/admin/dashboard";
                    break;
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

    /**
     * 관리자 메인
     */
    @GetMapping("/main")
    public String main(Model model) {
        return "admin/main";
    }

    /**
     * 배송 담당자 페이지
     */
    @GetMapping("/logistics")
    public String logistics(Model model) {
        return "admin/logistics";
    }
}