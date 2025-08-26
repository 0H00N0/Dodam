package com.dodam.admin.repository;

import com.dodam.admin.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // username 기반 조회
    Optional<UserEntity> findByUsername(String username);

    // username + role 조회
    Optional<UserEntity> findByUsernameAndRole(String username, UserEntity.UserRole role);

    // email 기반 조회
    Optional<UserEntity> findByEmail(String email);

    // 중복 체크
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // 로그인 시간 업데이트
    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.lastLoginAt = :lastLoginAt WHERE u.username = :username")
    void updateLastLoginTime(@Param("username") String username, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
