package com.dodam.admin.dto;

import com.dodam.member.entity.MemberEntity;
import lombok.Getter;

@Getter
public class AdminInfoResponse {
    private String mid;
    private String mname;
    private String mnic;
    private String role;

    public AdminInfoResponse(MemberEntity entity) {
        this.mid = entity.getMid();
        this.mname = entity.getMname();
        this.mnic = entity.getMnic();
        this.role = entity.getRoleString(); // Using the helper method from MemberEntity
    }
}
