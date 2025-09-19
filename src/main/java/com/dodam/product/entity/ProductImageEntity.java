// ProductImageEntity.java  (URL 저장)
package com.dodam.product.entity;
import jakarta.persistence.*; import lombok.*;

@Entity @Table(name="productimage")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImageEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="proimagenum") private Long id;

  @ManyToOne(fetch=FetchType.LAZY, optional=false)
  @JoinColumn(name="pronum") private ProductEntity product;

  @Column(name="proimageorder") private Integer orderNo;
  @Column(name="prourl", nullable=false, length=400) private String url;
  @Column(name="prodetailimage", length=20) private String type;   // 'THUMB' | 'DETAIL'
}
