package com.dodam.admin.controller;

import com.dodam.admin.service.AdminMemberService; // ✅ 새로 만든 서비스를 import
import com.dodam.member.dto.MemberResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/members")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminMemberController {

    // ✅ 의존성을 MemberService -> AdminMemberService로 변경
    private final AdminMemberService adminMemberService;

    // ✅ 생성자도 AdminMemberService를 주입받도록 변경
    public AdminMemberController(AdminMemberService adminMemberService) {
        this.adminMemberService = adminMemberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> memberList() {
        // ✅ 호출하는 메서드 변경: findAll() -> findAllMembers()
        return ResponseEntity.ok(adminMemberService.findAllMembers().stream()
                .map(MemberResponseDTO::fromEntity)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDTO> memberView(@PathVariable("id") Long id) {
        // ✅ 호출하는 메서드 변경: findById() -> findMemberById()
        return adminMemberService.findMemberById(id)
                .map(MemberResponseDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable("id") Long id) {
        // ✅ 호출하는 메서드 변경: deleteById() -> deleteMemberById()
        adminMemberService.deleteMemberById(id);
        return ResponseEntity.noContent().build();
    }
}