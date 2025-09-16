// src/main/java/com/dodam/plan/repository/PlanPaymentRepository.java
package com.dodam.plan.repository;

import com.dodam.member.entity.MemberEntity;         // 프로젝트 경로 그대로 유지
import com.dodam.plan.Entity.PlanPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlanPaymentRepository extends JpaRepository<PlanPaymentEntity, Long> {

    // ✅ 목록: PlanPaymentEntity에 있는 실제 문자열 컬럼명으로 맞추세요.
    // 엔티티에 'mid'가 아니라면 p.mid 를 p.memberId / p.userId 등 실제 명칭으로 변경
    @Query("select p from PlanPaymentEntity p where p.mid = :mid order by p.payId desc")
    List<PlanPaymentEntity> findListByMid(@Param("mid") String mid);

    // 과거 코드 호환: findByMid(...)를 호출해도 위 JPQL로 우회
    default List<PlanPaymentEntity> findByMid(String mid) {
        return findListByMid(mid);
    }

    // 혹시 컨트롤러/서비스 어딘가에서 아직 이 이름을 쓰고 있으면 안전하게 우회
    default List<PlanPaymentEntity> findByMemberMid(String mid) {
        return findListByMid(mid);
    }
    
    Optional<PlanPaymentEntity> findByPayKey(String payKey);

    Optional<PlanPaymentEntity> findByMidAndPayKey(String mid, String payKey);

    // 과거 시그니처 호환: 스프링이 쿼리 생성하지 않도록 default 본문에서 위 메서드로 위임
    default Optional<PlanPaymentEntity> findByMemberAndPayKey(MemberEntity member, String payKey) {
        // MemberEntity의 식별자 getter 이름이 다르면 getMid() 부분을 실제 이름으로 수정
        return findByMidAndPayKey(member.getMid(), payKey);
    }

    // 파생 쿼리는 mid 필드가 실제로 있을 때만 유효합니다 (있다면 그대로 두셔도 OK)
    Optional<PlanPaymentEntity> findTop1ByMidOrderByPayIdDesc(String mid);
    Optional<PlanPaymentEntity> findTopByMidOrderByPayIdDesc(String mid);
}
