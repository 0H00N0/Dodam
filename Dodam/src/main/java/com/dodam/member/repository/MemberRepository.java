package com.dodam.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.dodam.member.entity.MemberEntity;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByMid(String mid);
    boolean existsByMid(String mid);


}
