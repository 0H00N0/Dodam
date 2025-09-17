// src/main/java/com/dodam/plan/config/PortoneConfig.java
package com.dodam.plan.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
				.defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + props.getV2Secret())
				.baseUrl(props.getBaseUrl() != null && !props.getBaseUrl().isBlank() ? props.getBaseUrl()
						: "https://api.portone.io")
				.filter((request, next) -> {
					// 민감정보 노출 금지: 길이와 prefix만 출력
					String auth = request.headers().getFirst(HttpHeaders.AUTHORIZATION);
					String marker = (auth != null && auth.startsWith("PortOne ")) ? "SET" : "MISSING";
					log.info("[PortOne] {} {}  Authorization={}", request.method(), request.url(), marker);
					return next.exchange(request);
				}).build();
	}

	@PostConstruct
	public void validate() {
		if (props.getV2Secret() == null || props.getV2Secret().isBlank()) {
			throw new IllegalStateException("portone.v2Secret is not set");
		}
	}
}
