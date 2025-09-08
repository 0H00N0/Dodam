package com.dodam.admin.service;

import com.dodam.member.entity.MemberEntity;
import com.dodam.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 관리자 페이지의 회원 관리 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정, 데이터 변경이 필요한 메서드에 @Transactional 추가
public class AdminMemberService {

    private final MemberRepository memberRepo;

    /**
     * 모든 회원 목록을 조회합니다. (Entity를 그대로 반환)
     * @return MemberEntity 리스트
     */
    public List<MemberEntity> findAllMembers() {
        return memberRepo.findAll();
    }

    /**
     * 고유 ID로 특정 회원을 조회합니다. (Entity를 그대로 반환)
     * @param id 회원 ID
     * @return Optional<MemberEntity>
     */
    public Optional<MemberEntity> findMemberById(Long id) {
        return memberRepo.findById(id);
    }

    /**
     * 고유 ID로 특정 회원을 삭제합니다.
     * @param id 회원 ID
     */
    @Transactional // 데이터에 변경이 발생하므로 @Transactional 어노테이션 추가
    public void deleteMemberById(Long id) {
        memberRepo.deleteById(id);
    }
}
