package com.dodam.plan.service;

import com.dodam.plan.config.PlanPortoneProperties;
import com.dodam.plan.dto.PlanCardMeta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanPaymentGatewayServiceImpl implements PlanPaymentGatewayService {

    private final PlanPortoneProperties portone;
    private final ObjectMapper mapper = new ObjectMapper();

    private WebClient client() {
        String base = (portone.getBaseUrl() == null || portone.getBaseUrl().isBlank())
                ? "https://api.portone.io" : portone.getBaseUrl();
        return WebClient.builder()
                .baseUrl(base)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + portone.getSecret())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ========================
    // Payments: approve by billing key
    // ========================
    @Override
    public PayResult payByBillingKey(String billingKey, long amount, String customerId) {
        try {
            String orderId = makeOrderId();
            Map<String, Object> body = new HashMap<>();
            body.put("orderId", orderId);
            body.put("amount", Map.of("total", amount, "currency", "KRW"));
            body.put("paymentMethod", Map.of("type", "CARD", "billingKey", billingKey));
            body.put("customer", Map.of("id", customerId));
            if (notBlank(portone.getStoreId())) body.put("storeId", portone.getStoreId());

            String confirmPath = notBlank(portone.getConfirmPath()) ? portone.getConfirmPath() : "/payments/confirm";

            String raw = client().post().uri(confirmPath)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return buildPayResultFromRaw(raw);

        } catch (WebClientResponseException ex) {
            return fail("HTTP_" + ex.getRawStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("payByBillingKey error", ex);
            return fail(ex.getClass().getSimpleName() + ": " + ex.getMessage(), "{}");
        }
    }

    // ========================
    // (Compat) confirm by paymentId + amount
    // ========================
    @Override
    public PayResult confirmPaymentRaw(String paymentId, long amount) {
        try {
            // 일부 레거시 플로우는 /payments/{paymentId}/confirm 를 사용하기도 하지만,
            // v2 공통 confirm를 orderId=paymentId 로 사용하는 형태로 구현 (환경에 따라 properties로 분기 가능)
            Map<String, Object> body = new HashMap<>();
            body.put("orderId", paymentId);
            body.put("amount", Map.of("total", amount, "currency", "KRW"));
            if (notBlank(portone.getStoreId())) body.put("storeId", portone.getStoreId());

            String confirmPath = notBlank(portone.getConfirmPath()) ? portone.getConfirmPath() : "/payments/confirm";

            String raw = client().post().uri(confirmPath)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return buildPayResultFromRaw(raw);

        } catch (WebClientResponseException ex) {
            return fail("HTTP_" + ex.getRawStatusCode(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("confirmPaymentRaw error", ex);
            return fail(ex.getClass().getSimpleName() + ": " + ex.getMessage(), "{}");
        }
    }

    private PayResult buildPayResultFromRaw(String raw) throws Exception {
        if (raw == null) return fail("EMPTY_RESPONSE", "{}");
        JsonNode root = mapper.readTree(raw);
        String pid = pickText(root, "id", "paymentId");
        String status = pickText(root, "status");
        String receipt = pickText(root, "receipt", "url");

        boolean ok = "PAID".equalsIgnoreCase(status) ||
                     "SUCCEEDED".equalsIgnoreCase(status) ||
                     "SUCCESS".equalsIgnoreCase(status);

        final String fPid = pid, fReceipt = receipt, fStatus = status, fRaw = raw;
        return new PayResult() {
            @Override public boolean success() { return ok; }
            @Override public String paymentId() { return fPid; }
            @Override public String receiptUrl() { return fReceipt; }
            @Override public String failReason() { return ok ? null : statusOrReason(root, fStatus); }
            @Override public String rawJson() { return fRaw; }
        };
    }

    // ========================
    // (Compat) Lookup
    // ========================
    @Override
    public PgLookupResult safeLookup(String txId, String paymentId) {
        try {
            String getPath = Optional.ofNullable(portone.getPaymentGetPrefix()).orElse("/payments/") + paymentId;

            String raw = client().get().uri(getPath)
                    .retrieve().bodyToMono(String.class).block();

            if (raw == null) raw = "{}";
            JsonNode root = mapper.readTree(raw);

            String pgName   = coalesce(
                    pickText(root, "pgProvider"),
                    pickText(root, "pg"),
                    portone.getPgName()
            );
            // 카드 정보 다양한 위치 대응
            JsonNode card = firstNonNull(
                    pickNode(root, "method", "card"),
                    pickNode(root, "paymentMethod", "card"),
                    pickNode(root, "card")
            );
            String brand = coalesce(
                    pickText(card, "brand"), pickText(card, "issuer", "name"),
                    pickText(root, "card", "issuer"), pickText(root, "card", "brand")
            );
            String bin   = coalesce(
                    pickText(card, "number", "bin"), pickText(card, "bin"),
                    pickText(root, "card", "bin")
            );
            String last4 = coalesce(
                    pickText(card, "number", "last4"), pickText(card, "last4"),
                    pickText(root, "card", "last4")
            );

            String bKey = coalesce(
                    pickText(root, "paymentMethod", "billingKey"),
                    pickText(root, "billingKey"),
                    pickText(card, "billingKey")
            );

            final String fRaw = raw;
            final String fPg  = safe(pgName);
            final String fBr  = safe(brand);
            final String fBin = safe(bin);
            final String fL4  = safe(last4);
            final String fKey = safe(bKey);
            final String fPid = safe(pickText(root, "id", "paymentId", "uid", "orderId"));

            return new PgLookupResult() {
                @Override public String txId() { return txId; }
                @Override public String paymentId() { return fPid; }
                @Override public String pg() { return fPg; }
                @Override public String brand() { return fBr; }
                @Override public String bin() { return fBin; }
                @Override public String last4() { return fL4; }
                @Override public String billingKey() { return fKey; }
                @Override public String rawJson() { return fRaw; }
            };

        } catch (WebClientResponseException ex) {
            String body = ex.getResponseBodyAsString();
            log.warn("safeLookup http error {} {}", ex.getRawStatusCode(), body);
            return lookupFail(txId, paymentId, body);
        } catch (Exception ex) {
            log.warn("safeLookup error {}", ex.toString());
            return lookupFail(txId, paymentId, "{}");
        }
    }

    private PgLookupResult lookupFail(String txId, String paymentId, String raw) {
        final String fRaw = raw == null ? "{}" : raw;
        return new PgLookupResult() {
            @Override public String txId() { return txId; }
            @Override public String paymentId() { return paymentId; }
            @Override public String pg() { return safe(portone.getPgName()); }
            @Override public String brand() { return ""; }
            @Override public String bin() { return ""; }
            @Override public String last4() { return ""; }
            @Override public String billingKey() { return ""; }
            @Override public String rawJson() { return fRaw; }
        };
    }

    // ========================
    // Card Meta Extraction
    // ========================
    @Override
    public PlanCardMeta extractCardMeta(String rawJson) {
        try {
            if (rawJson == null || rawJson.isBlank()) return null;
            JsonNode root = mapper.readTree(rawJson);
            JsonNode card = firstNonNull(
                    pickNode(root, "method", "card"),
                    pickNode(root, "paymentMethod", "card"),
                    pickNode(root, "card")
            );
            if (card == null) return null;

            String last4 = coalesce(pickText(card, "number", "last4"), pickText(card, "last4"));
            String bin   = coalesce(pickText(card, "number", "bin"),   pickText(card, "bin"));
            String brand = coalesce(pickText(card, "brand"), pickText(card, "issuer", "name"));
            return new PlanCardMeta(safe(last4), safe(bin), safe(brand), safe(portone.getPgName()));
        } catch (Exception e) {
            log.warn("extractCardMeta parse fail: {}", e.toString());
            return null;
        }
    }

    // ========================
    // helpers
    // ========================
    private String makeOrderId() {
        return "od_" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
               "_" + UUID.randomUUID().toString().substring(0,8);
    }
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static String safe(String s) { return s == null ? "" : s; }
    private String coalesce(String... vals) { for (String v: vals) if (notBlank(v)) return v; return null; }

    private String pickText(JsonNode node, String... keys) {
        JsonNode cur = node;
        for (String k: keys) {
            if (cur == null) return null;
            cur = cur.get(k);
        }
        return (cur != null && !cur.isNull()) ? cur.asText() : null;
    }

    private JsonNode pickNode(JsonNode node, String... keys) {
        JsonNode cur = node;
        for (String k: keys) {
            if (cur == null) return null;
            cur = cur.get(k);
        }
        return cur;
    }

    private JsonNode firstNonNull(JsonNode... nodes) {
        for (JsonNode n: nodes) if (n != null && !n.isNull()) return n;
        return null;
    }

    private String statusOrReason(JsonNode root, String statusFallback) {
        String s = coalesce(
                pickText(root, "status"),
                pickText(root, "failure", "message"),
                pickText(root, "message"),
                statusFallback
        );
        return s;
    }

    private PayResult fail(String reason, String raw) {
        final String fRaw = (raw == null) ? "{}" : raw;
        final String fReason = (reason == null) ? "UNKNOWN" : reason;
        return new PayResult() {
            @Override public boolean success() { return false; }
            @Override public String paymentId() { return null; }
            @Override public String receiptUrl() { return null; }
            @Override public String failReason() { return fReason; }
            @Override public String rawJson() { return fRaw; }
        };
    }
}
