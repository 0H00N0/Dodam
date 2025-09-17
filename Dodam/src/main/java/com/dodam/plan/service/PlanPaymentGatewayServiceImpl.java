// src/main/java/com/dodam/plan/service/PlanPaymentGatewayServiceImpl.java
package com.dodam.plan.service;

import com.dodam.plan.config.PlanPortoneProperties;
import com.dodam.plan.dto.PlanCardMeta;
import com.dodam.plan.dto.PlanLookupResult;
import com.dodam.plan.dto.PlanPayResult;
import com.dodam.plan.dto.PlanPaymentLookupResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PlanPaymentGatewayServiceImpl implements PlanPaymentGatewayService {

    private final WebClient portone;   // PortOne 전용 WebClient (@Qualifier("portoneWebClient"))
    private final ObjectMapper mapper;
    private final PlanPortoneProperties props;

    public PlanPaymentGatewayServiceImpl(
            @Qualifier("portoneWebClient") WebClient portone,
            ObjectMapper mapper,
            PlanPortoneProperties props
    ) {
        this.portone = portone;
        this.mapper = mapper;
        this.props = props;
    }

    private static final ParameterizedTypeReference<Map<String,Object>> MAP =
            new ParameterizedTypeReference<>() {};

    @Override
    public PlanPayResult payByBillingKey(String paymentId, String billingKey, long amount, String customerId) {
        try {
            // ==== 요청 바디 구성 (v2 권장 필드 + 상점/환경 고정) ====
            Map<String, Object> payload = new HashMap<>();
            payload.put("paymentId", paymentId);
            payload.put("billingKey", billingKey);
            payload.put("amount", amount);
            if (StringUtils.hasText(customerId)) {
                payload.put("customerId", customerId);
            }
            if (StringUtils.hasText(props.getStoreId())) {
                payload.put("storeId", props.getStoreId());
            }
            // 선택: 테스트 환경이면 true
            if (props.getIsTest() != null) {
                payload.put("isTest", props.getIsTest());
            }

            // ==== 호출 ====
            String body = portone.post()
                    .uri("/payments/billing-keys/confirm") // ✅ v2 billing-key 결제 승인
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class).defaultIfEmpty("")
                                    .flatMap(err -> {
                                        log.error("[payByBillingKey] {} {}\nreq={}\nres={}",
                                                resp.statusCode(), "/payments/billing-keys/confirm", safeJson(payload), err);
                                        return resp.createException().flatMap(Mono::error);
                                    })
                    )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(20))
                    .block();

            if (!StringUtils.hasText(body)) {
                log.warn("[payByBillingKey] empty body");
                return accepted(paymentId, "{}"); // 모호하면 보류 처리
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

            // ==== 상태 판정 ====
            if ("PENDING".equals(status) || "PROCESSING".equals(status)) {
                return accepted(id, body);
            }
            if ("PAID".equals(status) || "SUCCEEDED".equals(status) || "SUCCESS".equals(status)) {
                return success(id, receiptUrl, body);
            }
            if ("FAILED".equals(status) || "CANCELED".equals(status)) {
                String reason = StringUtils.hasText(failReason) ? failReason : "PAYMENT_" + status;
                return fail(id, reason, receiptUrl, body);
            }
            // 상태 값이 비어있거나 알 수 없는 경우
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
    private String safeJson(Object o) {
        try { return mapper.writeValueAsString(o); } catch (Exception ignored) { return String.valueOf(o); }
    }

    @Override
    public PlanCardMeta extractCardMeta(String rawJson) {
        if (!StringUtils.hasText(rawJson)) return new PlanCardMeta(null, null, null, null, false, null);
        try {
            JsonNode r = mapper.readTree(rawJson);
            String status = r.path("status").asText("").toUpperCase();
            boolean issued = "ISSUED".equals(status);

            String brand = r.path("card").path("brand").asText(null);
            String bin   = r.path("card").path("bin").asText(null);
            String last4 = r.path("card").path("last4").asText(null);
            String pg    = r.path("pgProvider").asText(null); // 응답 키에 맞게

            String customerId = r.path("customer").path("id").asText(null);
            return new PlanCardMeta(brand, bin, last4, pg, issued, customerId);
        } catch (Exception e) {
            log.warn("[extractCardMeta] parse fail: {}", e.toString());
            return new PlanCardMeta(null, null, null, null, false, null);
        }
    }

    @Override
    public PlanLookupResult safeLookup(String paymentId) {
        try {
            String body = portone.get()
                    .uri("/payments/{id}", paymentId) // ✅ v2 조회
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
            return new PlanPaymentLookupResult(r.id(), st, r.rawJson(), HttpStatus.OK);
        } catch (Exception e) {
            return new PlanPaymentLookupResult(paymentId, "ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
