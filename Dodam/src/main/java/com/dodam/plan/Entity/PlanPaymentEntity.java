// src/main/java/com/dodam/plan/Entity/PlanPaymentEntity.java
package com.dodam.plan.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PlanPayment")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PlanPaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payId;

    // 세션의 회원 아이디
    @Column(nullable = false, length = 50)
    private String mid;

    // 고객아이디(PortOne customerId 등) – 정책에 맞춰 생성/저장
    @Column(nullable = false, length = 100, unique = true)
    private String payCustomer;

    // 빌링키
    @Column(nullable = false, length = 100, unique = true)
    private String payKey;

    @Column(length = 30)
    private String payPg;     // 예: tosspayments, nice, kcp 등

    @Column(length = 40)
    private String payBrand;  // 예: VISA/Master/BC 등

    @Column(length = 12)
    private String payBin;

    @Column(length = 8)
    private String payLast4;

    @Lob
    private String payRaw;
}
