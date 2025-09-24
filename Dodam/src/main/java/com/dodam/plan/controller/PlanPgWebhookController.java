// src/main/java/com/dodam/plan/controller/PlanPgWebhookController.java
package com.dodam.plan.controller;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.dto.PlanLookupResult;
import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.service.PlanBillingService;
import com.dodam.plan.service.PlanPaymentGatewayService;
import com.dodam.plan.service.PlanPortoneClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/webhooks/pg")
@RequiredArgsConstructor
public class PlanPgWebhookController {

    private final PlanInvoiceRepository invoiceRepo;
    private final PlanBillingService billingSvc;
    private final PlanPaymentGatewayService pgSvc;
    private final PlanPortoneClientService portoneClient; // orderId 조회용

    private static final JsonMapper M = JsonMapper.builder().build();
    private static final Set<String> PID_KEYS = setOf(
            "paymentId","payment_id","id","payment.id","transactionUid","transaction_uid","tx_id"
    );
    private static final Set<String> STATUS_KEYS = setOf("status");
    private static final Set<String> RECEIPT_KEYS = setOf("receiptUrl","receipt.url");

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> handle(@RequestBody String raw) {
        try {
            JsonNode root = M.readTree(raw);
            String paymentId = pickDeep(root, PID_KEYS);   // 보통 orderId
            String statusRaw = pickDeep(root, STATUS_KEYS);
            String receiptUrl = pickDeep(root, RECEIPT_KEYS);

            log.info("[WEBHOOK] recv paymentId={}, status={}, receipt={}", paymentId, statusRaw, receiptUrl);
            log.debug("[WEBHOOK][RAW] {}", raw);

            if (!StringUtils.hasText(paymentId)) {
                log.warn("[WEBHOOK] skip: empty payment id. (keys seen={})", listKeys(root));
                return ResponseEntity.ok().build();
            }

            Optional<PlanInvoiceEntity> optInv = invoiceRepo.findByPiUid(paymentId);
            if (optInv.isEmpty()) {
                try {
                    Long invId = Long.parseLong(paymentId.replaceFirst("^inv","").split("-")[0].replaceAll("[^0-9]",""));
                    optInv = invoiceRepo.findById(invId);
                } catch (Exception ignore) {}
            }
            if (optInv.isEmpty()) {
                log.warn("[WEBHOOK] invoice not found by paymentId={}", paymentId);
                return ResponseEntity.ok().build();
            }
            Long invoiceId = optInv.get().getPiId();

            String st = (statusRaw == null ? "" : statusRaw.trim().toUpperCase(Locale.ROOT));

            if (isPaid(st)) {
                String enrichedJson = raw;
                String enrichedReceipt = receiptUrl;

                // 0) 먼저 orderId -> providerId 찾기
                String providerId = null;
                try {
                    JsonNode byOrder = portoneClient.getPaymentByOrderId(paymentId); // orderId 조회
                    if (byOrder != null && !byOrder.isMissingNode()) {
                        providerId = pickProviderIdByOrderId(byOrder, paymentId); // ✅ 정확 매칭
                        if (providerId != null) {
                            String rcp = pickReceiptFromNode(findItemByOrderId(byOrder, paymentId));
                            if (StringUtils.hasText(rcp) && !StringUtils.hasText(enrichedReceipt)) {
                                enrichedReceipt = rcp;
                            }
                        } else {
                            log.debug("[WEBHOOK] no exact match for orderId={} in getPaymentByOrderId()", paymentId);
                        }
                    }
                } catch (Exception e) {
                    log.debug("[WEBHOOK] orderId enrich failed: {}", e.toString());
                }

                // 1) provider id가 있으면 그것으로 디테일 조회 → rawJson 대체
                if (StringUtils.hasText(providerId) && !providerId.startsWith("inv")) {
                    try {
                        var lr = pgSvc.lookup(providerId); // 내부에서 providerId 그대로 조회
                        if (StringUtils.hasText(lr.rawJson())) {
                            enrichedJson = lr.rawJson();
                            String rcp = tryReceipt(enrichedJson);
                            if (rcp != null && !StringUtils.hasText(enrichedReceipt)) enrichedReceipt = rcp;
                        }
                    } catch (Exception e) {
                        log.debug("[WEBHOOK] provider lookup failed: {}", e.toString());
                    }
                } else {
                    // 2) providerId를 못 찾았으면 safeLookup(paymentId)로 한 번 더 시도
                    PlanLookupResult look = pgSvc.safeLookup(paymentId);
                    if (StringUtils.hasText(look.paymentId()) && !look.paymentId().startsWith("inv")) {
                        providerId = look.paymentId();
                    }
                    if (StringUtils.hasText(look.rawJson())) {
                        enrichedJson = look.rawJson();
                        String rcp = tryReceipt(enrichedJson);
                        if (rcp != null && !StringUtils.hasText(enrichedReceipt)) enrichedReceipt = rcp;
                    }
                }

                // ✅ pattUid는 provider id가 있으면 그걸로, 없으면 orderId로
                String pattUid = StringUtils.hasText(providerId) ? providerId : paymentId;
                billingSvc.recordAttempt(invoiceId, true, null, pattUid, enrichedReceipt, enrichedJson);
                return ResponseEntity.ok().build();
            }

            if (isFailed(st)) {
                billingSvc.recordAttempt(invoiceId, false, "WEBHOOK:" + st, paymentId, receiptUrl, raw);
                return ResponseEntity.ok().build();
            }

            // 애매하면 lookup
            PlanLookupResult look = pgSvc.safeLookup(paymentId);
            String lst = look.status() == null ? "" : look.status().toUpperCase(Locale.ROOT);
            if (isPaid(lst)) {
                String rcp = firstNonBlank(tryReceipt(look.rawJson()), receiptUrl);
                billingSvc.recordAttempt(invoiceId, true, null, look.paymentId(), rcp, look.rawJson());
            } else if (isFailed(lst)) {
                billingSvc.recordAttempt(invoiceId, false, "LOOKUP:" + lst, paymentId, receiptUrl, look.rawJson());
            } else {
                billingSvc.recordAttempt(invoiceId, false, "LOOKUP:PENDING", paymentId, receiptUrl, look.rawJson());
            }
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("[WEBHOOK] error: {}", e.toString(), e);
            return ResponseEntity.ok().build();
        }
    }

    // helpers ...
    private static Set<String> setOf(String... a){ return new HashSet<>(Arrays.asList(a)); }
    private static String pickDeep(JsonNode root, Set<String> keys) {
        if (root == null) return null;
        for (String k : keys) {
            String v = findByPath(root, k);
            if (StringUtils.hasText(v)) return v;
        }
        return findByKeyAnywhere(root, keys);
    }
    private static String findByPath(JsonNode root, String dotted) {
        String[] parts = dotted.split("\\.");
        JsonNode cur = root;
        for (String p : parts) {
            if (cur == null) return null;
            cur = cur.get(p);
        }
        if (cur == null || cur.isMissingNode() || cur.isNull()) return null;
        return cur.isValueNode() ? cur.asText(null) : null;
    }
    private static String norm(String k){ return k==null? null : k.replace("_","").replace(".","").toLowerCase(Locale.ROOT); }
    private static String findByKeyAnywhere(JsonNode n, Set<String> keys){
        Set<String> norms = new HashSet<>();
        for (String k : keys) norms.add(norm(k));
        return dfs(n, norms);
    }
    private static String dfs(JsonNode n, Set<String> want){
        if (n == null) return null;
        if (n.isObject()) {
            var it = n.fields();
            while (it.hasNext()){
                var e = it.next();
                String k = norm(e.getKey());
                JsonNode v = e.getValue();
                if (want.contains(k) && v.isValueNode()){
                    String s = v.asText(null);
                    if (StringUtils.hasText(s)) return s;
                }
                String deep = dfs(v, want);
                if (StringUtils.hasText(deep)) return deep;
            }
        } else if (n.isArray()){
            for (JsonNode c : n){
                String deep = dfs(c, want);
                if (StringUtils.hasText(deep)) return deep;
            }
        }
        return null;
    }
    private static String firstNonBlank(String... v){ if (v==null) return null; for (String s : v) if (StringUtils.hasText(s)) return s; return null; }
    private static String tryReceipt(String rawJson){
        try {
            JsonNode r = M.readTree(rawJson);
            String r1 = pickDeep(r, RECEIPT_KEYS);
            if (StringUtils.hasText(r1)) return r1;
            JsonNode items = r.get("items");
            if (items != null && items.isArray() && items.size() > 0){
                return pickDeep(items.get(0), RECEIPT_KEYS);
            }
        } catch (Exception ignore) {}
        return null;
    }
    private static boolean isPaid(String s){
        if (s == null) return false;
        String u = s.trim().toUpperCase(Locale.ROOT);
        return u.equals("PAID") || u.equals("SUCCEEDED") || u.equals("SUCCESS") || u.equals("PARTIAL_PAID");
    }
    private static boolean isFailed(String s){
        if (s == null) return false;
        String u = s.trim().toUpperCase(Locale.ROOT);
        return u.equals("FAILED") || u.equals("CANCELED") || u.equals("CANCELLED");
    }
    private static List<String> listKeys(JsonNode n){
        List<String> out = new ArrayList<>();
        collectKeys(n, out, "");
        return out;
    }
    private static void collectKeys(JsonNode n, List<String> out, String prefix){
        if (n == null) return;
        if (n.isObject()){
            var it = n.fields();
            while (it.hasNext()){
                var e = it.next();
                String key = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
                out.add(key);
                collectKeys(e.getValue(), out, key);
            }
        } else if (n.isArray()){
            int i=0;
            for (JsonNode c : n){
                collectKeys(c, out, prefix + "["+(i++)+"]");
            }
        }
    }
    
    private static JsonNode findItemByOrderId(JsonNode root, String orderId) {
        if (root == null || !StringUtils.hasText(orderId)) return null;
        if (orderId.equals(root.path("orderId").asText(null))) return root;

        JsonNode items = root.get("items");
        if (items != null && items.isArray()) {
            for (JsonNode it : items) {
                if (orderId.equals(it.path("orderId").asText(null))) return it;
            }
        }
        return null;
    }

    private static String pickProviderIdByOrderId(JsonNode root, String orderId) {
        JsonNode n = findItemByOrderId(root, orderId);
        if (n == null) return null;
        return pickDeep(n, setOf("id","payment.id","transactionUid","transaction_uid","tx_id"));
    }

    private static String pickReceiptFromNode(JsonNode n) {
        if (n == null) return null;
        return pickDeep(n, RECEIPT_KEYS);
    }
}
