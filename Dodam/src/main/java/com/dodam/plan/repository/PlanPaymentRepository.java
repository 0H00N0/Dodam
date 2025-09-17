// src/main/java/com/dodam/plan/repository/PlanPaymentRepository.java
package com.dodam.plan.repository;

import com.dodam.member.entity.MemberEntity;                 // 프로젝트 경로 유지
import com.dodam.plan.Entity.PlanPaymentEntity;              // ⚠ 실제 패키지/대소문자에 맞춤 (Entity가 대문자면 그대로)
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanPaymentRepository extends JpaRepository<PlanPaymentEntity, Long> {

    /* ─────────────────────
     * mid(문자열 컬럼) 기반 파생 쿼리 (JPQL @Query 없이 안전)
     * ───────────────────── */
	
	List<PlanPaymentEntity> findAllByMid(String mid);

    boolean existsByMidAndPayKey(String mid, String payKey);

    // 정렬된 전체 목록
    List<PlanPaymentEntity> findByMidOrderByPayIdDesc(String mid);

    // 네 코드에서 사용 중인 "Top" 파생 쿼리 2종 모두 지원
    Optional<PlanPaymentEntity> findTop1ByMidOrderByPayIdDesc(String mid);
    Optional<PlanPaymentEntity> findTopByMidOrderByPayIdDesc(String mid);

    // 키로 단건 조회
    Optional<PlanPaymentEntity> findByPayKey(String payKey);

    Optional<PlanPaymentEntity> findByMidAndPayKey(String mid, String payKey);

    // 과거 시그니처 호환: MemberEntity + payKey
    default Optional<PlanPaymentEntity> findByMemberAndPayKey(MemberEntity member, String payKey) {
        // MemberEntity의 아이디 게터명이 mid가 아니라면 getMid()를 실제 게터로 바꾸세요.
        return findByMidAndPayKey(member.getMid(), payKey);
    }

    // ✅ 네 코드 호환용: findByMemberMidAndPayKey(...)가 필요한 곳이 있으므로, 안전 위임 제공
    default Optional<PlanPaymentEntity> findByMemberMidAndPayKey(String mid, String payKey) {
        return findByMidAndPayKey(mid, payKey);
    }

    // ✅ 네 코드 호환용: findByMid(...) 호출을 그대로 살리기 위해 위임 제공
    default List<PlanPaymentEntity> findByMid(String mid) {
        return findByMidOrderByPayIdDesc(mid);
    }

    /**
     * 컨트롤러/서비스가 호출하는 “기본 결제수단” 통일 API.
     * - defaultYn = 'Y' 또는 isDefault = true 를 우선 반환
     * - 없으면 최신 카드(리스트 첫 번째) 반환
     */
    default Optional<PlanPaymentEntity> findDefaultByMember(String mid) {
        List<PlanPaymentEntity> list = findByMidOrderByPayIdDesc(mid);
        if (list == null || list.isEmpty()) return Optional.empty();

        // 1) defaultYn = 'Y' 우선
        for (PlanPaymentEntity p : list) {
            try {
                var m = PlanPaymentEntity.class.getMethod("getDefaultYn");
                Object v = m.invoke(p);
                if (v instanceof String s && "Y".equalsIgnoreCase(s)) return Optional.of(p);
            } catch (Throwable ignore) { /* 필드/메서드 없을 수 있음 */ }
        }

        // 2) isDefault = true
        for (PlanPaymentEntity p : list) {
            try {
                var m = PlanPaymentEntity.class.getMethod("isDefault");
                Object v = m.invoke(p);
                if (v instanceof Boolean b && b) return Optional.of(p);
            } catch (Throwable ignore) { /* 필드/메서드 없을 수 있음 */ }
        }

        // 3) fallback: 가장 최근
        return Optional.of(list.get(0));
    }
}
