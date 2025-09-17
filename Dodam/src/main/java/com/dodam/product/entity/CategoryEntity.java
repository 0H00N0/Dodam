// CategoryEntity.java
package com.dodam.product.entity;
import jakarta.persistence.*; import lombok.*;

@Entity @Table(name="category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryEntity {
	
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="catenum") 
  private Long id;
  
  @Column(name="catename", nullable=false, length=100) 
  private String name;
}
