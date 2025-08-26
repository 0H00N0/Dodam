    package com.dodam.admin.handler;
    
    import org.springframework.security.core.Authentication;
    import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.stereotype.Component;
    
    import javax.servlet.ServletException;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import java.io.IOException;
    
    @Component
    public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, 
                                            HttpServletResponse response, 
                                            Authentication authentication) throws IOException, ServletException {
            
            // 🟢 사용자의 권한을 확인하여 리다이렉트할 URL을 결정합니다.
            // 권한은 UserDetailsService에서 설정한 'ROLE_ADMIN' 또는 'ROLE_REPORTER'가 될 것입니다.
            String redirectUrl = "/admin/dashboard"; // 기본값
            
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if (auth.getAuthority().equals("ROLE_ADMIN")) {
                    redirectUrl = "/admin/main";
                    break;
                } else if (auth.getAuthority().equals("ROLE_REPORTER")) {
                    redirectUrl = "/admin/logistics";
                    break;
                }
            }
            
            response.sendRedirect(redirectUrl);
        }
    }
    