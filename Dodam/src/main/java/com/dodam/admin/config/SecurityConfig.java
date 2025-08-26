package com.dodam.admin.config;

import com.dodam.admin.handler.CustomAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 로그인, H2 콘솔, 테스트 URL 등은 인증 없이 접근 허용
                .requestMatchers("/css/**", "/js/**", "/images/**", "/test", "/h2-console/**", "/admin/login", "/admin/process").permitAll()
                // 그 외 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                // H2 콘솔과 수동 로그인 프로세스에 대한 CSRF 보호 비활성화
                .ignoringRequestMatchers("/h2-console/**", "/admin/process")
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin() // H2 콘솔을 위한 설정
            )
            .formLogin(form -> form
            	    .loginPage("/admin/login")       // 커스텀 로그인 페이지
            	    .loginProcessingUrl("/admin/process") // 로그인 처리 URL
            	    .successHandler(customAuthenticationSuccessHandler)
            	    .permitAll()
            	)

            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .permitAll()
            );
            
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
