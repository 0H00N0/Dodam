package com.dodam.plan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "portone")
public class PlanPortoneProperties {
    private String apiKey;
    private String secret;
    private String baseUrl;
    private String storeId; // ⭐ v2에서 반드시 필요
}
