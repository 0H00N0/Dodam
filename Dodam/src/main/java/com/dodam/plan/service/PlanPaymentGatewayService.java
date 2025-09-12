package com.dodam.plan.service;

import com.dodam.plan.dto.PlanCardMeta;

public interface PlanPaymentGatewayService {

	/** v2: 빌링키로 즉시 승인 */
    PayResult payByBillingKey(String billingKey, long amount, String customerId);

    /** (호환) 예전 호출 시그니처: uid는 내부 결제 식별자 용도로만 쓰였으므로 무시하고 위 메서드로 위임 */
    default PayResult payWithBillingKey(String uid, String customerId, String billingKey, long amount) {
        return payByBillingKey(billingKey, amount, customerId);
    }

    /** (호환) ‘결제아이디 + 금액’으로 확인 호출하던 레거시 코드 지원 */
    PayResult confirmPaymentRaw(String paymentId, long amount);

    /** (호환) PG 조회(브랜드, BIN, last4, billingKey 등) */
    PgLookupResult safeLookup(String txId, String paymentId);

    /** 결제 원문에서 카드 메타 추출 */
    PlanCardMeta extractCardMeta(String rawJson);

    // ======= Result Types =======
    interface PayResult {
        boolean success();
        String paymentId();
        String receiptUrl();
        String failReason();
        String rawJson();

        /** (호환) 과거 코드에서 uid()를 쓰므로 paymentId()와 동일하게 제공 */
        default String uid() { return paymentId(); }
    }

    interface PgLookupResult {
        String txId();        // 내부 txId (넘겨받은 값 그대로 echo)
        String paymentId();   // PG 결제 id
        String pg();          // 예: TOSSPAYMENTS, KCP ...
        String brand();       // 예: VISA, MASTER, 하나카드 등
        String bin();         // 6~8자리
        String last4();       // 4자리
        String billingKey();  // 저장된 빌링키
        String rawJson();     // 원문
    }
}
