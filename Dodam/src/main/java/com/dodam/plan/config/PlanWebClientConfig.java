// src/main/java/com/dodam/plan/config/PlanWebClientConfig.java
package com.dodam.plan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PlanWebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
