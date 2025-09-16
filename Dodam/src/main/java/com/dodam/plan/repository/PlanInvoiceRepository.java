// src/main/java/com/dodam/plan/repository/PlanInvoiceRepository.java
package com.dodam.plan.repository;

import com.dodam.plan.Entity.PlanInvoiceEntity;
import com.dodam.plan.enums.PlanEnums.PiStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PlanInvoiceRepository extends JpaRepository<PlanInvoiceEntity, Long> {

    /**
     * 최근 생성된 동일 사용자(mid), 금액, 통화, 상태(PENDING) 인보이스 조회
     * - 시간 범위를 지정해서 멱등 보장 (예: 최근 10분)
     * - PlanInvoiceEntity → PlanMember → Member → mid 경로를 통해 접근
     */
    @Query("""
        select i from PlanInvoiceEntity i
        where i.planMember.member.mid = :mid
          and i.piStat = :stat
          and i.piAmount = :amount
          and i.piCurr = :curr
          and i.piStart between :from and :to
        order by i.piId desc
    """)
    Optional<PlanInvoiceEntity> findRecentPendingSameAmount(
            @Param("mid") String mid,
            @Param("stat") PiStatus stat,
            @Param("amount") BigDecimal amount,
            @Param("curr") String curr,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
    
    @Query("select i from PlanInvoiceEntity i where i.piUid = :uid")
    Optional<PlanInvoiceEntity> findByPiUid(@Param("uid") String uid);
}
