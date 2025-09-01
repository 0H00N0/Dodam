package com.dodam.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource) {
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 활성화 (위 Bean 사용)
            .cors(c -> c.configurationSource(corsConfigurationSource))
            // 개발 단계에서 CSRF 비활성화 (세션+SPA 조합)
            .csrf(cs -> cs.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/member/login",
                    "/member/signup",
                    "/member/session",
                    "/member/logout"
                ).permitAll()
                .anyRequest().authenticated()
            )
            // 폼로그인/세션만 쓸 경우 기본값으로 충분 (또는 .formLogin().disable())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
