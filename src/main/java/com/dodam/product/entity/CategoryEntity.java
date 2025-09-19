<<<<<<< HEAD
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
=======
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
>>>>>>> refs/remotes/origin/chan787
