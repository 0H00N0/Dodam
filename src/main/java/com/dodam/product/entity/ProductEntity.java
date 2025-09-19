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
