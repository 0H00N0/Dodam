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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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

        // 1) confirm
        ConfirmResponse res = portone.confirmByBillingKey(req);
        String status = (res.status() == null ? "UNKNOWN" : res.status().trim().toUpperCase(Locale.ROOT));
        boolean success = isPaidStatus(status);

        // 2) confirm 응답에서 1차 정보 추출
        String confirmRaw = res.raw();
        String confirmId  = n(res.id()); // 대개 inv... (merchant payment id)
        String receiptUrl = null;
        try {
            if (confirmRaw != null && confirmRaw.startsWith("{")) {
                JsonNode root = mapper.readTree(confirmRaw);
                receiptUrl = n(jsonText(root, "receiptUrl"));
                if (receiptUrl == null) receiptUrl = n(jsonText(root, "receipt", "url"));
            }
        } catch (Exception ignore) { }

        // 3) 즉시 lookup 보강 (카드메타/영수증/프로바이더 id 확보용)
        LookupResponse lookup = null;
        try {
            // confirm id 가 inv... 여도, portone.lookupPayment 가 내부에서 변환 처리함
            String lookupKey = firstNonBlank(confirmId, paymentId);
            if (StringUtils.hasText(lookupKey)) {
                lookup = portone.lookupPayment(lookupKey);
            }
        } catch (Exception e) {
            log.warn("[CardMeta] lookup after confirm failed: {}", e.toString());
        }

        // 4) provider payment uid / receipt url / rawJson 결정
        String providerIdFromLookup = (lookup != null ? n(lookup.id()) : null);
        String pattUid = firstNonBlank(providerIdFromLookup, confirmId, paymentId); // 시도기록용 uid

        // 카드 메타를 위해선 lookup.raw 가 최우선
        String rawForCardMeta = (lookup != null && StringUtils.hasText(lookup.raw())) ? lookup.raw() : confirmRaw;

        // receiptUrl 도 lookup에서 보강
        if (lookup != null && lookup.raw() != null) {
            try {
                JsonNode root = unwrapEnvelope(mapper.readTree(lookup.raw()));
                String r1 = n(jsonText(root, "receiptUrl"));
                String r2 = n(jsonText(root, "receipt", "url"));
                receiptUrl = firstNonBlank(receiptUrl, r1, r2);
            } catch (Exception ignore) { }
        }

        // 5) 최종 결과 구성 (raw 는 card meta 저장에 쓰이므로 lookup 우선)
        String finalRaw = firstNonBlank(
                (lookup != null ? lookup.raw() : null),
                confirmRaw
        );

        return new PlanPayResult(
                success,
                pattUid,                // ★ planattempt.pattUid
                receiptUrl,
                success ? null : status,
                status,
                finalRaw                // ★ billingSvc.recordAttempt(...) 에 들어가서 extractCardMeta 대상
        );
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

    // ---------- 카드 메타 추출 ----------
    @Override
    public PlanCardMeta extractCardMeta(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return new PlanCardMeta(null, null, null, null, null);
        }
        try {
            log.info("[CardMeta][RAW] {}", rawJson);

            JsonNode root0 = mapper.readTree(rawJson);
            JsonNode root  = unwrapEnvelope(root0);

            JsonNode methodNode = root.path("method");
            JsonNode cardNode   = methodNode.path("card");

            // brand: name(국문) > publisher > issuer > brand(영문)
            String brand = n(cardNode.path("name").asText(null));
            if (brand == null) brand = n(cardNode.path("publisher").asText(null));
            if (brand == null) brand = n(cardNode.path("issuer").asText(null));
            if (brand == null) brand = n(cardNode.path("brand").asText(null)); // VISA/MASTER 등

            // bin 그대로
            String bin = n(cardNode.path("bin").asText(null));

            // number의 끝 4글자를 그대로(별 포함) 사용
            String numberRaw = n(cardNode.path("number").asText(null)); // 예: 48901602****440*
            String last4 = null;
            if (numberRaw != null) {
                String compact = numberRaw.replace(" ", "");
                if (compact.length() <= 4) {
                    last4 = compact;
                } else {
                    last4 = compact.substring(compact.length() - 4); // 예: "440*"
                }
            }

            // PG provider
            String pg = n(root.path("channel").path("pgProvider").asText(null));
            if (pg == null) pg = n(root.path("pgProvider").asText(null));

            log.info("[CardMeta] parsed => brand={}, bin={}, last4={}, pg={}", brand, bin, last4, pg);
            return new PlanCardMeta(null, brand, bin, last4, pg);

        } catch (Exception e) {
            log.warn("[CardMeta] parse failed: {}", e.toString(), e);
            return new PlanCardMeta(null, null, null, null, null);
        }
    }

    // ---------- helpers ----------
    private JsonNode unwrapEnvelope(JsonNode n) {
        if (n == null) return null;
        if (n.has("items") && n.get("items").isArray() && n.get("items").size() > 0) {
            return n.get("items").get(0);
        }
        if (n.has("data") && n.get("data").isObject())   return n.get("data");
        if (n.has("payment") && n.get("payment").isObject()) return n.get("payment");
        return n;
    }

    private String jsonText(JsonNode n, String... path) {
        JsonNode cur = n;
        for (String k : path) {
            if (cur == null) return null;
            cur = cur.get(k);
        }
        return (cur == null || cur.isNull()) ? null : cur.asText(null);
    }

    private String pick(String... v) {
        for (String s : v) if (!isBlank(s)) return s;
        return null;
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }

    private Set<String> setOf(String... ks) { return new HashSet<>(java.util.Arrays.asList(ks)); }

    private String findByKey(JsonNode node, Set<String> keys) {
        if (node == null) return null;
        if (node.isObject()) {
            var it = node.fields();
            while (it.hasNext()) {
                var e = it.next();
                String k = e.getKey();
                JsonNode v = e.getValue();
                for (String want : keys) {
                    if (k.equalsIgnoreCase(want)) {
                        String val = v.isValueNode() ? v.asText(null) : null;
                        if (!isBlank(val)) return val;
                    }
                }
                String deep = findByKey(v, keys);
                if (!isBlank(deep)) return deep;
            }
        } else if (node.isArray()) {
            for (JsonNode c : node) {
                String deep = findByKey(c, keys);
                if (!isBlank(deep)) return deep;
            }
        }
        return null;
    }

    private String n(String s) { return (s == null || s.isBlank()) ? null : s; }

    private String firstNonBlank(String... arr) {
        if (arr == null) return null;
        for (String s : arr) {
            if (StringUtils.hasText(s)) return s;
        }
        return null;
    }

    private boolean isPaidStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase(Locale.ROOT);
        return s.equals("PAID") || s.equals("SUCCEEDED") || s.equals("SUCCESS") || s.equals("PARTIAL_PAID");
    }

    private boolean isFailedStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toUpperCase(Locale.ROOT);
        return s.equals("FAILED") || s.equals("CANCELED") || s.equals("CANCELLED");
    }

    private Long extractInvoiceId(String uid) {
        String num = uid.replaceFirst("^inv", "").split("-")[0].replaceAll("[^0-9]", "");
        return Long.parseLong(num);
    }
}
