package com.dodam.admin.handler;

import com.dodam.admin.entity.AdminEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        AdminEntity userDetails = (AdminEntity) authentication.getPrincipal();
        AdminEntity.AdminRole role = userDetails.getRole();

        if (role == AdminEntity.AdminRole.ADMIN) {
            response.sendRedirect("/admin/main");
        } else if (role == AdminEntity.AdminRole.DELIVERYMAN) {
            response.sendRedirect("/admin/logistics");
        } else {
            response.sendRedirect("/admin/dashboard");
        }
    }
}
