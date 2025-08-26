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

/**
 * AdminEntity에 대한 데이터베이스 접근을 처리하는 JpaRepository 인터페이스입니다.
 * Spring Data JPA의 기능을 활용하여 관리자 데이터를 조회, 저장, 업데이트, 삭제합니다.
 */
@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {

    /**
     * 사용자 이름(username)으로 AdminEntity를 조회합니다.
     * @param username 조회할 관리자의 사용자 이름
     * @return Optional<AdminEntity> 관리자 엔티티 (존재하지 않을 경우 Optional.empty())
     */
    Optional<AdminEntity> findByUsername(String username);

    /**
     * 사용자 이름(username)과 역할(role)로 AdminEntity를 조회합니다.
     * @param username 조회할 관리자의 사용자 이름
     * @param role 조회할 관리자의 역할
     * @return Optional<AdminEntity> 관리자 엔티티 (존재하지 않을 경우 Optional.empty())
     */
    Optional<AdminEntity> findByUsernameAndRole(String username, AdminEntity.AdminRole role);

    /**
     * 이메일(email)로 AdminEntity를 조회합니다.
     * @param email 조회할 관리자의 이메일
     * @return Optional<AdminEntity> 관리자 엔티티 (존재하지 않을 경우 Optional.empty())
     */
    Optional<AdminEntity> findByEmail(String email);

    /**
     * 주어진 사용자 이름(username)을 가진 관리자가 존재하는지 확인합니다.
     * @param username 확인할 사용자 이름
     * @return boolean 존재 여부
     */
    boolean existsByUsername(String username);

    /**
     * 주어진 이메일(email)을 가진 관리자가 존재하는지 확인합니다.
     * @param email 확인할 이메일
     * @return boolean 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 특정 관리자의 마지막 로그인 시간을 업데이트합니다.
     * @param username 마지막 로그인 시간을 업데이트할 관리자의 사용자 이름
     * @param lastLoginAt 업데이트할 마지막 로그인 시간
     */
    @Modifying
    @Transactional
    @Query("UPDATE AdminEntity a SET a.lastLoginAt = :lastLoginAt WHERE a.username = :username")
    void updateLastLoginTime(@Param("username") String username, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}