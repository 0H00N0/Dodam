package com.dodam.product.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "category") // 카테고리 테이블 가정
@Data
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long catenum;

    @Column(length = 100)
    private String catename; // 카테고리 이름
}