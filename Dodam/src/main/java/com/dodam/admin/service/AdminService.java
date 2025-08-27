package com.dodam.admin.service;
import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminEntity admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + username));

        // 마지막 로그인 시간 업데이트
        adminRepository.updateLastLoginTime(username, LocalDateTime.now());
        return admin;
    }
    public AdminEntity authenticate(String username, String password) {
        AdminEntity admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        if (!admin.isEnabled()) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        if (passwordEncoder.matches(password, admin.getPassword())) {
            adminRepository.updateLastLoginTime(admin.getUsername(), LocalDateTime.now());
            return admin;
        } else {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }
    public AdminEntity authenticate(String username, String password, AdminEntity.AdminRole role) {
        AdminEntity admin = adminRepository.findByUsernameAndRole(username, role)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없거나 역할이 올바르지 않습니다."));

        if (!admin.isEnabled()) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        if (passwordEncoder.matches(password, admin.getPassword())) {
            adminRepository.updateLastLoginTime(admin.getUsername(), LocalDateTime.now());
            return admin;
        } else {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }
    public AdminEntity createAdmin(AdminEntity admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setCreatedAt(LocalDateTime.now());
        return adminRepository.save(admin);
    }
    @Transactional(readOnly = true)
    public AdminEntity findByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다."));
    }
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    public void changePassword(String username, String newPassword) {
        AdminEntity admin = findByUsername(username);
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }
}
