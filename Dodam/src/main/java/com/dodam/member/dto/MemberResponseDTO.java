package com.dodam.member.dto;

import com.dodam.member.entity.MemberEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 회원 정보 응답을 위한 DTO (Data Transfer Object)
 * API를 통해 클라이언트(React)에게 전달될 사용자 정보입니다.
 */
@Getter
@Builder
public class MemberResponseDTO {

    private final Long mnum;         // 회원 고유번호
    private final String mid;        // 아이디
    private final String mname;      // 이름
    private final String memail;     // 이메일
    private final String mtel;       // 연락처
    private final LocalDate mreg;    // 가입일
    private final String mnic;       // 닉네임
    private final String roleName;   // 역할 (ADMIN, STAFF 등)
    private final String loginType;  // 로그인 타입 (local, google 등)

    /**
     * MemberEntity를 MemberResponseDTO로 변환하는 정적 팩토리 메서드
     * @param member 변환할 MemberEntity 객체
     * @return 변환된 MemberResponseDTO 객체
     */
    public static MemberResponseDTO fromEntity(MemberEntity member) {
        if (member == null) {
            return null;
        }

        return MemberResponseDTO.builder()
                .mnum(member.getMnum())
                .mid(member.getMid())
                .mname(member.getMname())
                .memail(member.getMemail())
                .mtel(member.getMtel())
                .mreg(member.getMreg())
                .mnic(member.getMnic())
                // Lazy-Loading 문제를 방지하고 필요한 데이터만 추출합니다.
                .roleName(member.getMemtype() != null ? member.getMemtype().getRoleName() : "N/A")
                .loginType(member.getLoginmethod() != null ? member.getLoginmethod().getLmtype() : "N/A")
                .build();
    }
}
