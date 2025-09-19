// src/main/java/com/dodam/config/SecurityConfig.java
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

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SessionAuthFilter sessionAuthFilter;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String front;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 기본 strength 10
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(c -> c.configurationSource(corsSource()))
            .headers(h -> h.frameOptions(f -> f.disable())) // H2 콘솔 허용

            // ✅ anyRequest()는 항상 맨 마지막!
            .authorizeHttpRequests(auth -> auth
                // ---- 공개 허용 경로 ----
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/member/**").permitAll()          // 회원가입/로그인 등
                .requestMatchers("/webhooks/pg").permitAll()        // PG 웹훅
                .requestMatchers(HttpMethod.GET, "/pg/payments/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/pg/transactions/**").permitAll()

                // ---- Plan 모듈 ----
                .requestMatchers("/payments/**").permitAll()        // 결제 REST 공개(필요 시 조정)
                .requestMatchers("/billing-keys/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/billing-keys/confirm", "/billing-keys/register").permitAll()
                .requestMatchers(HttpMethod.GET,  "/billing-keys/list").permitAll()
                .requestMatchers("/sub/**").authenticated()
                .requestMatchers("/pg/**").authenticated()          // 위에서 GET만 permitAll 한 경로는 예외

                // ---- 그 외 ----
                .anyRequest().permitAll()
            )
            .httpBasic(b -> b.disable())   // REST: 기본 인증 비활성화
            .formLogin(f -> f.disable());  // 폼 로그인 비활성화

        // 세션 인증 필터 등록 (UsernamePasswordAuthenticationFilter 앞에)
        http.addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(front, "http://127.0.0.1:3000"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
