package com.dodam.admin.controller;

import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminService adminService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "logout", required = false) String logout,
                          Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "아이디, 비밀번호 또는 역할이 올바르지 않습니다.");
        }
        
        if (logout != null) {
            model.addAttribute("logoutMessage", "성공적으로 로그아웃되었습니다.");
        }
        
        return "admin/login";
    }

    
    
    // 대시보드 페이지 (로그인 후 이동할 페이지)
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
    
    // ADMIN 전용 메인 페이지
    @GetMapping("/main")
    public String main() {
        return "admin/main";
    }

    // REPORTER 전용 로지스틱스 페이지
    @GetMapping("/logistics")
    public String logistics() {
        return "admin/logistics";
    }
}
