package com.dodam.plan.repository;

import com.dodam.plan.Entity.PlanPaymentEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanPaymentRepository extends JpaRepository<PlanPaymentEntity, Long> {

    // 회원번호로 결제목록 조회
    @Query("""
           select p
           from PlanPaymentEntity p, PlanMember pm
           where pm.payment = p
             and pm.member.mnum = :mnum
           order by p.payId desc
           """)
    List<PlanPaymentEntity> findByMemberMnum(@Param("mnum") Long mnum);

    // 결제ID + 회원번호로 단건 조회
    @Query("""
           select p
           from PlanPaymentEntity p, PlanMember pm
           where pm.payment = p
             and p.payId = :payId
             and pm.member.mnum = :mnum
           """)
    Optional<PlanPaymentEntity> findByPayIdAndMemberMnum(@Param("payId") Long payId,
                                                         @Param("mnum") Long mnum);
    
 // 존재 여부 (member.mnum + payCustomer)
    @Query("""
           select case when count(p) > 0 then true else false end
           from PlanPaymentEntity p
             join PlanMember pm on pm.payment = p
           where pm.member.mnum = :mnum
             and p.payCustomer   = :customerId
           """)
    boolean existsByMember_MnumAndPayCustomer(@Param("mnum") Long mnum,
                                              @Param("customerId") String customerId);

    // 최신 1건 반환 (정렬 기준: payId desc)
    @Query("""
           select p
           from PlanPaymentEntity p
             join PlanMember pm on pm.payment = p
           where pm.member.mnum = :mnum
             and p.payCustomer   = :customerId
           order by p.payId desc
           """)
    PlanPaymentEntity findTopByMember_MnumAndPayCustomer(@Param("mnum") Long mnum,
                                                         @Param("customerId") String customerId);
}
