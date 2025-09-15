package com.dodam.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final SessionAuthFilter sessionAuthFilter;

    @Value("${front.origin:http://localhost:3000}")
    private String front;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS
            .cors(cors -> cors.configurationSource(corsSource()))
            // CSRF: 상태변경 엔드포인트만 예외
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/oauth/**",
                "/member/loginForm",
                "/member/logout",
                "/member/updateProfile",
                "/member/changePw",
                "/member/changePwDirect",
                "/member/signup"
            ))
            // 인가
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html",
                    "/static/**", "/favicon.ico",
                    "/oauth/**",              // 소셜 엔드포인트 허용
                    "/member/signup",         // 회원가입 허용
                    "/member/loginForm"       // 로컬 로그인 허용
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 프리플라이트 허용
                .anyRequest().authenticated()
            )
            // 세션 기반
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            // 세션 인증 주입 필터
            .addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowCredentials(true); // axios withCredentials:true 매칭
        c.setAllowedOrigins(List.of(
            front,                      // ex) http://localhost:3000
            "http://127.0.0.1:3000"
        ));
        c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        // 필요시 노출헤더: c.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
