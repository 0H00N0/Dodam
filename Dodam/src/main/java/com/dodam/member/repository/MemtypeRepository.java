package com.dodam.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dodam.member.entity.MemtypeEntity;

public interface MemtypeRepository extends JpaRepository<MemtypeEntity, Long> { }
