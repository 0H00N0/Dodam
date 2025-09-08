package com.dodam.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.dodam.member.entity.LoginmethodEntity;

@Repository
public interface LoginmethodRepository extends JpaRepository<LoginmethodEntity, Long> {
    Optional<LoginmethodEntity> findByLmtype(String lmtype);
}
