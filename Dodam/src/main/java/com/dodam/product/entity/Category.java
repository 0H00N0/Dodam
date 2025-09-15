package com.dodam.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 카테고리 정보를 관리하는 Entity
 * 상품들을 분류하기 위한 카테고리 정보를 저장합니다.
 */
@Entity
@Table(name = "category", indexes = {
    @Index(name = "idx_category_name", columnList = "categoryName")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"products"}) // 순환 참조 방지
public class Category {

    /**
     * 카테고리 고유 번호 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 카테고리 이름 (필수값)
     */
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    /**
     * 카테고리 설명 (선택값)
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 생성일시
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 삭제일시 (소프트 삭제용)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 이 카테고리에 속한 상품들
     * 지연 로딩으로 성능 최적화
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    /**
     * 카테고리가 삭제되었는지 확인
     * @return 삭제 여부
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 카테고리 소프트 삭제
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 카테고리 복구
     */
    public void restore() {
        this.deletedAt = null;
    }
}