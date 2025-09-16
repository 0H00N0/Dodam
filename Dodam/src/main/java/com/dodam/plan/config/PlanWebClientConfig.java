package com.dodam.plan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PlanWebClientConfig {

    @Bean
    public WebClient planWebClient(PlanPortoneProperties props) { // ✅ Qualifier 제거
        // 필요 시 메시지 크기 늘리기
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();

        return WebClient.builder()
                .baseUrl(props.getBaseUrl()) // 예: https://api.portone.io
                .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + props.getSecret())
                .exchangeStrategies(strategies)
                .build();
    }
}
