package com.dodam.product.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA 설정 클래스
 * 
 * <p>상품 도메인의 JPA 관련 설정을 관리합니다.</p>
 * 
 * @since 1.0.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.dodam.product.repository")
@EnableTransactionManagement
public class JpaConfig {
    
    // JPA 설정은 application.yml에서 관리하므로 여기서는 기본 설정만 활용
}