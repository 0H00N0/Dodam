package com.dodam.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loginmethod")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginmethodEntity {
    @Id
    @Column(name = "lmunm") // PK: 가입번호
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lmunm;

    @Column(name = "lmtype", nullable = false, length = 50) // local/naver/google/kakao
    private String lmtype;
}
