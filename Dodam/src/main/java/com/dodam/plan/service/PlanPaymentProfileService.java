package com.dodam.plan.service;

import org.springframework.stereotype.Service;

import com.dodam.member.repository.MemberRepository;
import com.dodam.plan.Entity.PlanPaymentEntity;
import com.dodam.plan.repository.PlanPaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanPaymentProfileService {
	private final PlanPaymentRepository paymentRepo;
	private final MemberRepository memberRepo;
	
	@Transactional
	public PlanPaymentEntity upsert(Long mnum, String customerId, String pg, String brand, String bin, String last4) {
		var member = memberRepo.findById(mnum).orElseThrow();
		
		PlanPaymentEntity pp = paymentRepo.existsByMember_MnumAndPayCustomer(mnum, customerId)
				? paymentRepo.findTopByMember_MnumAndPayCustomer(mnum, customerId)
						: PlanPaymentEntity.builder().member(member).payCustomer(customerId).build();
		pp.setPayPg(pg);
		pp.setPayBrand(brand);
		pp.setPayBin(bin);
		pp.setPayLast4(last4);
		pp.setPayActive(true);
		return paymentRepo.save(pp);
	}
}
