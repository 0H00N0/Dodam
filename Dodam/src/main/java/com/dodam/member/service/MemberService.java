package com.dodam.member.service;

import com.dodam.member.dto.MemberDTO;
import com.dodam.member.entity.LoginmethodEntity;
import com.dodam.member.entity.MemberEntity;
import com.dodam.member.entity.MemtypeEntity;
import com.dodam.member.repository.LoginmethodRepository;
import com.dodam.member.repository.MemberRepository;
import com.dodam.member.repository.MemtypeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LoginmethodRepository loginmethodRepository;
    private final MemtypeRepository memtypeRepository;
    // private final BCryptPasswordEncoder encoder;

    private LoginmethodEntity getOrCreateLocal() {
        return loginmethodRepository.findByLmtype("LOCAL")
                .orElseGet(() -> loginmethodRepository.save(
                        LoginmethodEntity.builder().lmtype("LOCAL").build()
                ));
    }

    private MemtypeEntity getOrCreateDefault() {
        // 0 = 일반
        return memtypeRepository.findByMtcode(0)
                .orElseGet(() -> memtypeRepository.save(
                        MemtypeEntity.builder().mtcode(0).mtname("일반").build()
                ));
    }

    public void signup(MemberDTO dto) {
        if (memberRepository.existsByMid(dto.getMid())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "duplicated mid");
        }

        MemberEntity e = MemberEntity.builder()
                .mid(dto.getMid())
                // .mpw(encoder.encode(dto.getMpw()))
                .mpw(dto.getMpw())
                .mname(dto.getMname())
                .mtel(dto.getMtel())
                .loginmethod(getOrCreateLocal())   // ✅ 필수 FK
                .memtype(getOrCreateDefault())     // ✅ 필수 FK
                .build();

        memberRepository.save(e);
    }

    public MemberDTO login(String mid, String rawPw) {
        var e = memberRepository.findByMid(mid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid id/pw"));
        if (!rawPw.equals(e.getMpw())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid id/pw");
        }
        return new MemberDTO(e);
    }

<<<<<<< HEAD
    public boolean exists(String mid) {
        return memberRepository.existsByMid(mid);
    }
=======


    // 로그인 검증 등 다른 메서드가 있다면 여기에…
    // @Transactional(readOnly = true)
    // public boolean loginCheck(String mid, String rawPw) { ... }

    // 헬퍼
    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    
    //회원정보 수정
    public void updateProfile(Long memberId, MemberDTO dto) {
        MemberEntity entity = memberRepo.findById(memberId)
            .orElseThrow(() -> new RuntimeException("회원 없음"));
        entity.setMemail(dto.getMemail());
        entity.setMtel(dto.getMtel());
        entity.setMaddr(dto.getMaddr());
        entity.setMnic(dto.getMnic());
        // ...필요한 필드 추가...
        memberRepo.save(entity);
    }
    
    //비밀번호 수정
    public void changePw(Long memberId, String newPassword) {
    	MemberEntity entity = memberRepo.findById(memberId)
            .orElseThrow(() -> new RuntimeException("회원 없음"));
    	entity.setMpw(newPassword); // 평문 저장);
        memberRepo.save(entity);
    }
    
>>>>>>> 7d10f4adf4389fcca9a5fdd89100e877690ec59b
}
