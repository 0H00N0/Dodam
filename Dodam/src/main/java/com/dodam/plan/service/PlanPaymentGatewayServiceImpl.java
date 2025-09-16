// src/main/java/com/dodam/plan/service/PlanPaymentGatewayServiceImpl.java
package com.dodam.plan.service;

import com.dodam.plan.config.PlanPortoneProperties;
import com.dodam.plan.dto.PlanCardMeta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class PlanPaymentGatewayServiceImpl implements PlanPaymentGatewayService {

    private final PlanPortoneProperties portone;
    private final WebClient planWebClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PayResult payByBillingKey(String billingKey, long amount, String customerId) {
        return payByBillingKey("pay_" + System.currentTimeMillis(), billingKey, amount, customerId);
    }

    @Override
    public PayResult payByBillingKey(String paymentId, String billingKey, long amount, String customerId) {
        return payByBillingKey(paymentId, customerId, billingKey, amount);
    }

    @Override
    public PayResult payByBillingKey(String paymentId, String customerId, String billingKey, long amount) {
        try {
            var body = """
                {
                  "storeId":"%s",
                  "channelKey":"%s",
                  "paymentId":"%s",
                  "billingKey":"%s",
                  "customerId":"%s",
                  "amount":%d,
                  "currency":"%s"
                }
            """.formatted(
                    portone.getStoreId(),
                    portone.getChannelKey(),
                    paymentId,
                    billingKey,
                    customerId,
                    amount,
                    portone.getCurrency()
            );

            var res = planWebClient.post()
                    .uri("/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            final String raw = (res == null) ? "" : res;
            final JsonNode json = raw.isBlank() ? mapper.createObjectNode() : mapper.readTree(raw);
            final boolean success = "PAID".equalsIgnoreCase(json.path("status").asText());

            return new PayResult() {
                @Override public boolean success() { return success; }
                @Override public String paymentId() { return json.path("id").asText(paymentId); }
                @Override public String receiptUrl() { return json.path("receiptUrl").asText(""); }
                @Override public String failReason() { return success ? null : json.path("message").asText(""); }
                @Override public String rawJson() { return raw; }
            };

        } catch (Exception e) {
            final String errMsg = e.getMessage();
            return new PayResult() {
                @Override public boolean success() { return false; }
                @Override public String paymentId() { return paymentId; }
                @Override public String receiptUrl() { return null; }
                @Override public String failReason() { return errMsg; }
                @Override public String rawJson() { return null; }
            };
        }
    }

    @Override
    public PlanCardMeta extractCardMeta(String rawJson) {
        try {
            JsonNode json = (rawJson == null || rawJson.isBlank())
                    ? mapper.createObjectNode()
                    : mapper.readTree(rawJson);

            // PG 응답에 보통 /customerId 가 포함됩니다.
            String brand      = json.at("/card/brand").asText("");
            String bin        = json.at("/card/bin").asText("");
            String last4      = json.at("/card/last4").asText("");
            String issuerName = json.at("/card/issuerName").asText("");
            String customerId = json.at("/customerId").asText("");   // ✅ 추가 파싱

            // 5-인자 생성자 사용 (customerId 포함). 이전 코드가 4-인자 호출해도 호환됨.
            return new PlanCardMeta(brand, bin, last4, issuerName, customerId);

        } catch (Exception e) {
            // 파싱 실패 시 빈 메타 반환 (NPE 방지)
            return new PlanCardMeta("", "", "", "", "");
        }
    }

    @Override
    public LookupResult safeLookup(String paymentId) {
        try {
            var res = planWebClient.get()
                    .uri("/payments/" + paymentId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            final String raw = (res == null) ? "" : res;
            final JsonNode json = raw.isBlank() ? mapper.createObjectNode() : mapper.readTree(raw);

            final String id = json.path("id").asText(paymentId);
            final String status = json.path("status").asText("UNKNOWN");

            return new LookupResult() {
                @Override public String paymentId() { return id; }
                @Override public String status() { return status; }
                @Override public String rawJson() { return raw; }
            };
        } catch (Exception e) {
            final String err = e.getMessage();
            return new LookupResult() {
                @Override public String paymentId() { return paymentId; }
                @Override public String status() { return "ERROR"; }
                @Override public String rawJson() { return err; }
            };
        }
    }
}
