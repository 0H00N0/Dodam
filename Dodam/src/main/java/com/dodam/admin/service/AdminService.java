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

/**
 * 관리자 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * Spring Security의 UserDetailsService를 구현하여 사용자 인증에 사용됩니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 이름(username)을 기반으로 사용자 상세 정보를 로드합니다.
     * Spring Security의 인증 과정에서 호출됩니다.
     * @param username 로드할 사용자의 이름
     * @return 로드된 사용자 상세 정보 (AdminEntity 객체)
     * @throws UsernameNotFoundException 해당 사용자 이름의 관리자를 찾을 수 없을 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminEntity admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다: " + username));

        // 마지막 로그인 시간 업데이트
        adminRepository.updateLastLoginTime(username, LocalDateTime.now());
        return admin;
    }

    /**
     * 주어진 사용자 이름, 비밀번호, 역할로 관리자를 인증합니다.
     * @param username 인증할 사용자의 이름
     * @param password 인증할 사용자의 비밀번호 (평문)
     * @param role 인증할 사용자의 역할
     * @return 인증된 AdminEntity 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없거나 역할이 올바르지 않을 경우 발생
     * @throws IllegalArgumentException 비밀번호가 일치하지 않을 경우 발생
     */
    public AdminEntity authenticate(String username, String password, AdminEntity.AdminRole role) {
        AdminEntity admin = adminRepository.findByUsernameAndRole(username, role)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없거나 역할이 올바르지 않습니다."));

        if (passwordEncoder.matches(password, admin.getPassword())) {
            // 마지막 로그인 시간 업데이트
            adminRepository.updateLastLoginTime(admin.getUsername(), LocalDateTime.now());
            return admin;
        } else {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 새로운 관리자 계정을 생성합니다.
     * 비밀번호는 저장하기 전에 인코딩됩니다.
     * @param admin 생성할 AdminEntity 객체
     * @return 저장된 AdminEntity 객체
     */
    public AdminEntity createAdmin(AdminEntity admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setCreatedAt(LocalDateTime.now());
        return adminRepository.save(admin);
    }

    /**
     * 사용자 이름으로 관리자를 조회합니다.
     * @param username 조회할 관리자의 이름
     * @return 조회된 AdminEntity 객체
     * @throws UsernameNotFoundException 관리자를 찾을 수 없을 경우 발생
     */
    @Transactional(readOnly = true)
    public AdminEntity findByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자를 찾을 수 없습니다."));
    }

    /**
     * 평문 비밀번호와 인코딩된 비밀번호가 일치하는지 확인합니다.
     * @param rawPassword 평문 비밀번호
     * @param encodedPassword 인코딩된 비밀번호
     * @return 비밀번호 일치 여부
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 관리자의 비밀번호를 변경합니다.
     * @param username 비밀번호를 변경할 관리자의 이름
     * @param newPassword 새로운 비밀번호 (평문)
     */
    public void changePassword(String username, String newPassword) {
        AdminEntity admin = findByUsername(username);
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepository.save(admin);
    }

    /**
     * 주어진 사용자 이름의 관리자가 존재하는지 확인합니다.
     * @param username 확인할 사용자의 이름
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }
}
