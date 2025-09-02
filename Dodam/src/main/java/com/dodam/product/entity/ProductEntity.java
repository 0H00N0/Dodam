package com.dodam.product.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
@Data
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pronum; // PK

    @ManyToOne
    @JoinColumn(name = "catenum")
    private CategoryEntity category; // 카테고리 참조

    private Integer procreat; // 등록자 ID (쿠키에서 가져옴, Integer로 가정)

    @Column(length = 200)
    private String proname; // 상품명

    @Lob
    private String prodetai1; // 설명 (CLOB)

    @Column(precision = 12, scale = 2)
    private BigDecimal proprice; // 정가

    @Column(precision = 12, scale = 2)
    private BigDecimal prorent; // 대여가격

    @Column(precision = 12, scale = 2)
    private BigDecimal prodepos; // 보증금

    @Column(precision = 12, scale = 2)
    private BigDecimal prolatfe; // 연체료

    @Column(length = 100)
    private String probrand; // 브랜드

    @Column(length = 100)
    private String promanuf; // 제조사

    @Column(length = 100)
    private String prosafe; // 안전인증

    @Column(length = 1)
    private String prograd; // 등급 (S/A/B/C)

    private Integer proagfr; // 최소 연령

    private Integer proagto; // 최대 연령

    private Integer promind; // 최소 대여일

    @Column(length = 100)
    private String prostat; // 상태

    private Boolean proispu; // 공개 여부

    private LocalDate prodate; // 날짜 (자동 설정)

    private LocalDateTime procdate; // 생성일시 (자동 설정)

    @PrePersist
    public void prePersist() {
        this.prodate = LocalDate.now();
        this.procdate = LocalDateTime.now();
    }
}