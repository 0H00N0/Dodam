package com.dodam.plan.service;

import com.dodam.plan.config.PlanPortoneProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlanPaymentGatewayService {

    private final PlanPortoneProperties portone;

    private final WebClient webClient = WebClient.builder().build();

    // PortOne 결제 응답 DTO
    public record PayResult(
            boolean success,
            String uid,
            String receiptUrl,
            String rawJson,
            String failReason
    ) {}

    /**
     * v2 API - 빌링키를 이용한 결제
     * @param merchantUid  내부 주문번호
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
            var body = Map.of(
                    "storeId", portone.getStoreId(),   // ⭐ v2에 반드시 storeId 포함
                    "merchantUid", merchantUid,
                    "amount", Map.of(
                            "currency", "KRW",
                            "value", amount
                    ),
                    "billingKey", billingKey,
                    "customerId", customerId
            );

            var res = webClient.post()
                    .uri(portone.getBaseUrl() + "/billing-keys/payments") // v2 endpoint
                    .headers(h -> {
                        h.setBasicAuth(portone.getApiKey(), portone.getSecret()); // v2 인증 방식
                        h.setContentType(MediaType.APPLICATION_JSON);
                    })
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String raw = res != null ? res.toString() : "{}";

            if (res != null && "SUCCESS".equals(res.get("status"))) {
                return new PayResult(
                        true,
                        (String) res.get("paymentId"),        // v2 응답의 고유 결제 ID
                        (String) res.get("receiptUrl"),       // 영수증 URL
                        raw,
                        null
                );
            } else {
                return new PayResult(false, null, null, raw,
                        res != null ? (String) res.get("message") : "PG_ERROR");
            }
        } catch (Exception e) {
            return new PayResult(false, null, null, null, e.getMessage());
        }
    }
}
