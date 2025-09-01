package com.dodam.member.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.dodam.member.dto.MemberDTO;
import com.dodam.member.entity.*;
import com.dodam.member.repository.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepo;
    private final MemtypeRepository memtypeRepo;
    private final LoginmethodRepository loginRepo;
    private final PasswordEncoder encoder;

    /** 회원가입: 기본 memtype=0(일반), loginmethod=local */
    public void signup(MemberDTO dto){
        if (memberRepo.existsByMid(dto.getMid()))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        if (dto.getMemail() != null && memberRepo.existsByMemail(dto.getMemail()))
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");

        // ★ 타입코드 0 = 일반
        MemtypeEntity role = memtypeRepo.findById(0L)
            .orElseThrow(() -> new IllegalStateException("memtype(0=일반) 시드가 없습니다."));

        // ★ 가입방법 local
        LoginmethodEntity local = loginRepo.findByLmtype("local")
            .orElseThrow(() -> new IllegalStateException("loginmethod(local) 시드가 없습니다."));

        MemberEntity e = MemberDTO.toEntity(dto);
        e.setMpw(encoder.encode(dto.getMpw()));
        e.setMemtype(role);
        e.setLoginmethod(local);

        memberRepo.save(e);
    }

    /** 로그인 검증 */
    public boolean loginCheck(String mid, String rawPw){
        return memberRepo.findByMid(mid)
                .map(e -> encoder.matches(rawPw, e.getMpw()))
                .orElse(false);
    }

    public MemberDTO readByMid(String mid){
        return memberRepo.findByMid(mid).map(MemberDTO::new).orElse(null);
    }
}
