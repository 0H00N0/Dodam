package com.dodam.plan.Entity;

import com.dodam.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
  name = "planPayment",
  uniqueConstraints = @UniqueConstraint(name = "uk_payment_mnum_customer", columnNames = {"mnum","payCustomer"}),
  indexes = {
    @Index(name="idx_planpayment_mnum", columnList="mnum"),
    @Index(name="idx_planpayment_customer", columnList="payCustomer")
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanPaymentEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long payId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "mnum", nullable = false, foreignKey = @ForeignKey(name="fk_payment_member"))
  private MemberEntity member;

  @Column(nullable=false, length=200) private String payCustomer;
  @Column(length=300) private String payKey;
  @Column(length=50)  private String payPg;
  @Column(length=50)  private String payBrand;
  @Column(length=10)  private String payBin;
  @Column(length=4)   private String payLast4;

  @Column(nullable=false) private boolean payActive = true;
  @Column(nullable=false) private LocalDateTime payCreate = LocalDateTime.now();
}
