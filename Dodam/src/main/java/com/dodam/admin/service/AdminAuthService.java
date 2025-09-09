package com.dodam.admin.service;

import com.dodam.admin.dto.AdminLoginRequest;
import com.dodam.member.entity.MemberEntity;
import com.dodam.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor-based dependency injection
    public AdminAuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Admin login method.
     * @param request DTO containing admin ID and password.
     * @return The authenticated MemberEntity.
     */
    public MemberEntity login(AdminLoginRequest request) {
        // 1. Find admin by ID using the custom query in MemberRepository
        // This query ensures that only users with roles 'ADMIN', 'SUPERADMIN', or 'STAFF' can log in.
        MemberEntity admin = memberRepository.findAdminByMid(request.getMid())
                .orElseThrow(() -> new EntityNotFoundException("Admin account not found or you do not have admin privileges."));

        // 2. Verify the password
        if (!passwordEncoder.matches(request.getMpw(), admin.getMpw())) {
            throw new IllegalArgumentException("Invalid password.");
        }

        // 3. Return the entity if authentication is successful
        return admin;
    }
}
