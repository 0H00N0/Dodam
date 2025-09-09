// src/main/java/com/dodam/plan/config/WebClientConfig.java
package com.dodam.plan.config;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class PlanWebClientConfig {

    private final PlanPortoneProperties portone;

    @Bean
    @Qualifier("portoneWebClient")
    public WebClient portoneWebClient() {
        return WebClient.builder()
                .baseUrl(portone.getBaseUrl())                 // https://api.portone.io
                .defaultHeaders(h -> h.setBasicAuth(
                        portone.getApiKey(), portone.getSecret() // v2 인증
                ))
                .build();
    }
}
