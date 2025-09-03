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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mtnum")
    private Long mtnum;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName; // ADMIN, STAFF, DELIVERYMAN, SUPERADMIN 등
}