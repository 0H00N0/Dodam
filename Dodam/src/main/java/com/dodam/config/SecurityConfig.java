package com.dodam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Creates a BCryptPasswordEncoder bean to be used for hashing passwords.
     * The MemberEntity comments indicate that BCrypt is used.
     */
    @Bean  
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures security rules for HTTP requests.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection. For session-based auth, it's generally recommended
            // to enable it, but it's disabled here for simplicity.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Allow all requests to the admin login endpoint
                .requestMatchers("/api/admin/auth/**").permitAll()
                // You would add other rules here, for example:
                // .requestMatchers("/api/admin/dashboard/**").hasAnyRole("ADMIN", "SUPERADMIN")
                // For now, allow all other requests for demonstration purposes
                .anyRequest().permitAll()
            );

        return http.build();
    }
}