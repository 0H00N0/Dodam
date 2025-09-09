package com.dodam.board;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * 도담 Boards API 애플리케이션.
 * H2 메모리 DB + Spring Data JPA + Springdoc(OpenAPI).
 * Swagger UI: /swagger-ui.html
 */

@EnableJpaRepositories(basePackages = "com.dodam.board.repository")
@EntityScan(basePackages = "com.dodam.board.entity")
public class BoardApplication {
    public static void main(String[] args) {
        SpringApplication.run(BoardApplication.class, args);
    }
}