package com.dodam.admin.config;

// 개발 단계에서 보안을 완전히 비활성화한 상태입니다.
// 운영 환경에서는 주석을 해제하고 보안을 활성화해야 합니다.

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 설정을 담당하는 클래스입니다.
 * 현재는 개발 단계로 모든 보안이 완전히 비활성화되어 있습니다.
 * 운영 환경에서는 주석 처리된 코드를 활성화해야 합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 개발 단계용: 모든 보안을 완전히 비활성화하는 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()  // 모든 요청 허용
            )
            .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화
            .formLogin(form -> form.disable())  // 폼 로그인 비활성화
            .httpBasic(basic -> basic.disable())  // HTTP Basic 인증 비활성화
            .logout(logout -> logout.disable())  // 로그아웃 비활성화
            .sessionManagement(session -> session.disable());  // 세션 관리 비활성화
        
        return http.build();
    }

    /**
     * 비밀번호 인코더를 빈으로 등록합니다.
     * AdminService 등에서 필요로 하므로 개발 단계에서도 유지합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /* 
    ==================== 운영환경용 보안 설정 (현재 주석처리) ====================
    
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/admin/login", "/admin/process", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/process")
                .successHandler(customAuthenticationSuccessHandler)
            );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminService adminService) {
        return adminService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    ==================== 운영환경용 보안 설정 끝 ====================
    */
}