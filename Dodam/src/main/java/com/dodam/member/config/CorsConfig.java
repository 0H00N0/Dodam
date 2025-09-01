package com.dodam.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ 프론트 개발 주소들 추가
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://192.168.219.224:3000"   // 로컬 IP에서 React 실행할 때
        ));

        // ✅ 쿠키(세션) 허용
        config.setAllowCredentials(true);

        // ✅ 메서드/헤더 허용
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));

        // ✅ 응답에 노출할 헤더(필요 시)
        config.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 엔드포인트에 CORS 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
