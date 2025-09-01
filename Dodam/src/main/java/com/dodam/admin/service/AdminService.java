package com.dodam.admin.service;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.dodam.member.entity.MemberEntity;
import com.dodam.member.repository.MemberRepository;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;

    /**
     * Member 테이블에서 관리자 인증
     */
    public MemberEntity authenticate(String username, String password) {
        System.out.println("인증 시작: " + username + " / " + password);
        
        try {
            // 먼저 일반 조회로 사용자 존재 여부 확인
            System.out.println("1. 일반 사용자 조회 시작...");
            Optional<MemberEntity> generalUser = memberRepository.findByMid(username);
            if (generalUser.isPresent()) {
                MemberEntity user = generalUser.get();
                System.out.println("일반 사용자 찾음: " + user.getMid() + ", MEMTYPE: " + user.getMemtype().getRoleName());
            } else {
                System.out.println("일반 사용자 조회 실패: " + username + " 계정이 DB에 없음");
            }
            
            // Member 테이블에서 관리자 권한이 있는 사용자만 조회
            System.out.println("2. 관리자 권한 조회 시작...");
            MemberEntity member = memberRepository.findAdminByMid(username)
                    .orElseThrow(() -> new RuntimeException("관리자 권한이 없거나 존재하지 않는 사용자입니다."));

            System.out.println("사용자 찾음: " + member.getMid() + ", MTYPE: " + member.getMemtype());

            // 비밀번호 검증
            if (!password.equals(member.getMpw())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }

            System.out.println("인증 성공: " + member.getMid() + ", 권한: " + member.getRoleString());
            return member;
        } catch (Exception e) {
            System.out.println("인증 실패: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 사용자명으로 회원 조회
     */
    public MemberEntity findByUsername(String username) {
        return memberRepository.findByMid(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}