// src/main/java/com/dodam/plan/service/PlanPaymentGatewayServiceImpl.java
package com.dodam.plan.service;

import com.dodam.plan.dto.PlanCardMeta;
import com.dodam.plan.dto.PlanLookupResult;
import com.dodam.plan.dto.PlanPayResult;
import com.dodam.plan.dto.PlanPaymentLookupResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;          // ✅ 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
public class PlanPaymentGatewayServiceImpl implements PlanPaymentGatewayService {

    private final WebClient portone;   // PortOne 전용 WebClient
    private final ObjectMapper mapper;

    // ✅ 명시 생성자 + Qualifier 로 주입 충돌 해결
    public PlanPaymentGatewayServiceImpl(
            @Qualifier("portoneWebClient") WebClient portone,  // <-- 여기 중요
            ObjectMapper mapper
    ) {
        this.portone = portone;
        this.mapper = mapper;
    }

    @Override
    public PlanPayResult payByBillingKey(String paymentId, String billingKey, long amount, String customerId) {
        try {
            Map<String, Object> payload = Map.of(
                    "paymentId", paymentId,
                    "billingKey", billingKey,
                    "amount", amount,
                    "customerId", customerId
            );

            Mono<ClientResponse> call = portone.post()
                    .uri("/v2/payments/billing-keys/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .exchangeToMono(Mono::just)
                    .timeout(Duration.ofSeconds(15));

            ClientResponse resp = call.block();
            if (resp == null) {
                return fail(paymentId, "NO_RESPONSE", null, "{}");
            }

            HttpStatusCode http = resp.statusCode();
            String body = resp.bodyToMono(String.class).defaultIfEmpty("").block();

            if (!StringUtils.hasText(body)) {
                log.warn("[payByBillingKey] empty body. status={}", http.value());
                return http.is2xxSuccessful()
                        ? accepted(paymentId, body)
                        : fail(paymentId, "EMPTY_RESPONSE", null, "{}");
            }

            JsonNode json;
            try {
                json = mapper.readTree(body);
            } catch (Exception parseEx) {
                log.error("[payByBillingKey] parse error: {}", parseEx.toString());
                return fail(paymentId, "INVALID_JSON", null, body);
            }

            String id = json.path("id").asText(paymentId);
            String status = json.path("status").asText("").toUpperCase();
            String receiptUrl = json.path("receiptUrl").asText(null);
            String failReason = json.path("message").asText(null);

            if (http.value() == 202 || "PENDING".equals(status) || "PROCESSING".equals(status)) {
                return accepted(id, body);
            }
            if ("PAID".equals(status) || "SUCCEEDED".equals(status) || "SUCCESS".equals(status)) {
                return success(id, receiptUrl, body);
            }
            if ("FAILED".equals(status) || "CANCELED".equals(status) || http.is4xxClientError() || http.is5xxServerError()) {
                String reason = StringUtils.hasText(failReason) ? failReason : ("HTTP_" + http.value());
                return fail(id, reason, receiptUrl, body);
            }
            return fail(id, "UNKNOWN_STATUS:" + status, receiptUrl, body);

        } catch (Exception e) {
            log.error("[payByBillingKey] error: {}", e.toString(), e);
            return fail(paymentId, e.getClass().getSimpleName(), null, "{}");
        }
    }

    private PlanPayResult success(String id, String receiptUrl, String raw) {
        return new PlanPayResult(true, id, receiptUrl, null, ensureRaw(raw));
    }
    private PlanPayResult fail(String id, String reason, String receiptUrl, String raw) {
        return new PlanPayResult(false, id, receiptUrl, reason, ensureRaw(raw));
    }
    private PlanPayResult accepted(String id, String raw) {
        return new PlanPayResult(false, id, null, "ACCEPTED", ensureRaw(raw));
    }
    private String ensureRaw(String raw) { return StringUtils.hasText(raw) ? raw : "{}"; }

    @Override
    public PlanCardMeta extractCardMeta(String rawJson) {
        if (!StringUtils.hasText(rawJson)) return new PlanCardMeta(null, null, null, null);
        try {
            JsonNode r = mapper.readTree(rawJson);
            return new PlanCardMeta(
                    r.path("card").path("bin").asText(null),
                    r.path("card").path("brand").asText(null),
                    r.path("card").path("last4").asText(null),
                    r.path("pg").asText(null)
            );
        } catch (Exception e) {
            log.warn("[extractCardMeta] parse fail: {}", e.toString());
            return new PlanCardMeta(null, null, null, null);
        }
    }

    @Override
    public PlanLookupResult safeLookup(String paymentId) {
        try {
            String body = portone.get()
                    .uri("/v2/payments/{id}", paymentId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorReturn("")
                    .block();

            if (!StringUtils.hasText(body)) {
                return new PlanLookupResult(paymentId, "UNKNOWN", "{}");
            }
            JsonNode j = mapper.readTree(body);
            String status = j.path("status").asText("UNKNOWN");
            return new PlanLookupResult(paymentId, status, body);
        } catch (Exception e) {
            return new PlanLookupResult(paymentId, "ERROR", e.getMessage());
        }
    }

    @Override
    public PlanPaymentLookupResult lookupPayment(String paymentId) {
        try {
            PlanLookupResult r = safeLookup(paymentId);
            String st = (r.status() == null ? "UNKNOWN" : r.status().toUpperCase());

            return new PlanPaymentLookupResult(
                    r.id(),
                    st,
                    r.rawJson(),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            return new PlanPaymentLookupResult(
                    paymentId,
                    "ERROR",
                    e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
