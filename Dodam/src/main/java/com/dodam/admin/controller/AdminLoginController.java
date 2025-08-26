package com.dodam.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping; // <-- 추가

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "logout", required = false) String logout,
                          Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        
        if (logout != null) {
            model.addAttribute("logoutMessage", "성공적으로 로그아웃되었습니다.");
        }
        
        return "admin/login"; // templates/admin/login.html을 반환
    }
    
    // 대시보드 페이지 (로그인 후 이동할 페이지)
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    // 새로운 로그인 처리 메서드
    // @RequestParam으로 userType과 username, password를 받습니다.
    @PostMapping("/process")
    public String processLogin(@RequestParam("userType") String userType, 
                             @RequestParam("username") String username,
                             @RequestParam("password") String password) {
        
        // 실제로는 DB에서 사용자 정보를 확인하는 로직이 필요합니다.
        // 여기서는 간단하게 userType만으로 페이지를 결정합니다.
        
        if ("ADMIN".equals(userType)) {
            // 관리자 계정일 경우 main 페이지로 리다이렉트
            // URL을 직접 명시하여 이동시킵니다.
            return "redirect:/admin/main";
        } else if ("REPORTER".equals(userType)) {
            // 기자 계정일 경우 logistics 페이지로 리다이렉트
            // URL을 직접 명시하여 이동시킵니다.
            return "redirect:/admin/logistics";
        }
        
        // 유효하지 않은 유형일 경우 로그인 페이지로 다시 보냄
        return "redirect:/admin/login?error";
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