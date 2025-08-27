package com.dodam.admin.controller;

import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.service.AdminService;
import com.todo.dto.MemberDTO;
import com.todo.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 관리자 로그인 및 관련 페이지를 처리하는 컨트롤러 클래스입니다.
 * Spring Security를 통해 인증이 이루어지며, 로그인 성공 후 역할에 따라 다른 페이지로 리디렉션됩니다.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminService adminService;
    
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                             @RequestParam String password,
                             HttpSession session,
                             Model model) {
        try {
            AdminEntity admin = adminService.authenticate(username, password);
            
            // 세션에 사용자 정보 저장
            session.setAttribute("user", admin);
            session.setAttribute("username", admin.getUsername());
            session.setAttribute("role", admin.getRole().name());
            
            // 역할에 따른 리다이렉트
            switch (admin.getRole()) {
                case ADMIN:
                case SUPER_ADMIN:
                    return "redirect:/admin/main";
                case DELIVERYMAN:
                    return "redirect:/admin/logistics";
                default:
                    return "redirect:/admin/dashboard";
            }
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "로그인에 실패했습니다: " + e.getMessage());
            return "admin/login";
        }
    }
    
   

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session){
        session.invalidate();
        return ResponseEntity.ok(Map.of("result", true));
        
        
    }
        
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
   
    @GetMapping("/main")
    public String main() {
        return "admin/main";
    }
    @GetMapping("/logistics")
    public String logistics() {
        return "admin/logistics";
    }
    
}
