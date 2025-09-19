package com.dodam.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dodam.member.entity.MemtypeEntity;

@Repository
public interface MemtypeRepository extends JpaRepository<MemtypeEntity, Long> {
    Optional<MemtypeEntity> findByMtcode(Integer mtcode);
    Optional<MemtypeEntity> findByMtname(String mtname);
}
