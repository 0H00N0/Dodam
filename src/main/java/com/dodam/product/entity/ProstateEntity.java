// ProstateEntity.java 
package com.dodam.product.entity;
import jakarta.persistence.*; import lombok.*;

@Entity @Table(name="prostate")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProstateEntity {
	
  public enum Grade { S, A, B, C }
  
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="prosnum") 
  private Long id;
  
  @Enumerated(EnumType.STRING)
  @Column(name="prograde", nullable=false, length=1) 
  private Grade grade;
}
