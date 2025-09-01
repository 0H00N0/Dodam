package com.dodam.admin.service;

import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final AdminRepository adminRepository;
    
    /**
     * DB에서 사용자 인증
     */
    public AdminEntity authenticate(String username, String password) {
        // DB에서 사용자명으로 조회
        AdminEntity admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        // 활성화 상태 확인
        if (!admin.getEnabled()) {
            throw new RuntimeException("비활성화된 계정입니다.");
        }
        
        // 비밀번호 검증 (평문 비교)
        if (!password.equals(admin.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        return admin;
    }
}