package com.dodam.plan.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dodam.plan.Entity.PlanPriceEntity;

public interface PlanPriceRepository extends JpaRepository<PlanPriceEntity, Long> {
	List<PlanPriceEntity> findByPlan_PlanIdAndPpriceActiveTrue(Long planId);

	Optional<PlanPriceEntity> findByPlan_PlanIdAndPterm_PtermIdAndPpriceBilModeAndPpriceActiveTrue(Long planId,
			Long ptermId, String bilMode);

	// ✅ 테이블/컬럼명은 실제 스키마에 맞게 필요 시 조정
	// - PhysicalNamingStrategyStandardImpl를 쓰면 @Table(name="PlanPrice") 기준으로
	// "PlanPrice"
	// - plan FK 컬럼은 보통 "plan_id" 혹은 "PlanId"
	// - 기간 컬럼은 "months"가 아니라면 그대로 실제 컬럼명으로 바꿔도 WHERE 절은 동작함
	@Query(value = """
			SELECT *
			  FROM PlanPrice
			 WHERE (plan_id = :planId OR PlanId = :planId)
			   AND (months = :months OR PpriceMonths = :months OR PPRICE_MONTHS = :months)
			 FETCH FIRST 1 ROWS ONLY
			""", nativeQuery = true)
	Optional<PlanPriceEntity> findByPlanIdAndMonths(@Param("planId") Long planId, @Param("months") int months);

	// ── 1) 불리언 직비교 버전 ─────────────────────────────
    @Query("""
        select p
          from PlanPriceEntity p
          join fetch p.planTerms t
         where p.plan.planId = :planId
           and p.ppriceActive = true
    """)
    List<PlanPriceEntity> findActiveWithTerms(@Param("planId") Long planId);

    // ── 2) 파라미터 버전(원하면 같이 둬도 됨) ──────────────
    @Query("""
        select p
          from PlanPriceEntity p
          join fetch p.planTerms t
         where p.plan.planId = :planId
           and p.ppriceActive = :active
    """)
    List<PlanPriceEntity> findWithTermsByPlanAndActive(@Param("planId") Long planId,
                                                       @Param("active") Boolean active);
}
