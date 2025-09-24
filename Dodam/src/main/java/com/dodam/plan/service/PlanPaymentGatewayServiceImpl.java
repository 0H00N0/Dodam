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
import org.springframework.util.StringUtils;

import java.util.Locale;

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
            PlanAttemptRepository attemptRepo
    ) {
        this.portone = portone;
        this.props = props;
        this.attemptRepo = attemptRepo;
    }

    @Override
    public PlanPayResult payByBillingKey(String paymentId, String billingKey, long amount, String customerId) {
        return payByBillingKey(
                paymentId,
                billingKey,
                amount,
                props.getCurrency() != null ? props.getCurrency() : "KRW",
                "Dodam Subscription",
                props.getStoreId(),
                customerId,
                props.getChannelKey()
        );
    }

    @Override
    public PlanPayResult payByBillingKey(
            String paymentId,
            String billingKey,
            long amount,
            String currency,
            String orderName,
            String storeId,
            String customerId,
            String channelKey
    ) {
        ConfirmRequest req = new ConfirmRequest(
                paymentId, billingKey, amount, currency, customerId, orderName,
                Boolean.TRUE.equals(props.getIsTest())
        );

        ConfirmResponse res = portone.confirmByBillingKey(req);
        String status = norm(res.status());
        boolean success = isPaidStatus(status);

        String providerPaymentUid = n(res.id());
        String receiptUrl = null;

        try {
            if (res.raw() != null && res.raw().startsWith("{")) {
                JsonNode root = mapper.readTree(res.raw());
                receiptUrl = n(root.path("receiptUrl").asText(null));
                if (receiptUrl == null) receiptUrl = n(root.path("receipt").path("url").asText(null));
            }
        } catch (Exception ignore) {}

        String payUidToStore = n(providerPaymentUid) != null ? providerPaymentUid : paymentId;

        // ------- 보강 폴링 (최대 6초) -------
        if (!success) {
            final String loopKey = resolvePaymentId(firstNonBlank(providerPaymentUid, paymentId));
            final long until = System.currentTimeMillis() + 6_000L;
            while (System.currentTimeMillis() < until) {
                try {
                    LookupResponse lr = portone.lookupPayment(loopKey);
                    String st = norm(lr.status());
                    if (isPaidStatus(st)) {
                        success = true;
                        status = st;
                        providerPaymentUid = n(lr.id());
                        if (lr.raw() != null && !lr.raw().isBlank()) {
                            try {
                                JsonNode root = mapper.readTree(lr.raw());
                                String rcp = n(root.path("receiptUrl").asText(null));
                                if (rcp == null) rcp = n(root.path("receipt").path("url").asText(null));
                                if (rcp != null) receiptUrl = rcp;
                            } catch (Exception ignore) {}
                        }
                        break;
                    }
                    if (isFailedStatus(st)) { status = st; break; }
                } catch (Exception ignore) {}
                try { Thread.sleep(700); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); break;
                }
            }
        }

        return new PlanPayResult(success, payUidToStore, receiptUrl, success ? null : status, status, res.raw());
    }

    private String resolvePaymentId(String anyId) {
        if (!StringUtils.hasText(anyId)) return anyId;
        if (anyId.startsWith("inv")) {
            Long invoiceId = extractInvoiceId(anyId);
            return attemptRepo.findLatestPaymentUidByInvoiceId(invoiceId)
                    .filter(StringUtils::hasText)
                    .orElse(null);
        }
        return anyId;
    }

    @Override
    public PlanLookupResult safeLookup(String paymentId) {
        try {
            String pid = resolvePaymentId(paymentId);
            if (!StringUtils.hasText(pid)) {
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
        if (!StringUtils.hasText(pid)) {
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
            if (!StringUtils.hasText(pid)) {
                return new PlanPaymentLookupResult(paymentId, "NOT_FOUND", "{\"error\":\"no providerId for invoice\"}", HttpStatus.OK);
            }
            var r = portone.lookupPayment(pid);
            return new PlanPaymentLookupResult(r.id(), r.status(), r.raw(), HttpStatus.OK);
        } catch (Exception e) {
            return new PlanPaymentLookupResult(paymentId, "ERROR", e.toString(), HttpStatus.BAD_GATEWAY);
        }
    }

    // -------- 카드 메타 추출 --------
    @Override
    public PlanCardMeta extractCardMeta(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return new PlanCardMeta(null, null, null, null, null);
        }
        try {
            JsonNode root0 = mapper.readTree(rawJson);
            JsonNode root = selectPaymentNode(root0); // ✅ 변경: 최신/정확 노드 선택

            JsonNode methodNode = root.path("method");
            JsonNode cardNode = methodNode.path("card");

            String brand = n(cardNode.path("name").asText(null));
            if (brand == null) brand = n(cardNode.path("publisher").asText(null));
            if (brand == null) brand = n(cardNode.path("issuer").asText(null));
            if (brand == null) brand = n(cardNode.path("brand").asText(null));

            String bin = n(cardNode.path("bin").asText(null));

            String last4 = null;
            String number = n(cardNode.path("number").asText(null));
            if (number != null) {
                String compact = number.replace(" ", "");
                last4 = compact.length() <= 4 ? compact : compact.substring(compact.length() - 4);
            }
            if (last4 == null) last4 = n(cardNode.path("last4").asText(null));

            String pg = n(root.path("channel").path("pgProvider").asText(null));
            if (pg == null) pg = n(root.path("pgProvider").asText(null));

            return new PlanCardMeta(null, brand, bin, last4, pg);
        } catch (Exception e) {
            log.warn("[CardMeta] parse failed: {}", e.toString(), e);
            return new PlanCardMeta(null, null, null, null, null);
        }
    }

    // ✅ 변경: payment > data > items(최신) 우선
    private JsonNode selectPaymentNode(JsonNode n) {
        if (n == null) return null;
        if (n.has("payment") && n.get("payment").isObject()) return n.get("payment");
        if (n.has("data") && n.get("data").isObject()) return n.get("data");
        if (n.has("items") && n.get("items").isArray() && n.get("items").size() > 0) {
            return pickNewestItem(n.get("items"));
        }
        return n;
    }

    private JsonNode pickNewestItem(JsonNode items) {
        JsonNode best = null;
        long bestTs = Long.MIN_VALUE;
        for (JsonNode it : items) {
            long ts = scoreTime(it);
            if (ts > bestTs) { bestTs = ts; best = it; }
        }
        return (best != null) ? best : items.get(items.size() - 1);
    }

    private long scoreTime(JsonNode n) {
        return parseTs(
                n.path("updatedAt").asText(null),
                n.path("paidAt").asText(null),
                n.path("statusChangedAt").asText(null),
                n.path("requestedAt").asText(null)
        );
    }

    private long parseTs(String... ss) {
        for (String s : ss) {
            if (s != null && !s.isBlank()) {
                try { return java.time.OffsetDateTime.parse(s).toInstant().toEpochMilli(); }
                catch (Exception ignore) {}
            }
        }
        return Long.MIN_VALUE;
    }


    private JsonNode unwrapEnvelope(JsonNode n) {
        if (n == null) return null;
        if (n.has("items") && n.get("items").isArray() && n.get("items").size() > 0) return n.get("items").get(0);
        if (n.has("data") && n.get("data").isObject()) return n.get("data");
        if (n.has("payment") && n.get("payment").isObject()) return n.get("payment");
        return n;
    }

    private String n(String s) { return (s == null || s.isBlank()) ? null : s; }
    private String firstNonBlank(String... a) { for (String s : a) if (StringUtils.hasText(s)) return s; return null; }
    private String norm(String v){ return (v==null) ? "" : v.trim().toUpperCase(Locale.ROOT); }

    private boolean isPaidStatus(String status) {
        String s = norm(status);
        return s.equals("PAID") || s.equals("SUCCEEDED") || s.equals("SUCCESS") || s.equals("PARTIAL_PAID");
    }
    private boolean isFailedStatus(String status) {
        String s = norm(status);
        return s.equals("FAILED") || s.equals("CANCELED") || s.equals("CANCELLED");
    }

    private Long extractInvoiceId(String uid) {
        String num = uid.replaceFirst("^inv","").split("-")[0].replaceAll("[^0-9]","");
        return Long.parseLong(num);
    }
}
