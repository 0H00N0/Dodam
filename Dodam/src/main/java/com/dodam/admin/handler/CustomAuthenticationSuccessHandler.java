package com.dodam.admin.handler;

import com.dodam.admin.entity.AdminEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Spring Security 로그인 성공 후 처리를 담당하는 커스텀 핸들러입니다.
 * 사용자의 역할에 따라 다른 URL로 리디렉션합니다.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 인증 성공 시 호출되는 메소드입니다.
     * 인증된 사용자의 역할을 확인하고, 해당 역할에 맞는 페이지로 리디렉션합니다.
     * @param request HttpServletRequest 객체
     * @param response HttpServletResponse 객체
     * @param authentication 인증된 사용자 정보를 담고 있는 Authentication 객체
     * @throws IOException 리디렉션 중 발생할 수 있는 입출력 예외
     * @throws ServletException 서블릿 관련 예외
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        // 인증된 사용자 정보에서 AdminEntity 객체를 가져옵니다.
        AdminEntity userDetails = (AdminEntity) authentication.getPrincipal();
        // 사용자의 역할을 가져옵니다.
        AdminEntity.AdminRole role = userDetails.getRole();

        // 역할에 따라 다른 페이지로 리디렉션합니다.
        if (role == AdminEntity.AdminRole.ADMIN) {
            response.sendRedirect("/admin/main");
        } else if (role == AdminEntity.AdminRole.DELIVERYMAN) {
            response.sendRedirect("/admin/logistics");
        } else {
            // 정의되지 않은 역할의 경우 기본 대시보드 페이지로 리디렉션합니다.
            response.sendRedirect("/admin/dashboard");
        }
    }
}
