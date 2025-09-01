package com.dodam.product;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "category")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "catenum")
    private Integer catenum;

    @Column(name = "catename", nullable = false)
    private String catename;
}
