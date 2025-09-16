package com.dodam.plan.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "portone")
public class PlanPortoneProperties {
    private String apiKey;
    private String secret;
    private String baseUrl;
    private String storeId;
    private String channelKey;
    private String currency;
}
