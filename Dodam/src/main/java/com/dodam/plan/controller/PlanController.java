package com.dodam.plan.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dodam.plan.service.PlanService;
import com.dodam.plan.dto.PlanDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PlanController {
	private final PlanService ps;
	
	@GetMapping
	public List<PlanDTO> list(){
		return ps.getActivePlans();
	}
	
	@GetMapping("/{planCode}")
	public PlanDTO detail(@PathVariable String planCode) {
		return ps.getByCode(planCode);
	}
}
