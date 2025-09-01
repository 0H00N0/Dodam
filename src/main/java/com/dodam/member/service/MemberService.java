package com.dodam.member.service;

import org.springframework.stereotype.Service;
import com.dodam.member.dto.MemberDTO;
import com.dodam.member.entity.*;
import com.dodam.member.repository.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepo;
    private final MemtypeRepository memtypeRepo;
    private final LoginmethodRepository loginRepo;

    /** 회원가입: 기본 memtype=0(일반), loginmethod=local */
    public void signup(MemberDTO dto){
        if (memberRepo.existsByMid(dto.getMid()))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        if (dto.getMemail() != null && memberRepo.existsByMemail(dto.getMemail()))
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");

        MemtypeEntity role = memtypeRepo.findById(0L)
            .orElseThrow(() -> new IllegalStateException("memtype(0=일반) 시드가 없습니다."));
        LoginmethodEntity local = loginRepo.findByLmtype("local")
            .orElseThrow(() -> new IllegalStateException("loginmethod(local) 시드가 없습니다."));

        MemberEntity e = MemberDTO.toEntity(dto);
        e.setMpw(dto.getMpw()); // Security 미적용 → 평문 저장
        e.setMemtype(role);
        e.setLoginmethod(local);

        memberRepo.save(e);
    }

    /** 로그인 검증 (Security 제외, 단순 equals 비교) */
    public boolean loginCheck(String mid, String rawPw){
        return memberRepo.findByMid(mid)
                .map(e -> rawPw.equals(e.getMpw()))
                .orElse(false);
    }

    public MemberDTO readByMid(String mid){
        return memberRepo.findByMid(mid).map(MemberDTO::new).orElse(null);
    }

    /** 관리자용: 모든 회원 조회 */
    public List<MemberDTO> findAll(){
        return memberRepo.findAll()
                .stream()
                .map(MemberDTO::new)
                .collect(Collectors.toList());
    }

    /** 관리자용: ID로 회원 조회 */
    public MemberDTO findById(Long id){
        return memberRepo.findById(id).map(MemberDTO::new).orElse(null);
    }

    /** 관리자용: 회원 삭제 */
    public void deleteById(Long id){
        memberRepo.deleteById(id);
    }
    public MemberEntity authenticate(String username, String password) {
        System.out.println("인증 시작: " + username + " / " + password);
        
        // 붉은색으로 변경된 부분은 변수 이름과 필드 접근 방식을 수정한 부분입니다.
        MemberEntity member = memberRepo.findByMid(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        System.out.println("사용자 찾음: " + member.getMid() + ", MTYPE: " + member.getMemtype().getRoleName());
        
        // 관리자 권한 확인
        if (!"ADMIN".equals(member.getMemtype().getRoleName())) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }
        
        // 비밀번호 검증
        if (!password.equals(member.getMpw())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        System.out.println("인증 성공: " + member.getMid() + ", 권한: " + member.getMemtype().getRoleName());
        return member;
    }
}

