package com.dodam.member.dto;

import java.time.LocalDate;
import com.dodam.member.entity.MemberEntity;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberDTO {
    private Long mnum;
    private String mid;
    private String mpw;     // 평문 입력 -> 서비스에서 BCrypt 인코딩
    private String mname;
    private String memail;
    private String mtel;
    private String maddr;
    private Long   mpost;
    private LocalDate mbirth;
    private String mnic;

    // 화면 출력용
    private Long   roleCode; // 0/1/2/3
    private String roleName; // 일반/SuperAdmin/Staff/Deliveryman
    private String joinWay;  // local/naver/google/kakao

    public static MemberEntity toEntity(MemberDTO d){
        return MemberEntity.builder()
            .mnum(d.getMnum())
            .mid(d.getMid())
            .mpw(d.getMpw()) // encode in service
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