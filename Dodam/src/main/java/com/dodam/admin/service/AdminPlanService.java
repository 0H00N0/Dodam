package com.dodam.admin.service;

import com.dodam.admin.dto.AdminPlanDto;
import com.dodam.plan.Entity.PlanNameEntity;
import com.dodam.plan.Entity.PlansEntity;
import com.dodam.plan.repository.PlanNameRepository;
import com.dodam.plan.repository.PlansRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPlanService {

    private final PlansRepository plansRepository;
    private final PlanNameRepository planNameRepository;

    @Transactional
    public AdminPlanDto.Response createPlan(AdminPlanDto.CreateRequest requestDto) {
        PlanNameEntity planNameEntity = planNameRepository.findById(requestDto.getPlanNameId())
                .orElseThrow(() -> new EntityNotFoundException("PlanName not found with id: " + requestDto.getPlanNameId()));

        PlansEntity newPlan = requestDto.toEntity(planNameEntity);
        PlansEntity savedPlan = plansRepository.save(newPlan);
        return AdminPlanDto.Response.fromEntity(savedPlan);
    }

    public List<AdminPlanDto.Response> getAllPlans() {
        return plansRepository.findAll().stream()
                .map(AdminPlanDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public AdminPlanDto.Response getPlan(Long planId) {
        PlansEntity plan = plansRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + planId));
        return AdminPlanDto.Response.fromEntity(plan);
    }

    @Transactional
    public AdminPlanDto.Response updatePlan(Long planId, AdminPlanDto.UpdateRequest requestDto) {
        PlansEntity existingPlan = plansRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + planId));

        PlanNameEntity planNameEntity = planNameRepository.findById(requestDto.getPlanNameId())
                .orElseThrow(() -> new EntityNotFoundException("PlanName not found with id: " + requestDto.getPlanNameId()));

        existingPlan.setPlanName(planNameEntity);
        existingPlan.setPlanCode(requestDto.getPlanCode());
        existingPlan.setPlanActive(requestDto.getPlanActive());

        return AdminPlanDto.Response.fromEntity(existingPlan);
    }

    @Transactional
    public void deletePlan(Long planId) {
        if (!plansRepository.existsById(planId)) {
            throw new EntityNotFoundException("Plan not found with id: " + planId);
        }
        plansRepository.deleteById(planId);
    }
}
