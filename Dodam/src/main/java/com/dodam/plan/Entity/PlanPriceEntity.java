package com.dodam.plan.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name="PlanPrice")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanPriceEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long ppriceId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="planId", nullable=false, foreignKey=@ForeignKey(name="fk_price_plan"))
  private PlansEntity plan;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="ptermId", nullable=false, foreignKey=@ForeignKey(name="fk_price_term"))
  private PlanTermsEntity pterm;

  @Column(nullable=false, length=20)
  private String ppriceBilMode; // MONTHLY / PREPAID_TERM

  @Column(nullable=false, precision=12, scale=2)
  private BigDecimal ppriceAmount;

  @Column(nullable=false, length=3)
  private String ppriceCurr;

  @Column(nullable=false)
  private Boolean ppriceActive;

  @CreationTimestamp
  @Column(nullable=false)
  private LocalDateTime ppriceCreate;
}
