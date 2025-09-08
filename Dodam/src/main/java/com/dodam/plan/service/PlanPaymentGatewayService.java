package com.dodam.plan.service;

import com.dodam.plan.config.PlanPortoneProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanPaymentGatewayService {

    private final PlanPortoneProperties portone;
    private final WebClient portoneWebClient;      // ✅ WebClientConfig에서 주입
    private final ObjectMapper mapper = new ObjectMapper(); // ✅ 간단히 직접 생성 (주입해도 OK)

    // PortOne 결제 응답 DTO
    public record PayResult(
            boolean success,
            String uid,
            String receiptUrl,
            String rawJson,
            String failReason
    ) {}

    /**
     * v2 API - 빌링키 결제
     * @param merchantUid  내부 주문번호(멱등 키)
     * @param customerId   PortOne customerId
     * @param billingKey   PortOne 발급 billingKey
     * @param amount       결제금액
     */
    public PayResult payWithBillingKey(
            String merchantUid,
            String customerId,
            String billingKey,
            BigDecimal amount
    ) {
        try {
            // ✅ 요청 바디 구성 (v2)
            Map<String, Object> body = new HashMap<>();
            body.put("storeId", portone.getStoreId());              // 필수
            body.put("merchantUid", merchantUid);
            body.put("billingKey", billingKey);
            body.put("customerId", customerId);
            body.put("amount", Map.of("currency", "KRW", "value", amount));

            String resJson = portoneWebClient.post()
                    .uri("/billing-keys/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = mapper.readTree(resJson == null ? "{}" : resJson);
            JsonNode data = root.hasNonNull("data") ? root.get("data") : root; // 계정에 따라 래핑/미래핑 모두 방어

            // ✅ 성공 판단: status, paymentId 등 안전하게 체크
            String status = text(data, "status");           // 예: PAID / FAILED 등
            String paymentId = text(data, "paymentId");     // v2 고유 결제 ID
            String receiptUrl = text(data.path("receipt"), "url");
            if (receiptUrl == null) receiptUrl = text(data, "receiptUrl"); // 다른 계정 응답 포맷 방어

            boolean ok = "PAID".equalsIgnoreCase(status) || (paymentId != null && !"FAILED".equalsIgnoreCase(status));

            if (ok) {
                return new PayResult(true, paymentId, receiptUrl, root.toString(), null);
            } else {
                String msg = text(root, "message");
                if (msg == null) msg = text(data, "message");
                if (msg == null) msg = status != null ? status : "PG_ERROR";
                return new PayResult(false, null, null, root.toString(), msg);
            }

        } catch (Exception e) {
            return new PayResult(false, null, null, null, e.getMessage());
        }
    }

    // ===== 조회 =====

    public static record PaymentLookupRes(
            String paymentId,
            String status,
            String billingKey,
            String customerId,
            String issuerName,
            String bin,
            String last4,
            String rawJson
    ) { }

    public PaymentLookupRes getPayment(String paymentId) {
        try {
            String json = portoneWebClient.get()
                    .uri("/payments/{pid}", paymentId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = mapper.readTree(json == null ? "{}" : json);
            JsonNode pay  = root.hasNonNull("data") ? root.get("data") : root;

            JsonNode billing = pay.path("billing");
            JsonNode card    = pay.path("card");

            return new PaymentLookupRes(
                    text(pay, "id"),                                 // 혹은 paymentId
                    text(pay, "status"),
                    text(billing, "billingKey"),
                    text(pay.path("customer"), "id"),
                    text(card, "issuerName"),
                    text(card, "bin"),
                    text(card, "lastFourDigits"),
                    root.toString()
            );
        } catch (Exception e) {
            return new PaymentLookupRes(null, null, null, null, null, null, null,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String text(JsonNode n, String field) {
        if (n == null) return null;
        JsonNode v = n.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }
}
