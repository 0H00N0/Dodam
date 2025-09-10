package com.dodam.plan.service;

import com.dodam.plan.config.PlanPortoneProperties;
import com.dodam.plan.dto.PlanPortoneBillingKeyDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanPaymentGatewayService {

    private final PlanPortoneProperties portone;
    private final WebClient webClient = WebClient.builder().build();

    /**
     * ✅ 카드 등록 → BillingKey 발급
     */
    public Mono<PlanPortoneBillingKeyDTO> registerBillingKey(Map<String, Object> req) {
        return webClient.post()
            .uri("https://api.portone.io/v2/billing-keys")
            .header("Authorization", "PortOne " + portone.getApiKey() + ":" + portone.getSecret())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class).flatMap(body ->
                    Mono.error(new RuntimeException("PORTONE_ERROR " + resp.statusCode() + " " + body))
                )
            )
            .bodyToMono(PlanPortoneBillingKeyDTO.class);
    }

    /**
     * ✅ 결제 승인 (paymentId + amount)
     */
    public Mono<JsonNode> confirmPayment(String paymentId, Long amount) {
        return webClient.post()
            .uri("https://api.portone.io/v2/payments/" + paymentId + "/confirm")
            .header("Authorization", "PortOne " + portone.getApiKey() + ":" + portone.getSecret())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("amount", amount))
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp ->
                resp.bodyToMono(String.class).flatMap(body ->
                    Mono.error(new RuntimeException("PORTONE_ERROR " + resp.statusCode() + " " + body))
                )
            )
            .bodyToMono(JsonNode.class);
    }
}
