// src/main/java/com/dodam/plan/dto/PlanCardMeta.java
package com.dodam.plan.dto;

import lombok.Data;

/**
 * 카드 메타정보 (브랜드, BIN, 끝 4자리, 발급사명, 고객아이디)
 * - 기존 코드에서 new PlanCardMeta(brand, bin, last4, issuerName) 4-인자 생성자를
 *   사용하던 부분과 호환을 위해 4-인자 생성자도 유지합니다.
 */
@Data
public class PlanCardMeta {
    private String brand;
    private String bin;
    private String last4;
    private String issuerName;
    private String customerId;  // ✅ 추가
    private String pg;
    
    public PlanCardMeta() {}

    /** 기존 4-인자 생성자 (호환용) */
    public PlanCardMeta(String brand, String bin, String last4, String issuerName) {
        this.brand = brand;
        this.bin = bin;
        this.last4 = last4;
        this.issuerName = issuerName;
    }

    /** 신규 5-인자 생성자 */
    public PlanCardMeta(String brand, String bin, String last4, String issuerName, String customerId) {
        this.brand = brand;
        this.bin = bin;
        this.last4 = last4;
        this.issuerName = issuerName;
        this.customerId = customerId;
    }
}
