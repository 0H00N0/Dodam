package com.dodam.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequest {
    private String mid; // Admin ID
    private String mpw; // Admin Password
}