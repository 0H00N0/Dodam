package com.dodam.plan.Entity;

import com.dodam.plan.enums.PlanEnums.PattResult;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="planAttempt", indexes=@Index(name="idx_planattempt_piid", columnList="piId"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanAttemptEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long pattId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="piId", nullable=false, foreignKey=@ForeignKey(name="fk_patt_pi"))
  private PlanInvoiceEntity invoice;

  @Column(nullable=false) private LocalDateTime pattAt = LocalDateTime.now();

  @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
  private PattResult pattResult;

  @Column(length=500) private String pattFail;
  @Column(length=200) private String pattUid;
  @Column(length=500) private String pattUrl;

  @Lob private String pattResponse; // 원문 JSON
}
