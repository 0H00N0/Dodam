package com.dodam.event.entity;

import java.time.LocalDateTime;

import com.dodam.member.entity.MemberEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "First")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class First {

    @Id
    @Column(name = "fNum", nullable = false)
    private Long fNum;   // PK: 선착순 참여 고유번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mnum", nullable = false)
    private MemberEntity member;   // FK: 회원

    @Column(name = "gifyCon")
    private Long gifyCon;   // FK: Gifycon (기프티콘)

    @ManyToOne
    @JoinColumn(name = "evNum", nullable = false)
    private EventNumber event;   // FK: 이벤트 번호

    @Column(name = "cTNum")
    private String cTNum;   // FK: 쿠폰타입

    @Column(name = "fDate", nullable = false)
    private LocalDateTime fDate;   // 참여 일시

    @Column(name = "orderNum")
    private Integer orderNum;   // 몇 번째 참여인지

    @Column(name = "winState", nullable = false)
    private Integer winState = 0;   // 0=탈락, 1=당첨
}
