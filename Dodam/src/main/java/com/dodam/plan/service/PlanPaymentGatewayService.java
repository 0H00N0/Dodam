// src/main/java/com/dodam/plan/service/PlanPaymentGatewayService.java
package com.dodam.plan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.dodam.plan.Entity.PlanPaymentEntity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanPaymentGatewayService {

    // ✅ (1) ObjectMapper는 빈으로 주입 가능하지만, 여기서는 간단히 재사용
    private final ObjectMapper mapper = new ObjectMapper();

    // ✅ (2) WebClient 공통 설정: baseUrl + 기본 Accept 헤더
    private final WebClient web = WebClient.builder()
            .baseUrl("https://api.portone.io") // 공통 prefix
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    // === 공통 유틸 ===
    private static String txt(JsonNode n){
        return (n==null||n.isMissingNode()||n.isNull())? "" : n.asText("").trim();
    }
    private static String nz(String s){ return s==null? "" : s; }
    private static String clampBin(String bin){
        if (!StringUtils.hasText(bin)) return "";
        return bin.length() > 6 ? bin.substring(0,6) : bin;
    }
    private static String clampLast4(String last4){
        if (!StringUtils.hasText(last4)) return "";
        return last4.length() > 4 ? last4.substring(last4.length()-4) : last4;
    }

    // ✅ (3) 실제 인증 헤더 생성
    // - PortOne v2는 계정 설정에 따라 Basic(apiKey:secret) 또는 Bearer를 사용합니다.
    // - 지금은 Basic 예시로 구현 (환경에 맞게 교체 가능).
    private String auth(){
        // TODO: properties에서 apiKey/secret을 주입받아 사용하세요.
        String apiKey = System.getenv("test_ck_jZ61JOxRQVEAgxoERWwVW0X9bAqw");   // 예: test_ck_...
        String secret = System.getenv("test_sk_4vZnjEJeQVxD1gk2Gw9VPmOoBN0k");     // 예: test_sk_...
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(secret)) {
            // 개발 편의를 위해 placeholder 허용 (운영에선 예외를 던지세요)
            log.warn("PortOne API key/secret not set in env. Using placeholder.");
            apiKey = "test_ck_jZ61JOxRQVEAgxoERWwVW0X9bAqw";
            secret = "test_sk_4vZnjEJeQVxD1gk2Gw9VPmOoBN0k";
        }
        String basic = apiKey + ":" + secret;
        String b64 = Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));
        return "Basic " + b64; // 필요 시 "Bearer <token>"로 교체
    }

    // === DTOs ===
    public record PaymentChargeRes(boolean success, String failReason, String uid, String receiptUrl, String rawJson) {}
    public record PgLookupResult(String paymentId, String txId, String status, Long amount,
                                 String billingKey, String pg, String brand, String bin, String last4, String rawJson) {}
    public record CardMeta(String pg, String brand, String bin, String last4, String raw) {
        public static CardMeta empty(){ return new CardMeta("", "", "", "", ""); }
    }

    // === 1) 리다이렉트 복귀 조회(paymentId/txId) ===
    public PgLookupResult safeLookup(String txId, String paymentId) {
        if (!StringUtils.hasText(paymentId) && !StringUtils.hasText(txId))
            throw new IllegalArgumentException("Either paymentId or txId is required");
        try {
            final String path = StringUtils.hasText(paymentId)
                    ? "/v2/payments/" + paymentId
                    : "/v2/transactions/" + txId;

            String raw = web.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, auth())
                    .retrieve()
                    .onStatus(s -> s.isError(), r -> r.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new RuntimeException("PORTONE_LOOKUP_ERROR "+r.statusCode()+" "+b))))
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = (raw==null)? mapper.createObjectNode() : mapper.readTree(raw);

            String _pid = StringUtils.hasText(paymentId) ? paymentId : txt(root.path("id"));
            String _tx  = StringUtils.hasText(txId) ? txId : txt(root.path("txId"));
            String st   = txt(root.path("status"));

            Long amount = null;
            String amt = txt(root.path("amount").path("total"));
            if (!StringUtils.hasText(amt)) amt = txt(root.path("amount"));
            if (StringUtils.hasText(amt)) try { amount = Long.parseLong(amt); } catch (Exception ignored) {}

            String bKey = txt(root.path("billingKey"));
            if (!StringUtils.hasText(bKey)) bKey = txt(root.path("paymentMethod").path("billingKey"));

            String pg  = txt(root.path("pg_provider"));
            if (!StringUtils.hasText(pg)) pg = txt(root.path("paymentMethod").path("provider"));

            String brand = txt(root.path("card").path("brand"));
            if (!StringUtils.hasText(brand)) brand = txt(root.path("card").path("issuer_name"));
            if (!StringUtils.hasText(brand)) brand = txt(root.path("paymentMethod").path("card").path("brand"));

            String bin = txt(root.path("card").path("bin"));
            if (!StringUtils.hasText(bin)) bin = txt(root.path("card").path("number").path("first6"));
            if (!StringUtils.hasText(bin)) bin = txt(root.path("paymentMethod").path("card").path("number").path("first6"));

            String last4 = txt(root.path("card").path("last4"));
            if (!StringUtils.hasText(last4)) last4 = txt(root.path("card").path("number").path("last4"));
            if (!StringUtils.hasText(last4)) last4 = txt(root.path("paymentMethod").path("card").path("number").path("last4"));

            // ✅ 길이 보정
            bin   = clampBin(bin);
            last4 = clampLast4(last4);

            return new PgLookupResult(_pid, _tx, st, amount, bKey, pg, brand, bin, last4, raw);
        } catch (Exception e) {
            log.error("[safeLookup] error txId={}, paymentId={}", txId, paymentId, e);
            throw new RuntimeException("LOOKUP_FAILED", e);
        }
    }

    // === 2) paymentId 직접 승인 ===
    public PaymentChargeRes confirmPaymentRaw(String paymentId, long amount) {
        try {
            String path = "/v2/payments/" + paymentId + "/confirm";
            String body = mapper.createObjectNode().put("amount", amount).toString();

            String raw = web.post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(s -> s.isError(), r -> r.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new RuntimeException("PORTONE_CONFIRM_ERROR "+r.statusCode()+" "+b))))
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = (raw==null)? mapper.createObjectNode() : mapper.readTree(raw);
            String st = txt(root.path("status"));
            boolean ok = "paid".equalsIgnoreCase(st) || "succeeded".equalsIgnoreCase(st) || "success".equalsIgnoreCase(st);

            String uid = txt(root.path("id"));
            String rc  = txt(root.path("receipt_url"));
            if (!StringUtils.hasText(rc)) rc = txt(root.path("receiptUrl"));

            String reason = ok ? null :
                    (StringUtils.hasText(txt(root.path("fail").path("message"))) ? txt(root.path("fail").path("message"))
                            : txt(root.path("message")));

            return new PaymentChargeRes(ok, reason, StringUtils.hasText(uid)? uid : paymentId, rc, raw);
        } catch (Exception e) {
            log.error("[confirmPaymentRaw] error paymentId={}", paymentId, e);
            return new PaymentChargeRes(false, e.getMessage(), paymentId, null, null);
        }
    }

    // === 3) 빌링키로 승인(정기결제) ===
    public PaymentChargeRes payWithBillingKey(String invoiceUid, String customerId, String billingKey, long amount) {
        try {
            String path = "/v2/payments/confirm";
            String body = mapper.createObjectNode()
                    .put("orderId", invoiceUid)
                    .put("amount", amount)
                    .put("customerId", customerId)
                    .put("billingKey", billingKey)
                    .toString();

            String raw = web.post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, auth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(s -> s.isError(), r -> r.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new RuntimeException("PORTONE_BILLING_CONFIRM_ERROR "+r.statusCode()+" "+b))))
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = (raw==null)? mapper.createObjectNode() : mapper.readTree(raw);
            String st = txt(root.path("status"));
            boolean ok = "paid".equalsIgnoreCase(st) || "succeeded".equalsIgnoreCase(st) || "success".equalsIgnoreCase(st);
            String uid = txt(root.path("id"));
            if (!StringUtils.hasText(uid)) uid = invoiceUid;

            String rc  = txt(root.path("receipt_url"));
            if (!StringUtils.hasText(rc)) rc = txt(root.path("receiptUrl"));

            String reason = ok ? null :
                    (StringUtils.hasText(txt(root.path("fail").path("message"))) ? txt(root.path("fail").path("message"))
                            : txt(root.path("message")));

            return new PaymentChargeRes(ok, reason, uid, rc, raw);
        } catch (Exception e) {
            log.error("[payWithBillingKey] error invoiceUid={}, customerId={}", invoiceUid, customerId, e);
            return new PaymentChargeRes(false, e.getMessage(), invoiceUid, null, null);
        }
    }

    // === 4) 빌링키 → 카드 메타 (null 대신 empty 반환 + 길이 보정) ===
    public CardMeta getCardMetaByBillingKey(String billingKey) {
        if (!StringUtils.hasText(billingKey)) return CardMeta.empty();
        try {
            String path = "/v2/billing-keys/" + billingKey;

            String raw = web.get()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, auth())
                    .retrieve()
                    .onStatus(s -> s.isError(), r -> r.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new RuntimeException("PORTONE_BILLINGKEY_ERROR "+r.statusCode()+" "+b))))
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = (raw==null)? mapper.createObjectNode() : mapper.readTree(raw);

            String pg  = txt(root.path("pg_provider"));
            if (!StringUtils.hasText(pg)) pg = txt(root.path("paymentMethod").path("provider"));

            String brand = txt(root.path("card").path("brand"));
            if (!StringUtils.hasText(brand)) brand = txt(root.path("card").path("issuer_name"));
            if (!StringUtils.hasText(brand)) brand = txt(root.path("paymentMethod").path("card").path("brand"));

            String bin = txt(root.path("card").path("bin"));
            if (!StringUtils.hasText(bin)) bin = txt(root.path("card").path("number").path("first6"));
            if (!StringUtils.hasText(bin)) bin = txt(root.path("paymentMethod").path("card").path("number").path("first6"));

            String last4 = txt(root.path("card").path("last4"));
            if (!StringUtils.hasText(last4)) last4 = txt(root.path("card").path("number").path("last4"));
            if (!StringUtils.hasText(last4)) last4 = txt(root.path("paymentMethod").path("card").path("number").path("last4"));

            // ✅ 길이 보정
            bin   = clampBin(bin);
            last4 = clampLast4(last4);

            return new CardMeta(nz(pg), nz(brand), bin, last4, nz(raw));
        } catch (Exception e) {
            log.error("[getCardMetaByBillingKey] error billingKey={}", billingKey, e);
            // ❗ null 대신 empty 반환 (NPE 예방)
            return CardMeta.empty();
        }
    }
    
    /**
     * 엔티티에 카드 메타를 비어 있을 때만 보강합니다.
     * - billingKey로 PortOne 조회 → brand/bin/last4/pg 채움
     * - 값이 이미 있으면 덮어쓰지 않음
     */
    public void enrichCardMetaIfEmpty(PlanPaymentEntity e, String billingKey) {
        if (e == null || !StringUtils.hasText(billingKey)) return;

        boolean needBrand = !StringUtils.hasText(e.getPayBrand());
        boolean needBin   = !StringUtils.hasText(e.getPayBin());
        boolean needLast4 = !StringUtils.hasText(e.getPayLast4());
        boolean needPg    = !StringUtils.hasText(e.getPayPg());

        if (!(needBrand || needBin || needLast4 || needPg)) return;

        CardMeta meta = getCardMetaByBillingKey(billingKey);
        if (meta == null) return; // getCardMetaByBillingKey가 empty를 반환한다면 이 줄은 생략 가능

        if (needPg && StringUtils.hasText(meta.pg()))       e.setPayPg(meta.pg());
        if (needBrand && StringUtils.hasText(meta.brand())) e.setPayBrand(meta.brand());

        // 길이 보정(이미 서비스에 clamp 헬퍼가 있다면 그걸 사용)
        String bin   = meta.bin();
        String last4 = meta.last4();
        if (StringUtils.hasText(bin))   e.setPayBin(bin.length() > 6 ? bin.substring(0,6) : bin);
        if (StringUtils.hasText(last4)) e.setPayLast4(last4.length() > 4 ? last4.substring(last4.length()-4) : last4);
    }
}
