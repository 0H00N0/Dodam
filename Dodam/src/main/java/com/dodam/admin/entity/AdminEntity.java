package com.dodam.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 관리자 정보를 나타내는 엔티티 클래스입니다.
 * 데이터베이스의 'admin' 테이블과 매핑되며, Spring Security의 UserDetails 인터페이스를 구현하여 인증에 사용됩니다.
 */
@Entity
@Table(name = "admin")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminEntity implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * 엔티티가 영속화되기 전에 호출되어 생성일과 수정일을 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 엔티티가 업데이트되기 전에 호출되어 수정일을 설정합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Spring Security에서 사용자의 권한 목록을 반환합니다.
     * @return 사용자의 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    /**
     * 계정 만료 여부를 반환합니다. (항상 true)
     * @return 계정 만료 여부
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    /**
     * 계정 잠금 여부를 반환합니다. (항상 true)
     * @return 계정 잠금 여부
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    /**
     * 자격 증명(비밀번호) 만료 여부를 반환합니다. (항상 true)
     * @return 자격 증명 만료 여부
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    /**
     * 계정 활성화 여부를 반환합니다.
     * @return 계정 활성화 여부
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 관리자 역할을 정의하는 Enum입니다.
     */
    public enum AdminRole {
    	 SUPER_ADMIN,  // 최고 관리자
         ADMIN,        // 일반 관리자
         STAFF,        // 직원
         DELIVERYMAN   // 배달기사
    }
}