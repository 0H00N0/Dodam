package com.dodam.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "memtype")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemtypeEntity {
    @Id
    @Column(name = "mtnum")             // ★ 고정 코드 PK (0/1/2/3)
    private Long mtnum;                  // auto 증가 없음

    @Column(name = "mtmname", nullable = false, length = 50)
    private String mtmname;              // "일반", "SuperAdmin", "Staff", "Deliveryman"
}
