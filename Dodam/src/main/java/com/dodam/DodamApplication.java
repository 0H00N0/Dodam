package com.dodam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@ComponentScan(
    basePackages = {
        "com.dodam"                 // 전체 스캔하되
    },
    excludeFilters = {
        // ✅ admin 패키지 전부 제외 (컨트롤러/서비스 등)
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.dodam\\.admin\\..*")
    }
)
@EnableJpaRepositories(basePackages = "com.dodam.board.repository")   // ✅ board 레포만
@EntityScan(basePackages = "com.dodam.board.entity")                   // ✅ board 엔티티만
public class DodamApplication {
    public static void main(String[] args) {
        SpringApplication.run(DodamApplication.class, args);
    }
}