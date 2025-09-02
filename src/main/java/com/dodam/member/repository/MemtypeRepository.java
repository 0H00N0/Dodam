package com.dodam.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dodam.member.entity.MemtypeEntity;

import java.util.Optional;

public interface MemtypeRepository extends JpaRepository<MemtypeEntity, Long> {
    Optional<MemtypeEntity> findByRoleName(String roleName);
}
