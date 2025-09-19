package com.dodam.member.service;

import com.dodam.member.dto.ChangePwDTO;
import com.dodam.member.dto.MemberDTO;
import com.dodam.member.entity.LoginmethodEntity;
import com.dodam.member.entity.MemberEntity;
import com.dodam.member.entity.MemtypeEntity;
import com.dodam.member.repository.LoginmethodRepository;
import com.dodam.member.repository.MemberRepository;
import com.dodam.member.repository.MemtypeRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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

        String encoded = passwordEncoder.encode(dto.getMpw());

        MemberEntity e = MemberEntity.builder()
                .mid(dto.getMid())
                .mpw(encoded)
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

        if (!passwordEncoder.matches(rawPw, e.getMpw())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid id/pw");
        }

        return new MemberDTO(e);
    }

    public boolean exists(String mid) {
        return memberRepository.existsByMid(mid);
    }

    private static boolean isBlank(String s) { 
        return s == null || s.isBlank(); 
    }

    public void updateProfile(String sid, MemberDTO dto) {
        MemberEntity entity = memberRepository.findByMid(sid)
                .orElseThrow(() -> new RuntimeException("회원 없음"));
        entity.setMemail(dto.getMemail());
        entity.setMtel(dto.getMtel());
        entity.setMaddr(dto.getMaddr());
        entity.setMnic(dto.getMnic());
        memberRepository.save(entity);
    }

    public void changePw(String sid, ChangePwDTO dto) {
        MemberEntity entity = memberRepository.findByMid(sid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 없음"));

        // 현재 비밀번호 검증
        if (!passwordEncoder.matches(dto.getCurrentPw(), entity.getMpw())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 저장
        entity.setMpw(passwordEncoder.encode(dto.getNewPw()));
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

    // 비밀번호 암호화 후 DB에 저장
    public void updatePassword(String mid, String tempPw) {
        MemberEntity member = memberRepository.findByMid(mid).orElseThrow();
        member.setMpw(passwordEncoder.encode(tempPw));
        memberRepository.save(member);
    }

    public boolean existsByMidNameEmail(String mid, String mname, String memail) {
        return memberRepository.findByMidAndMnameAndMemail(mid, mname, memail).isPresent();
    }

    public boolean existsByMidNameTel(String mid, String mname, String mtel) {
        return memberRepository.findByMidAndMnameAndMtel(mid, mname, mtel).isPresent();
    }
    
    public void changePwDirect(String mid, String newPw) {
        MemberEntity entity = memberRepository.findByMid(mid)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원 없음"));
        entity.setMpw(passwordEncoder.encode(newPw));
        memberRepository.save(entity);
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
                e.setMpw(passwordEncoder.encode(rawPw));
                memberRepository.save(e);
            }
        }
        if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid id/pw");
        return new MemberDTO(e);
    }
    */
}
