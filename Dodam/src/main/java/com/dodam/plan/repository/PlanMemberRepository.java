package com.dodam.plan.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dodam.plan.Entity.PlanMember;
import com.dodam.plan.enums.PlanEnums.PmStatus;

public interface PlanMemberRepository extends JpaRepository<PlanMember, Long> {
	  List<PlanMember> findByMember_MnumAndPmStat(Long mnum, PmStatus pmStat);
	  List<PlanMember> findByPmNextBilBeforeAndPmStat(LocalDateTime now, PmStatus pmStat);
	  Optional<PlanMember> findFirstByMember_MnumAndPmStat(Long mnum, PmStatus pmStat);
	}
