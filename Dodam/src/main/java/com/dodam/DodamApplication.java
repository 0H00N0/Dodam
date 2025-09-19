package com.dodam;

import com.dodam.plan.config.PlanPortoneProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PlanPortoneProperties.class) // ✅ properties 빈 등록
public class DodamApplication {
    public static void main(String[] args) {
        SpringApplication.run(DodamApplication.class, args);
    }
}
