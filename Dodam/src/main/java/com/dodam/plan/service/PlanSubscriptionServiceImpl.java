// src/main/java/com/dodam/plan/service/PlanSubscriptionServiceImpl.java
package com.dodam.plan.service;

import com.dodam.member.entity.MemberEntity;
import com.dodam.member.repository.MemberRepository;
import com.dodam.plan.Entity.*;
import com.dodam.plan.dto.PlanSubscriptionStartReq;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.enums.PlanEnums.PmBillingMode;
import com.dodam.plan.enums.PlanEnums.PmStatus;
import com.dodam.plan.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanSubscriptionServiceImpl implements PlanSubscriptionService {

    private final PlanInvoiceRepository invoiceRepo;
    private final PlanMemberRepository planMemberRepo;
    private final PlansRepository plansRepo;
    private final PlanTermsRepository termsRepo;
    private final PlanPriceRepository priceRepo;
    private final PlanPaymentRepository paymentRepo;
    private final MemberRepository memberRepo;

    private final PlanPortoneClientService portoneClient;

    // ⬆️ 타임아웃 기본값 늘림
    private static final Duration TIMEOUT_DEFAULT = Duration.ofSeconds(90);
    private static final Duration POLL_INTERVAL   = Duration.ofSeconds(1);
    private final ObjectMapper om = new ObjectMapper();

    /** 구독 확정 → 인보이스/멤버 반영 */
    @Override
    @Transactional
    public void activateInvoice(PlanInvoiceEntity invoice, int months) {
        PlanMember pm = invoice.getPlanMember();
        if (pm == null) throw new IllegalStateException("PlanMember가 없는 인보이스입니다. piId=" + invoice.getPiId());

        LocalDateTime now = LocalDateTime.now();

        if (pm.getPmStat() == null || pm.getPmStat() == PmStatus.CANCELED) {
            pm.setPmStat(PmStatus.ACTIVE);
        }
        pm.setPmBilMode(months == 1 ? PmBillingMode.MONTHLY : PmBillingMode.PREPAID_TERM);

        LocalDateTime termStart = (pm.getPmTermEnd() != null && pm.getPmTermEnd().isAfter(now))
                ? pm.getPmTermEnd()
                : now;

        pm.setPmTermStart(termStart);
        pm.setPmTermEnd(termStart.plusMonths(months));
        pm.setPmNextBil(pm.getPmTermEnd());
        pm.setPmCycle(months);
        planMemberRepo.save(pm);

        invoice.setPiStat(PiStatus.PAID);
        invoice.setPiPaid(LocalDateTime.now());
        invoiceRepo.save(invoice);

        log.info("[Subscription] invoice {} -> PAID, pmId={}, nextBill={}",
                invoice.getPiId(), pm.getPmId(), pm.getPmNextBil());
    }

    /** 인보이스 기준 결제 → 폴링 확정 */
    @Transactional
    public Map<String, Object> chargeByBillingKeyAndConfirm(Long invoiceId, String mid, int termMonths) {
        if (termMonths <= 0) termMonths = 1;

        PlanInvoiceEntity invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new IllegalStateException("인보이스가 존재하지 않습니다. invoiceId=" + invoiceId));

        if (invoice.getPiStat() != PiStatus.PENDING) {
            throw new IllegalStateException("결제 가능한 상태가 아닙니다. 현재=" + invoice.getPiStat());
        }

        PlanMember pm = invoice.getPlanMember();
        if (pm == null) throw new IllegalStateException("인보이스에 연결된 PlanMember가 없습니다.");

        memberRepo.findByMid(mid)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다. mid=" + mid));

        PlanPaymentEntity payment = paymentRepo.findTopByMidOrderByPayIdDesc(mid)
                .orElseThrow(() -> new IllegalStateException("결제수단이 없습니다. 먼저 빌링키를 등록하세요."));

        String billingKey = payment.getPayKey();
        if (!StringUtils.hasText(billingKey)) {
            throw new IllegalStateException("저장된 빌링키(payKey)가 없습니다.");
        }

        BigDecimal amount = invoice.getPiAmount();
        String currency = StringUtils.hasText(invoice.getPiCurr()) ? invoice.getPiCurr() : "KRW";
        long amountLong = amount.longValueExact();

        String paymentId = "inv" + invoice.getPiId() + "-ts" + System.currentTimeMillis();
        String orderName = "Dodam Subscription";

        // 🔽 즉시 → 3초 뒤로 (PortOne 인입 지연 보완)
        portoneClient.scheduleByBillingKey(
                paymentId,
                billingKey,
                amountLong,
                currency,
                payment.getPayCustomer(),
                orderName,
                Instant.now().plusSeconds(3)
        );
        log.info("[PortOne] scheduleByBillingKey requested. paymentId={}", paymentId);

        // 🔽 다중 조회 기반 폴링
        JsonNode result = pollUntilPaid(paymentId, TIMEOUT_DEFAULT);
        String status = result.path("status").asText("");

        Map<String, Object> resp = new HashMap<>();
        resp.put("invoiceId", invoiceId);
        resp.put("paymentId", paymentId);
        resp.put("status", status);

        if ("PAID".equals(status)) {
            invoice.setPiStat(PiStatus.PAID);
            invoice.setPiPaid(LocalDateTime.now());
            invoice.setPiUid(paymentId);
            invoiceRepo.save(invoice);

            activateInvoice(invoice, termMonths);

            // 카드 메타 업데이트
            try {
                updatePaymentCardMetaIfPresent(payment, result);
                JsonNode detail = portoneClient.getPaymentByOrderId(paymentId);
                updatePaymentCardMetaIfPresent(payment, detail);
            } catch (Exception e) {
                log.debug("[PaymentMeta] fetch/update skipped: {}", e.toString());
            }

            String receiptUrl = result.path("receiptUrl").asText(null);
            resp.put("receiptUrl", receiptUrl);
            log.info("[Subscription] payment {} confirmed by polling", paymentId);

        } else if ("FAILED".equals(status) || "CANCELLED".equals(status)) {
            invoice.setPiStat(PiStatus.FAILED);
            invoiceRepo.save(invoice);
            resp.put("error", result.toString());
            log.warn("[Subscription] payment {} failed: {}", paymentId, result.toString());

        } else {
            resp.put("info", result.toString());
            log.warn("[Subscription] payment {} not confirmed in time: {}", paymentId, result.toString());
        }

        return resp;
    }

    /** ===== 폴링 (다중 id 조회) ===== */
    private JsonNode pollUntilPaid(String orderId, Duration timeout) {
        Instant end = Instant.now().plus(timeout != null ? timeout : TIMEOUT_DEFAULT);
        Exception lastError = null;
        JsonNode lastSeen = null;

        try { Thread.sleep(700); } catch (InterruptedException ignored) {}

        while (Instant.now().isBefore(end)) {
            try {
                // 1) orderId(inv...)로 조회
                JsonNode byOrder = portoneClient.getPaymentByOrderId(orderId);
                if (byOrder != null && !byOrder.isMissingNode()) {
                    JsonNode node = firstPaymentNode(byOrder);
                    lastSeen = node;

                    String s = pickStatus(node);
                    if ("PAID".equals(s) || "FAILED".equals(s) || "CANCELLED".equals(s)) {
                        return node;
                    }

                    // 2) status가 없거나 PENDING이면 id/transactionId로 한 번 더 조회
                    String id1 = pick(node, "id");
                    if (id1 == null) id1 = pick(node.path("payment"), "id");
                    String tx  = pick(node, "transactionId");
                    if (tx == null) tx = pick(node.path("payment"), "transactionId");

                    String candidate = null;
                    if (id1 != null && !id1.isBlank() && !id1.startsWith("inv")) candidate = id1;
                    else if (tx != null && !tx.isBlank()) candidate = tx;

                    if (candidate != null) {
                        PlanPortoneClientService.LookupResponse lr = portoneClient.lookupPayment(candidate);
                        JsonNode j = safeJson(lr.raw());
                        JsonNode node2 = firstPaymentNode(j);
                        String s2 = pickStatus(node2);
                        if ("PAID".equals(s2) || "FAILED".equals(s2) || "CANCELLED".equals(s2)) {
                            return node2;
                        }
                        lastSeen = node2;
                    }
                }
            } catch (Exception e) {
                lastError = e;
                log.debug("[PortOne] polling error (ignored): {}", e.toString());
            }

            try { Thread.sleep(POLL_INTERVAL.toMillis()); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
        }

        ObjectNode timeoutNode = om.createObjectNode();
        timeoutNode.put("status", "TIMEOUT");
        timeoutNode.put("paymentId", orderId);
        if (lastSeen != null) timeoutNode.set("lastSeen", lastSeen);
        if (lastError != null) timeoutNode.put("lastError", lastError.toString());
        return timeoutNode;
    }
    
    @Override
    @Transactional
    public Map<String, Object> chargeAndConfirm(String mid, PlanSubscriptionStartReq req) {
        int months = (req.getMonths() != null && req.getMonths() > 0) ? req.getMonths() : 1;
        String planCode = (req.getPlanCode() != null) ? req.getPlanCode().trim() : null;
        if (!StringUtils.hasText(planCode)) {
            throw new IllegalStateException("MISSING_PLAN_CODE");
        }

        MemberEntity member = memberRepo.findByMid(mid)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다. mid=" + mid));

        PlanPaymentEntity payment = paymentRepo.findTopByMidOrderByPayIdDesc(mid)
                .orElseThrow(() -> new IllegalStateException("결제수단이 없습니다. 먼저 카드(빌링키)를 등록하세요."));

        PlansEntity plan = plansRepo.findByPlanCodeIgnoreCase(planCode)
                .orElseGet(() -> plansRepo.findByPlanCodeEqualsIgnoreCase(planCode)
                        .orElseThrow(() -> new IllegalStateException("플랜 코드가 유효하지 않습니다. planCode=" + planCode)));

        PlanTermsEntity terms = termsRepo.findByPtermMonth(months)
                .orElseThrow(() -> new IllegalStateException("해당 개월 약정이 없습니다. months=" + months));

        final String mode = (months == 1) ? "MONTHLY" : "PREPAID_TERM";

        PlanPriceEntity price = priceRepo
                .findFirstByPlan_PlanIdAndPterm_PtermIdAndPpriceBilModeAndPpriceActiveTrue(
                        plan.getPlanId(), terms.getPtermId(), mode)
                .or(() -> priceRepo.findBestPrice(plan.getPlanId(), terms.getPtermId(), mode))
                .orElseThrow(() -> new IllegalStateException("가격 정보가 없습니다. plan=" + planCode + ", months=" + months));

        final BigDecimal amount = price.getPpriceAmount();
        final String currency = StringUtils.hasText(price.getPpriceCurr()) ? price.getPpriceCurr() : "KRW";

        PlanMember pm = planMemberRepo.findByMember_Mid(mid).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        if (pm == null) {
            pm = PlanMember.builder()
                    .member(member)
                    .payment(payment)
                    .plan(plan)
                    .terms(terms)
                    .price(price)
                    .pmStat(PmStatus.ACTIVE)
                    .pmBilMode(PmBillingMode.valueOf(mode))
                    .pmStart(now)
                    .pmTermStart(now)
                    .pmTermEnd(now.plusMonths(months))
                    .pmNextBil(now.plusMonths(months))
                    .pmCycle(months)
                    .pmCancelCheck(false)
                    .build();
            pm = planMemberRepo.save(pm);
            log.info("[subscriptions/charge-and-confirm] PlanMember created mid={}, pmId={}", mid, pm.getPmId());
        }

        LocalDateTime nowTrunc = now.truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime from = nowTrunc.minusMinutes(10);
        var recentOpt = invoiceRepo.findRecentPendingSameAmount(mid, PiStatus.PENDING, amount, currency, from, nowTrunc);

        PlanInvoiceEntity invoice;
        if (recentOpt.isPresent()) {
            invoice = recentOpt.get();
            log.info("[subscriptions/charge-and-confirm] reuse pending invoice mid={}, invoiceId={}", mid, invoice.getPiId());
        } else {
            invoice = PlanInvoiceEntity.builder()
                    .planMember(pm)
                    .piStart(now)
                    .piEnd(now.plusMonths(months))
                    .piAmount(amount)
                    .piCurr(currency)
                    .piStat(PiStatus.PENDING)
                    .build();
            invoiceRepo.save(invoice);
            log.info("[subscriptions/charge-and-confirm] PENDING_CREATED mid={}, invoiceId={}", mid, invoice.getPiId());
        }

        return chargeByBillingKeyAndConfirm(invoice.getPiId(), mid, months);
    }

    /** ===== 카드 메타 업데이트 ===== */
    private void updatePaymentCardMetaIfPresent(PlanPaymentEntity payment, JsonNode root) {
        if (payment == null || root == null || root.isMissingNode()) return;

        String last4 = firstNonBlank(
                root.at("/card/number/last4").asText(null),
                root.at("/payment/card/number/last4").asText(null),
                root.at("/card/last4").asText(null),
                root.at("/payment/card/last4").asText(null)
        );
        String bin = firstNonBlank(
                root.at("/card/number/bin").asText(null),
                root.at("/payment/card/number/bin").asText(null),
                root.at("/card/bin").asText(null),
                root.at("/payment/card/bin").asText(null)
        );
        String brand = firstNonBlank(
                root.at("/card/brand").asText(null),
                root.at("/payment/card/brand").asText(null),
                root.at("/method/card/brand").asText(null)
        );
        String pg = firstNonBlank(
                root.path("pgProvider").asText(null),
                root.at("/payment/pgProvider").asText(null),
                root.at("/provider/pg").asText(null)
        );

        boolean changed = false;
        try {
            if (StringUtils.hasText(bin) && (payment.getPayBin() == null || !bin.equals(payment.getPayBin()))) {
                payment.setPayBin(bin); changed = true;
            }
            if (StringUtils.hasText(brand) && (payment.getPayBrand() == null || !brand.equals(payment.getPayBrand()))) {
                payment.setPayBrand(brand); changed = true;
            }
            if (StringUtils.hasText(last4) && (payment.getPayLast4() == null || !last4.equals(payment.getPayLast4()))) {
                payment.setPayLast4(last4); changed = true;
            }
            if (StringUtils.hasText(pg) && (payment.getPayPg() == null || !pg.equals(payment.getPayPg()))) {
                payment.setPayPg(pg); changed = true;
            }
        } catch (Exception e) {
            log.debug("[PaymentMeta] parsing failed (ignored): {}", e.toString());
        }

        if (changed) {
            paymentRepo.save(payment);
            log.info("[PaymentMeta] updated mid={}, bin={}, brand={}, last4={}, pg={}",
                    payment.getMid(), payment.getPayBin(), payment.getPayBrand(), payment.getPayLast4(), payment.getPayPg());
        } else {
            log.debug("[PaymentMeta] nothing to update for mid={}", payment.getMid());
        }
    }

    private String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
    
    private JsonNode firstPaymentNode(JsonNode root) {
        if (root == null || root.isMissingNode()) return om.createObjectNode();
        if (root.isArray()) {
            return root.size() > 0 ? root.get(0) : om.createObjectNode();
        }
        if (root.has("items") && root.path("items").isArray()) {
            JsonNode arr = root.path("items");
            return arr.size() > 0 ? arr.get(0) : om.createObjectNode();
        }
        if (root.has("content") && root.path("content").isArray()) {
            JsonNode arr = root.path("content");
            return arr.size() > 0 ? arr.get(0) : om.createObjectNode();
        }
        return root; // 이미 단건 객체 형태
    }
    
    private String pickStatus(JsonNode n) {
        String s = pick(n, "status");
        if (s == null) s = pick(n.path("payment"), "status");
        return s;
    }

    private String pick(JsonNode n, String field) {
        if (n == null || n.isMissingNode()) return null;
        String v = n.path(field).asText(null);
        return (v == null || v.isBlank()) ? null : v;
    }
    private JsonNode safeJson(String s) {
        try { return om.readTree(s == null ? "{}" : s); }
        catch (Exception e) { return om.createObjectNode(); }
    }
}
