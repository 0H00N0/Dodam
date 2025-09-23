package com.dodam.event.entity;

import java.time.LocalDateTime;

import com.dodam.member.entity.MemberEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "LotteryTicket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LotteryTicket {

    @Id
    @Column(name = "lotNum", nullable = false)
    private Long lotNum;   // PK: 추첨권 고유번호

    @Column(name = "lotCount", nullable = false)
    private Integer lotCount = 0;   // 보유 개수 (default 0)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mnum", nullable = false)
    private MemberEntity member;   // FK: Member 참조

    @Column(name = "lmNum", nullable = false)
    private Long lmNum;   // FK: LoginMethod 참조

    @Column(name = "mtNum", nullable = false)
    private Long mtNum;   // FK: Memtype 참조

    @ManyToOne
    @JoinColumn(name = "lotTypeNum", nullable = false)
    private LotteryTicketType ticketType;   // FK: 추첨권 종류 참조

    @Column(name = "issuedAt", nullable = false)
    private LocalDateTime issuedAt;   // 발급 시각

    @Column(name = "usedAt")
    private LocalDateTime usedAt;   // 사용 시각

    @Column(name = "status", nullable = false)
    private Integer status = 0;   // 0=미사용, 1=사용, 2=만료
}

