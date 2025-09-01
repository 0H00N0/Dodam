package com.dodam.member.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	 @Bean
	  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	      .csrf(csrf -> csrf.disable()) // REST API면 보통 비활성(또는 CSRF 토큰 사용)
	      .cors(cors -> {})             // 아래 CORS 설정 Bean 사용
	      .authorizeHttpRequests(auth -> auth
	    		  .requestMatchers(
	    				  "/member/api/login", "/member/api/logout",
	    				  "/member/api/signup", "/member/api/session"
	    				).permitAll()
	          .anyRequest().authenticated()
	      );
	    return http.build();
	  }

	  @Bean
	  public CorsConfigurationSource corsConfigurationSource() {
	    var config = new CorsConfiguration();
	    config.setAllowedOrigins(List.of("http://localhost:3000"));
	    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
	    config.setAllowedHeaders(List.of("*"));
	    config.setAllowCredentials(true); // 세션 쿠키 허용

	    var source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);
	    return source;
	  }
}
