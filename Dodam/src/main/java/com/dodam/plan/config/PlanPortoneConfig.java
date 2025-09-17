// src/main/java/com/dodam/plan/config/PortoneConfig.java
package com.dodam.plan.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(PlanPortoneProperties.class)
public class PlanPortoneConfig {

	private final PlanPortoneProperties props;

	@Bean
	@Qualifier("portoneWebClient")
	public WebClient portoneWebClient(PlanPortoneProperties props) {
		return WebClient.builder().defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + props.getV2Secret().trim())
				.baseUrl((props.getBaseUrl() != null && !props.getBaseUrl().isBlank()) ? props.getBaseUrl()
						: "https://api.portone.io")
				.filter((request, next) -> {
					String h = request.headers().getFirst(HttpHeaders.AUTHORIZATION);
					String masked = (h != null && h.startsWith("PortOne "))
							? ("PortOne " + h.substring(8, Math.min(h.length(), 14)) + "****")
							: "MISSING";
					log.info("[PortOne] {} {}  Authorization={}", request.method(), request.url(), masked);
					return next.exchange(request);
				}).build();
	}

	@PostConstruct
	public void validate() {
		if (props.getV2Secret() == null || props.getV2Secret().isBlank())
			throw new IllegalStateException("portone.v2Secret is not set");
		log.info("[PortOne] baseUrl={}, storeId={}, isTest={}",
				props.getBaseUrl() == null ? "https://api.portone.io" : props.getBaseUrl(), props.getStoreId(),
				props.getIsTest());
	}
	
	@PostConstruct
	public void validateSecret() {
	    String apiSecret = props.getV2Secret();
	    if (apiSecret == null || apiSecret.isBlank()) {
	        throw new IllegalStateException("portone.v2Secret is not set");
	    }

	    var client = WebClient.create(
	        props.getBaseUrl() == null || props.getBaseUrl().isBlank() ? "https://api.portone.io" : props.getBaseUrl()
	    );

	    try {
	        var res = client.post()
	            .uri("/login/api-secret")
	            .contentType(MediaType.APPLICATION_JSON)
	            .bodyValue(Map.of("apiSecret", apiSecret.trim()))
	            .exchangeToMono(resp -> resp.bodyToMono(String.class)
	                .defaultIfEmpty("")
	                .map(body -> Map.entry(resp.statusCode().value(), body))
	            )
	            .block(Duration.ofSeconds(10));

	        int status = res.getKey();
	        String body = res.getValue();

	        String masked = apiSecret.length() > 10 ? apiSecret.substring(0,6) + "****" : "****";
	        log.info("[PortOne] Secret test status={} body={} (v2Secret={})", status, body, masked);

	        if (status != 200) {
	            throw new IllegalStateException("❌ PortOne V2 API Secret 유효성 검사 실패! properties 확인 필요");
	        }
	    } catch (Exception e) {
	        throw new IllegalStateException("❌ PortOne V2 API Secret 유효성 검사 호출 중 예외: " + e.getMessage(), e);
	    }
	}
}
