package com.dodam.member.dto;

import java.time.LocalDate;
import com.dodam.member.entity.MemberEntity;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberDTO {
    private Long mnum;
    private String mid;
<<<<<<< HEAD
    private String mpw;     // 평문 입력 -> 서비스에서 BCrypt 인코딩
=======
    private String mpw;     // 평문 입력 -> 서비스에서(선택) BCrypt 인코딩
>>>>>>> origin/chan
    private String mname;
    private String memail;
    private String mtel;
    private String maddr;
    private Long   mpost;
    private LocalDate mbirth;
    private String mnic;

    // 화면 출력용
<<<<<<< HEAD
    private Long   roleCode; // 0/1/2/3
    private String roleName; // 일반/SuperAdmin/Staff/Deliveryman
    private String joinWay;  // local/naver/google/kakao
=======
    private Long   roleCode; // 0/1/2/3  (memtype.mtcode)
    private String roleName; // 일반/SuperAdmin/Staff/Deliveryman (memtype.mtname)
    private String joinWay;  // local/naver/google/kakao (loginmethod.lmtype)
>>>>>>> origin/chan

    public static MemberEntity toEntity(MemberDTO d){
        return MemberEntity.builder()
            .mnum(d.getMnum())
            .mid(d.getMid())
<<<<<<< HEAD
            .mpw(d.getMpw()) // encode in service
=======
            .mpw(d.getMpw()) // (선택) 서비스에서 BCrypt 적용 가능
>>>>>>> origin/chan
            .mname(d.getMname())
            .memail(d.getMemail())
            .mtel(d.getMtel())
            .maddr(d.getMaddr())
            .mpost(d.getMpost())
            .mbirth(d.getMbirth())
            .mnic(d.getMnic())
            .build();
    }

    public MemberDTO(MemberEntity e){
        this.mnum = e.getMnum();
        this.mid = e.getMid();
        this.mpw = e.getMpw();
        this.mname = e.getMname();
        this.memail = e.getMemail();
        this.mtel = e.getMtel();
        this.maddr = e.getMaddr();
        this.mpost = e.getMpost();
        this.mbirth = e.getMbirth();
        this.mnic = e.getMnic();
<<<<<<< HEAD
        if (e.getMemtype() != null) {
            this.roleCode = e.getMemtype().getMtnum();
            this.roleName = e.getMemtype().getRoleName(); // ⚠️ roleName 필드가 올바르므로 getRoleName()으로 변경
        }
        if (e.getLoginmethod() != null) {
            // 붉은색으로 변경된 부분이 수정되었습니다.
            this.joinWay = e.getLoginmethod().getLmtype(); 
        }
    }
}
=======

        if (e.getMemtype() != null) {
            // mtcode(int/Integer) -> Long 캐스팅
            Integer code = e.getMemtype().getMtcode();
            this.roleCode = (code != null) ? code.longValue() : null;
            this.roleName = e.getMemtype().getMtname();
        }
        if (e.getLoginmethod() != null) {
            this.joinWay = e.getLoginmethod().getLmtype();
        }
    }
}
>>>>>>> origin/chan
