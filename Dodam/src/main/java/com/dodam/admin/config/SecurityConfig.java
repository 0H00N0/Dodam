package com.dodam.admin.config;

import com.dodam.admin.handler.CustomAuthenticationSuccessHandler;
import com.dodam.admin.service.AdminService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정을 담당하는 클래스입니다.
 * 웹 보안을 구성하고, 인증 및 권한 부여 규칙을 정의합니다.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    /**
     * HTTP 보안 필터 체인을 구성합니다.
     * 이 메소드에서 URL별 접근 권한, 폼 로그인 설정, CSRF 보호 등을 정의합니다.
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 객체
     * @throws Exception 보안 설정 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // 특정 URL 패턴에 대한 접근을 허용합니다.
                // 로그인 페이지, 정적 리소스, H2 콘솔, 로그인 처리 URL 등은 인증 없이 접근 가능합니다.
                .requestMatchers("/css/**", "/js/**", "/images/**", "/test", "/h2-console/**", "/admin/login", "/admin/process").permitAll()
                // 그 외 모든 요청은 인증된 사용자만 접근할 수 있도록 설정합니다.
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf
                // CSRF 보호를 설정합니다.
                // H2 콘솔과 로그인 처리 URL에 대해서는 CSRF 보호를 비활성화합니다.
                .ignoringRequestMatchers("/h2-console/**", "/admin/process")
            )
            .headers(headers -> headers
                // X-Frame-Options 헤더를 설정하여 클릭재킹 공격을 방지합니다.
                // H2 콘솔 사용을 위해 sameOrigin으로 설정합니다.
                .frameOptions().sameOrigin() 
            )
            .formLogin(form -> form
            	    .loginPage("/admin/login")       // 커스텀 로그인 페이지의 URL을 지정합니다.
            	    .loginProcessingUrl("/admin/process") // 로그인 폼이 제출될 URL을 지정합니다.
            	    .successHandler(customAuthenticationSuccessHandler) // 로그인 성공 시 호출될 커스텀 핸들러를 지정합니다.
            	    .permitAll() // 로그인 페이지 및 로그인 처리 URL에 대한 접근을 모든 사용자에게 허용합니다.
            	)

            .logout(logout -> logout
                .logoutUrl("/admin/logout") // 로그아웃을 처리할 URL을 지정합니다.
                .logoutSuccessUrl("/admin/login?logout") // 로그아웃 성공 시 리디렉션될 URL을 지정합니다.
                .permitAll() // 로그아웃 관련 URL에 대한 접근을 모든 사용자에게 허용합니다.
            );
            
        return http.build();
    }

    /**
     * 인증 관리자(AuthenticationManager)를 빈으로 등록합니다.
     * Spring Security의 인증 프로세스를 관리합니다.
     * @param config AuthenticationConfiguration 객체
     * @return AuthenticationManager 객체
     * @throws Exception 인증 관리자 생성 중 발생할 수 있는 예외
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    /**
     * 사용자 상세 정보를 로드하는 UserDetailsService를 빈으로 등록합니다.
     * 여기서는 AdminService가 UserDetailsService 인터페이스를 구현하므로 AdminService를 사용합니다.
     * @param adminService AdminService 객체
     * @return UserDetailsService 객체
     */
    @Bean
    public UserDetailsService userDetailsService(AdminService adminService) {
        return adminService; 
    }

    /**
     * 비밀번호 인코더를 빈으로 등록합니다.
     * BCryptPasswordEncoder를 사용하여 비밀번호를 안전하게 해싱합니다.
     * @return PasswordEncoder 객체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
