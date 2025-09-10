package com.dodam.plan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanConfirmResDTO {
    private String id;           // 결제 ID (예: pay_xxx)
    private String status;       // 결제 상태 (PAID, FAILED, CANCELED)
    private String orderName;    // 주문명
    private Amount amount;       // 금액 정보
    private Transaction transaction; // 거래 정보
    private Receipt receipt;     // 영수증 정보

    // --- 내부 클래스 ---
    @Getter @Setter
    public static class Amount {
        private Long total;      // 총 결제 금액
        private Long paid;       // 실제 결제 금액
        private String currency; // 통화 (예: KRW)
    }

    @Getter @Setter
    public static class Transaction {
        private String id;       // 거래 ID
        private String requestedAt; // 요청 시각 (ISO8601 문자열)
    }

    @Getter @Setter
    public static class Receipt {
        private String url;      // 영수증 URL
    }
}
