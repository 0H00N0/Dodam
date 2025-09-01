package com.dodam.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(a -> a
              .requestMatchers("/", "/member/**", "/h2-console/**",
                               "/css/**","/js/**","/images/**").permitAll()
              .anyRequest().permitAll()
          )
          .headers(h -> h.frameOptions(f -> f.disable())) // H2 콘솔
          .formLogin(f -> f.disable())                    // sbb 스타일 컨트롤러 사용
          .logout(Customizer.withDefaults());
        return http.build();
    }
}
