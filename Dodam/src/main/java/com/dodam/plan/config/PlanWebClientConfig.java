// src/main/java/com/dodam/plan/config/WebClientConfig.java
package com.dodam.plan.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class PlanWebClientConfig {

    private final PlanPortoneProperties portone;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(portone.getBaseUrl() != null ? portone.getBaseUrl() : "https://api.portone.io")
                .build();
    }
}

