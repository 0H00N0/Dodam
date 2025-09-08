package com.dodam.plan.service;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import com.dodam.plan.repository.PlanInvoiceRepository;
import com.dodam.plan.repository.PlanPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlanPaymentOrchestratorService {

    private final PlanInvoiceRepository invoiceRepo;
    private final PlanPaymentRepository paymentRepo;
    private final PlanBillingService billingSvc;
    private final PlanPaymentGatewayService pgSvc;

    @Transactional
    public void tryPayInvoice(Long invoiceId) {
        var inv = invoiceRepo.findById(invoiceId).orElseThrow();
        internalPay(inv, null);
    }

    @Transactional
    public void tryPayInvoiceForMember(Long invoiceId, Long mnum) {
        var inv = invoiceRepo.findById(invoiceId).orElseThrow();
        var owner = inv.getPlanMember().getMember().getMnum();
        if (!owner.equals(mnum)) {
            billingSvc.recordAttempt(inv.getPiId(), false, "FORBIDDEN", null, null,
                    "{\"reason\":\"forbidden\"}");
            return;
        }
        internalPay(inv, mnum);
    }

    private void internalPay(PlanInvoiceEntity inv, Long mnum) {
        // 멱등성: PENDING 아니면 무시
        if (inv.getPiStat() != PiStatus.PENDING) {
            return;
        }

        var pm = inv.getPlanMember();
        var rel = pm.getPayment();
        if (rel == null) {
            billingSvc.recordAttempt(inv.getPiId(), false, "NO_PAYMENT_METHOD", null, null,
                    "{\"reason\":\"no_payment_method\"}");
            return;
        }

        var payment = paymentRepo.findById(rel.getPayId()).orElse(null);
        if (payment == null || payment.getPayKey() == null || payment.getPayKey().isBlank()) {
            billingSvc.recordAttempt(inv.getPiId(), false, "NO_BILLING_KEY", null, null,
                    "{\"reason\":\"no_billing_key\"}");
            return;
        }

        var res = pgSvc.payWithBillingKey(
                inv.getPiUid(),
                payment.getPayCustomer(),
                payment.getPayKey(),
                inv.getPiAmount()
        );

        billingSvc.recordAttempt(
                inv.getPiId(),
                res.success(),
                res.failReason(),
                res.uid(),
                res.receiptUrl(),
                res.rawJson()
        );
    }
}
