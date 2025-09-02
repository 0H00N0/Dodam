package com.dodam.member.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dodam.member.dto.MemberDTO;
import com.dodam.member.entity.LoginmethodEntity;
import com.dodam.member.entity.MemberEntity;
import com.dodam.member.entity.MemtypeEntity;
import com.dodam.member.repository.LoginmethodRepository;
import com.dodam.member.repository.MemberRepository;
import com.dodam.member.repository.MemtypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepo;
    private final LoginmethodRepository loginRepo;  
    private final MemtypeRepository memtypeRepo;

    // signup(MemberDTO dto)
    /** 회원가입: 기본 memtype=0(일반), loginmethod=local */
    @Transactional
    public void signup(MemberDTO dto) {
        if (isBlank(dto.getMid()) || isBlank(dto.getMpw()) || isBlank(dto.getMname()) || isBlank(dto.getMtel())) {
            throw new IllegalArgumentException("필수 입력 누락(mid/mpw/mname/mtel)");
        }
        if (memberRepo.existsByMid(dto.getMid())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        // 기본값(프론트가 안 주는 필드 보강)
        if (dto.getMaddr() == null) dto.setMaddr("");
        if (dto.getMpost() == null) dto.setMpost(0L);
        if (dto.getMbirth() == null) dto.setMbirth(LocalDate.of(2000,1,1));

        // FK 로딩(없으면 생성)
        String loginType = (dto.getJoinWay() != null) ? dto.getJoinWay() : "LOCAL";
        var login = loginRepo.findByLmtype(loginType)
                     .orElseGet(() -> loginRepo.save(LoginmethodEntity.builder().lmtype(loginType).build()));

        int code = (dto.getRoleCode() != null) ? dto.getRoleCode().intValue() : 0;
        var memtype = memtypeRepo.findByMtcode(code)
                      .orElseGet(() -> memtypeRepo.save(
                          MemtypeEntity.builder().mtcode(code)
                            .mtname(switch(code){ case 1->"SuperAdmin"; case 2->"Staff"; case 3->"Deliveryman"; default->"일반"; })
                            .build()
                      ));

        // 엔티티 변환 + FK 주입
        var entity = MemberDTO.toEntity(dto);
        entity.setLoginmethod(login);
        entity.setMemtype(memtype);

        // (선택) BCrypt 적용 시:
        // entity.setMpw(encoder.encode(dto.getMpw()));

        memberRepo.save(entity);
    }
    
 // 로그인 검증: 아이디로 조회 후 비밀번호 비교 (개발용: 평문 비교)
    @Transactional(readOnly = true)
    public boolean loginCheck(String mid, String rawPw) {
        if (mid == null || rawPw == null) return false;
        String m = mid.trim();
        String p = rawPw.trim();
        return memberRepo.findByMid(m)
                .map(e -> e.getMpw().equals(p))   // 실서비스는 BCrypt.matches(p, e.getMpw())
                .orElse(false);
    }

    // 세션 정보 세팅용: 아이디로 조회해 DTO로 변환해 전달
    @Transactional(readOnly = true)
    public MemberDTO readByMid(String mid) {
        if (mid == null) return null;
        return memberRepo.findByMid(mid.trim())
                .map(MemberDTO::new)   // MemberDTO(MemberEntity e) 생성자 사용
                .orElse(null);
    }


    // 로그인 검증 등 다른 메서드가 있다면 여기에…
    // @Transactional(readOnly = true)
    // public boolean loginCheck(String mid, String rawPw) { ... }

    // 헬퍼
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    
 // 비밀번호 변경
    public void updatePw(String mid, String currentPw, String newPw) {
        MemberEntity member = memberRepo.findByMid(mid)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 현재 비밀번호 확인
        if (!currentPw.equals(member.getMpw())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 저장 
        member.setMpw(newPw);
        memberRepo.save(member);
    }
}
