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
<<<<<<< HEAD
            .csrf(csrf -> csrf.disable())
            .cors(c -> c.configurationSource(corsSource()))
            .headers(h -> h.frameOptions(f -> f.disable())) // H2 콘솔 허용
            .authorizeHttpRequests(auth -> auth
                // ---- 공개 허용 경로 ----
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/member/**").permitAll()      // 회원가입/로그인 등 공개
                .requestMatchers("/webhooks/pg").permitAll()    // PG 웹훅은 외부 호출 허용
                .requestMatchers(HttpMethod.GET, "/pg/payments/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/pg/transactions/**").permitAll()

                // ---- Plan 모듈: 로그인 필요 ----
                .requestMatchers("/billing-keys/**").authenticated()
                .requestMatchers("/payments/**").authenticated()
                .requestMatchers("/sub/**").authenticated()
                .requestMatchers("/pg/**").authenticated() // 단, 위에서 명시적으로 permitAll 한 GET 경로는 제외됨

                // ---- 그 외 ----
                .anyRequest().permitAll()
=======
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/oauth/**",          // ✅ 소셜 엔드포인트 CSRF 예외
                    "/member/**"          // (필요 시 유지)
                )
>>>>>>> branch 'chan787' of https://github.com/0H00N0/Dodam.git
            )
<<<<<<< HEAD
            .httpBasic(b -> b.disable())  // REST 방식 → 기본 인증 비활성화
            .formLogin(f -> f.disable());
=======
            .cors(cors -> cors.configurationSource(corsSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/index.html",
                    "/oauth/**",          // ✅ 소셜 엔드포인트 허용
                    "/member/signup",     // 회원가입
                    "/member/loginForm",  // 로컬 로그인
                    "/static/**", "/favicon.ico"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class);
>>>>>>> branch 'chan787' of https://github.com/0H00N0/Dodam.git

<<<<<<< HEAD
        // 세션 인증 필터 등록
        http.addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class);

=======
>>>>>>> branch 'chan787' of https://github.com/0H00N0/Dodam.git
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

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}