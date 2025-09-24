// src/main/java/com/dodam/config/SessionAuthFilter.java
package com.dodam.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.List;

@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PM = new AntPathMatcher();

    // SecurityConfig의 permitAll 경로와 맞춰주세요
    private static final String[] PUBLIC_ENDPOINTS = {
        "/h2-console/**",
        "/webhooks/pg",
        "/member/**",
        "/subscriptions/**",    // 👈 구독 API (start/confirm 등)
        "/payments/**",
        "/billing-keys/**",
        "/pg/payments/**",
        "/pg/transactions/**"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 1) CORS Preflight는 항상 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        // 2) 공개 경로는 필터 건너뜀
        String path = request.getRequestURI();
        for (String pattern : PUBLIC_ENDPOINTS) {
            if (PM.match(pattern, path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                Object sid = session.getAttribute("sid");
                if (sid != null) {
                    var auth = new UsernamePasswordAuthenticationToken(
                        sid, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(req, res);
    }
}
