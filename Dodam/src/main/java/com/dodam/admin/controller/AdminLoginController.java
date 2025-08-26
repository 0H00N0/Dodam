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

/**
 * 관리자 로그인 및 관련 페이지를 처리하는 컨트롤러 클래스입니다.
 * Spring Security를 통해 인증이 이루어지며, 로그인 성공 후 역할에 따라 다른 페이지로 리디렉션됩니다.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminService adminService;

    /**
     * 관리자 로그인 페이지를 반환합니다.
     * 로그인 실패 또는 로그아웃 시 메시지를 모델에 추가하여 뷰에 전달합니다.
     * @param error 로그인 실패 시 전달되는 파라미터
     * @param logout 로그아웃 시 전달되는 파라미터
     * @param model 뷰에 데이터를 전달하기 위한 Model 객체
     * @return 관리자 로그인 페이지 뷰 이름
     */
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

    
    
    /**
     * 관리자 대시보드 페이지를 반환합니다.
     * 로그인 성공 후 이동할 수 있는 페이지 중 하나입니다.
     * @return 관리자 대시보드 페이지 뷰 이름
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
    
    /**
     * ADMIN 역할 전용 메인 페이지를 반환합니다.
     * @return ADMIN 메인 페이지 뷰 이름
     */
    @GetMapping("/main")
    public String main() {
        return "admin/main";
    }

    /**
     * DELIVERYMAN 역할 전용 로지스틱스 페이지를 반환합니다.
     * @return DELIVERYMAN 로지스틱스 페이지 뷰 이름
     */
    @GetMapping("/logistics")
    public String logistics() {
        return "admin/logistics";
    }
}
