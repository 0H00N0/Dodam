package com.dodam.member.service;

<<<<<<< HEAD
=======
import java.time.LocalDate;

>>>>>>> origin/chan
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dodam.member.dto.MemberDTO;
<<<<<<< HEAD
import com.dodam.member.entity.*;
import com.dodam.member.repository.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
=======
import com.dodam.member.entity.LoginmethodEntity;
import com.dodam.member.entity.MemtypeEntity;
import com.dodam.member.repository.LoginmethodRepository;
import com.dodam.member.repository.MemberRepository;
import com.dodam.member.repository.MemtypeRepository;

import lombok.RequiredArgsConstructor;
>>>>>>> origin/chan

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepo;
<<<<<<< HEAD
    private final MemtypeRepository memtypeRepo;
    private final LoginmethodRepository loginRepo;

    /** 회원가입: 기본 memtype=0(일반), loginmethod=local */
    @Transactional
    public void signup(MemberDTO dto){
        if (memberRepo.existsByMid(dto.getMid()))
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        if (dto.getMemail() != null && memberRepo.existsByMemail(dto.getMemail()))
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");

        MemtypeEntity role = memtypeRepo.findByRoleName("USER")
            .orElseThrow(() -> new IllegalStateException("'USER' 역할이 DB에 없습니다."));
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
        String role = member.getMemtype().getRoleName();
        if (!("SUPERADMIN".equals(role) || "ADMIN".equals(role) || "STAFF".equals(role))) {
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

=======
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

}
>>>>>>> origin/chan
