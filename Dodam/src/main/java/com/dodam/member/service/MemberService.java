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
import org.springframework.security.crypto.password.PasswordEncoder; // ✅ 추가
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LoginmethodRepository loginmethodRepository;
    private final MemtypeRepository memtypeRepository;
    private final PasswordEncoder passwordEncoder; // ✅ 추가

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

        // ✅ 비밀번호 해시 저장
        String encoded = passwordEncoder.encode(dto.getMpw());

        MemberEntity e = MemberEntity.builder()
                .mid(dto.getMid())
                .mpw(encoded)                  // ✅ 해시 저장
                .mname(dto.getMname())
                .mtel(dto.getMtel())
                .loginmethod(getOrCreateLocal())
                .memtype(getOrCreateDefault())
                .build();

        memberRepository.save(e);
    }

    public void signupAdmin(MemberDTO dto, String memtype) {
        if (memberRepository.existsByMid(dto.getMid())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "duplicated mid");
        }

        String encoded = passwordEncoder.encode(dto.getMpw());

        MemtypeEntity memtypeEntity = memtypeRepository.findByMtname(memtype)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid member type"));

        MemberEntity e = MemberEntity.builder()
                .mid(dto.getMid())
                .mpw(encoded)
                .mname(dto.getMname())
                .memail(dto.getMemail())
                .mtel(dto.getMtel())
                .maddr(dto.getMaddr())
                .mpost(dto.getMpost())
                .mbirth(dto.getMbirth())
                .mnic(dto.getMnic())
                .loginmethod(getOrCreateLocal())
                .memtype(memtypeEntity)
                .build();

        memberRepository.save(e);
    }

    public MemberDTO login(String mid, String rawPw) {
        var e = memberRepository.findByMid(mid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid id/pw"));

        // ✅ 해시 검증
        if (!passwordEncoder.matches(rawPw, e.getMpw())) {
            // (선택) 평문→해시 마이그레이션이 필요하면 아래 주석 블록 참고
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid id/pw");
        }

        return new MemberDTO(e);
    }

    public boolean exists(String mid) {
        return memberRepository.existsByMid(mid);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    public void updateProfile(String sid, MemberDTO dto) {
        MemberEntity entity = memberRepository.findByMid(sid)
            .orElseThrow(() -> new RuntimeException("회원 없음"));
        entity.setMemail(dto.getMemail());
        entity.setMtel(dto.getMtel());
        entity.setMaddr(dto.getMaddr());
        entity.setMnic(dto.getMnic());
        memberRepository.save(entity);
    }

    public void changePw(String sid, String currentPassword, String newPassword) {
        MemberEntity entity = memberRepository.findByMid(sid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 없음"));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, entity.getMpw())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 해시 저장
        entity.setMpw(passwordEncoder.encode(newPassword));
        memberRepository.save(entity);
    }

    public MemberDTO me(String mid) {
        var e = memberRepository.findByMid(mid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 없음"));
        return new MemberDTO(e);
    }

    public String findIdByNameAndTel(String mname, String mtel) {
        return memberRepository.findByMnameAndMtel(mname, mtel)
            .map(MemberEntity::getMid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일치하는 회원이 없습니다."));
    }

    public String findIdByNameAndEmail(String mname, String memail) {
        return memberRepository.findByMnameAndMemail(mname, memail)
            .map(MemberEntity::getMid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일치하는 회원이 없습니다."));
    }

    public List<MemberDTO> findAll() {
        return memberRepository.findAll().stream()
                .map(MemberDTO::new)
                .collect(Collectors.toList());
    }

    public MemberDTO findById(Long id) {
        return memberRepository.findById(id)
                .map(MemberDTO::new)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    /*
    // (선택) 기존 평문 비번 마이그레이션 예시:
    public MemberDTO loginWithSoftMigration(String mid, String rawPw) {
        var e = memberRepository.findByMid(mid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid id/pw"));

        String stored = e.getMpw();
        boolean looksHashed = stored != null && stored.startsWith("$2"); // BCrypt
        boolean ok;

        if (looksHashed) {
            ok = passwordEncoder.matches(rawPw, stored);
        } else {
            ok = rawPw.equals(stored); // 기존 평문 비교
            if (ok) {
                // 첫 성공 시 해시로 교체
                e.setMpw(passwordEncoder.encode(rawPw));
                memberRepository.save(e);
            }
        }
        if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid id/pw");
        return new MemberDTO(e);
    }
    */
}
