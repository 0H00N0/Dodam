package com.dodam.plan.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.ForeignKey;

@Entity
@Table(name = "PlanPrice",
		uniqueConstraints = @UniqueConstraint(name = "uk_planPrice_plan_term_mode", columnNames = {"planId", "ptermId", "ppriceBilMode"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanPriceEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ppriceId;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "planId", nullable = false,
      			foreignKey = @ForeignKey(name = "fk_planPrice_plan"))
	private PlansEntity plan;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "ptermId", nullable = false,
    			foreignKey = @ForeignKey(name = "fk_planPrice_planTerms"))
	private PlanTermsEntity pterm;
	
	@Column(nullable = false, length = 20)
	private String ppriceBilMode;
	
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal ppriceAmount;
	
	@Column(nullable = false, length = 3)
	private String ppriceCurr;
	
	@Column(nullable = false)
	private Boolean ppriceActive;
	
	@CreationTimestamp
	@Column(nullable = false)
	private LocalDateTime ppriceCreate;
}
