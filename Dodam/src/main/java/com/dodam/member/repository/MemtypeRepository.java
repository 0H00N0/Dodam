package com.dodam.member.repository;

<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;
import com.dodam.member.entity.MemtypeEntity;

import java.util.Optional;

public interface MemtypeRepository extends JpaRepository<MemtypeEntity, Long> {
    Optional<MemtypeEntity> findByRoleName(String roleName);
=======
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dodam.member.entity.MemtypeEntity;

public interface MemtypeRepository extends JpaRepository<MemtypeEntity, Long> {
    Optional<MemtypeEntity> findByMtcode(Integer mtcode);
>>>>>>> origin/chan
}
