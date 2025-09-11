package com.dodam.plan.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "portone")
public class PlanPortoneProperties {
    private String apiKey;
    private String secret;
    private String baseUrl;
    private String storeId;
}
