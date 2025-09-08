package com.dodam.admin.dto;
import com.dodam.member.entity.MemberEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDTO {
    private Long id;
    private String name;
    private String userId;
    private String role;
    private String token;

    public static LoginResponseDTO fromEntity(MemberEntity member, String token) {
        return LoginResponseDTO.builder()
                .id(member.getMnum())
                .name(member.getMname())
                .userId(member.getMid())
                .role(member.getMemtype().getRoleName())
                .token(token)
                .build();
    }
}

