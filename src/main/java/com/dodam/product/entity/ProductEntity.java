<<<<<<< HEAD
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
=======
// ProductEntity.java
package com.dodam.product.entity;
import jakarta.persistence.*; import lombok.*;
import org.hibernate.annotations.CreationTimestamp; import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.OffsetDateTime; import java.util.*;

@Entity @Table(name="product")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="pronum") private Long id;

  @ManyToOne(fetch=FetchType.LAZY, optional=false)
  @JoinColumn(name="catenum") private CategoryEntity category;

  @ManyToOne(fetch=FetchType.LAZY, optional=false)
  @JoinColumn(name="prosnum") private ProstateEntity status;

  @Column(name="prname", nullable=false, length=200) private String name;
  @Lob @Column(name="prodetail", columnDefinition="CLOB") private String detail;

  @Column(name="proprice", precision=12, scale=0, nullable=false) private BigDecimal price;
  @Column(name="proborrow", precision=12, scale=0) private BigDecimal borrowPrice;

  @Column(name="probrand", length=100) private String brand;
  @Column(name="promade", length=100) private String maker;
  @Column(name="proage") private Integer recommendAge;
  @Column(name="procerti", length=100) private String certification;
  @Column(name="prodate") private LocalDate releaseDate;

  @CreationTimestamp @Column(name="procre", updatable=false) private OffsetDateTime createdAt;
  @UpdateTimestamp @Column(name="propudate") private OffsetDateTime updatedAt;

  @OneToMany(mappedBy="product", cascade=CascadeType.ALL, orphanRemoval=true)
  @OrderBy("orderNo ASC")
  private List<ProductImageEntity> images = new ArrayList<>();
}
>>>>>>> refs/remotes/origin/chan787
