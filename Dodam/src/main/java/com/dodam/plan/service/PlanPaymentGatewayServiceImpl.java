// src/main/java/com/dodam/plan/service/PlanPaymentGatewayServiceImpl.java
package com.dodam.plan.service;

import com.dodam.plan.config.PlanPortoneProperties;
import com.dodam.plan.dto.PlanCardMeta;
import com.dodam.plan.dto.PlanLookupResult;
import com.dodam.plan.dto.PlanPayResult;
import com.dodam.plan.dto.PlanPaymentLookupResult;
import com.dodam.plan.repository.PlanAttemptRepository;
import com.dodam.plan.service.PlanPortoneClientService.ConfirmRequest;
import com.dodam.plan.service.PlanPortoneClientService.ConfirmResponse;
import com.dodam.plan.service.PlanPortoneClientService.LookupResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PlanPaymentGatewayServiceImpl implements PlanPaymentGatewayService {

    private final PlanPortoneClientService portone;
    private final PlanPortoneProperties props;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PlanAttemptRepository attemptRepo;

    public PlanPaymentGatewayServiceImpl(
            @Qualifier("planPortoneClientServiceImpl") PlanPortoneClientService portone,
            PlanPortoneProperties props,
            PlanAttemptRepository attemptRepo) {
        this.portone = portone;
        this.props = props;
        this.attemptRepo = attemptRepo;
    }

    @Override
    public PlanPayResult payByBillingKey(String paymentId, String billingKey, long amount, String customerId) {
        return payByBillingKey(
                paymentId, billingKey, amount,
                props.getCurrency() != null ? props.getCurrency() : "KRW",
                "Dodam Subscription",
                props.getStoreId(), customerId, props.getChannelKey()
        );
    }

    @Override
    public PlanPayResult payByBillingKey(String paymentId, String billingKey, long amount, String currency,
                                         String orderName, String storeId, String customerId, String channelKey) {

        ConfirmRequest req = new ConfirmRequest(
                paymentId, billingKey, amount, currency, customerId, orderName,
                Boolean.TRUE.equals(props.getIsTest())
        );

        ConfirmResponse res = portone.confirmByBillingKey(req);
        String status = res.status() == null ? "UNKNOWN" : res.status().trim().toUpperCase();

        // 성공 판정
        boolean success = "PAID".equals(status) || "SUCCEEDED".equals(status) || "PARTIAL_PAID".equals(status);

        // ✅ providerId를 우선 사용 (inv… 아님)
        String providerPaymentUid = n(res.id());
        String receiptUrl = null;

        try {
            if (res.raw() != null && res.raw().startsWith("{")) {
                JsonNode root = mapper.readTree(res.raw());
                // 영수증 URL 후보
                receiptUrl = n(root.path("receiptUrl").asText(null));
                if (receiptUrl == null) receiptUrl = n(root.path("receipt").path("url").asText(null));
            }
        } catch (Exception ignore) { }

        // pattUid 저장용 id (providerId가 없으면 최후에 merchant id 유지)
        String payUidToStore = providerPaymentUid != null ? providerPaymentUid : paymentId;

        return new PlanPayResult(
                success,
                payUidToStore,          // ★ 여기 값이 시도기록 pattUid가 됨 (성공/대기 모두 저장 필요)
                receiptUrl,
                success ? null : status,
                status,
                res.raw()
        );
    }

    private String resolvePaymentId(String anyId) {
        if (!org.springframework.util.StringUtils.hasText(anyId)) return anyId;
        if (anyId.startsWith("inv")) {
            Long invoiceId = extractInvoiceId(anyId);
            return attemptRepo.findLatestPaymentUidByInvoiceId(invoiceId)
                    .filter(org.springframework.util.StringUtils::hasText)
                    .orElse(null);
        }
        return anyId;
    }

    @Override
    public PlanLookupResult safeLookup(String paymentId) {
        try {
            String pid = resolvePaymentId(paymentId);
            if (!org.springframework.util.StringUtils.hasText(pid)) {
                return new PlanLookupResult(false, paymentId, "NOT_FOUND", "{\"error\":\"no providerId for invoice\"}");
            }
            LookupResponse r = portone.lookupPayment(pid);
            boolean ok = isPaidStatus(r.status());
            return new PlanLookupResult(ok, r.id(), r.status(), r.raw());
        } catch (Exception e) {
            return new PlanLookupResult(false, paymentId, "ERROR", "{\"error\":\"" + e + "\"}");
        }
    }

    @Override
    public PlanLookupResult lookup(String paymentId) {
        String pid = resolvePaymentId(paymentId);
        if (!org.springframework.util.StringUtils.hasText(pid)) {
            return new PlanLookupResult(false, paymentId, "NOT_FOUND", "{\"error\":\"no providerId for invoice\"}");
        }
        var r = portone.lookupPayment(pid);
        boolean ok = isPaidStatus(r.status());
        return new PlanLookupResult(ok, r.id(), r.status(), r.raw());
    }

    @Override
    public PlanPaymentLookupResult lookupPayment(String paymentId) {
        String pid = resolvePaymentId(paymentId);
        try {
            if (!org.springframework.util.StringUtils.hasText(pid)) {
                return new PlanPaymentLookupResult(paymentId, "NOT_FOUND", "{\"error\":\"no providerId for invoice\"}", HttpStatus.OK);
            }
            var r = portone.lookupPayment(pid);
            return new PlanPaymentLookupResult(r.id(), r.status(), r.raw(), HttpStatus.OK);
        } catch (Exception e) {
            return new PlanPaymentLookupResult(paymentId, "ERROR", e.toString(), HttpStatus.BAD_GATEWAY);
        }
    }

    @Override
    public com.dodam.plan.dto.PlanCardMeta extractCardMeta(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return new PlanCardMeta(null, null, null, null, null);
        try {
            JsonNode root = mapper.readTree(rawJson);
            JsonNode card = root.path("method").path("card");
            String brand = n(card.path("brand").asText(null));
            String bin = n(card.path("bin").asText(null));
            String last4 = n(card.path("number").path("last4").asText(null));
            String pg = n(root.path("pgProvider").asText(null));

            if (brand == null) brand = n(root.path("card").path("brand").asText(null));
            if (bin == null) bin = n(root.path("card").path("bin").asText(null));
            if (last4 == null) last4 = n(root.path("card").path("last4").asText(null));
            if (pg == null) pg = n(root.path("pg").asText(null));

            return new PlanCardMeta(null, brand, bin, last4, pg);
        } catch (Exception e) {
            log.warn("[CardMeta] parse failed: {}", e.toString());
            return new PlanCardMeta(null, null, null, null, null);
        }
    }

    private boolean isPaidStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase();
        return s.equals("PAID") || s.equals("SUCCEEDED") || s.equals("PARTIAL_PAID");
    }
    private String n(String s) { return (s == null || s.isBlank()) ? null : s; }

    private Long extractInvoiceId(String uid) {
        String num = uid.replaceFirst("^inv", "").split("-")[0].replaceAll("[^0-9]", "");
        return Long.parseLong(num);
    }
}
