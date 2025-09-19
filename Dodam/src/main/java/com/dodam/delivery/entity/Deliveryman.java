package com.dodam.delivery.entity;

import com.dodam.member.entity.MemberEntity;
import com.dodam.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "deliveryman")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deliveryman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delinum")
    private Long deliNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pronum", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mnum")
    private MemberEntity member;

    @Column(name = "dayoff")
    private Integer dayOff;

    @Column(name = "delcost")
    private Integer delCost;

    @Column(name = "location")
    private String location;
}