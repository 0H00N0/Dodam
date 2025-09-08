package com.dodam.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



import com.dodam.member.entity.*;
@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByMid(String mid);
    boolean existsByMid(String mid);
    boolean existsByMemail(String memail);

    // 관리자만 조회 (ADMIN, SUPERADMIN, STAFF)
    @Query("""
           SELECT m
           FROM MemberEntity m
           JOIN m.memtype t
           WHERE m.mid = :mid
             AND t.mtname IN ('ADMIN','SUPERADMIN','STAFF')
           """)
    Optional<MemberEntity> findAdminByMid(@Param("mid") String mid);

    Optional<MemberEntity> findByMnameAndMtel(String mname, String mtel);
    Optional<MemberEntity> findByMnameAndMemail(String mname, String memail);
}
