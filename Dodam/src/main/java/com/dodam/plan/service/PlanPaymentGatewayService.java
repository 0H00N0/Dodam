// src/main/java/com/dodam/plan/service/PlanPaymentGatewayService.java
package com.dodam.plan.service;

import com.dodam.plan.dto.PlanCardMeta;

public interface PlanPaymentGatewayService {

    PayResult payByBillingKey(String billingKey, long amount, String customerId);
    PayResult payByBillingKey(String paymentId, String billingKey, long amount, String customerId);
    PayResult payByBillingKey(String paymentId, String customerId, String billingKey, long amount);

    PlanCardMeta extractCardMeta(String rawJson);
    LookupResult safeLookup(String paymentId);

    interface PayResult {
        boolean success();
        String paymentId();
        default String uid() {              // ✅ 호환용 alias
            return paymentId();
        }
        String receiptUrl();
        String failReason();
        String rawJson();
    }

    interface LookupResult {
        String paymentId();
        String status();
        String rawJson();
    }
}
