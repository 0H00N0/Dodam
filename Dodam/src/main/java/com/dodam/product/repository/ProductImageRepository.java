// ProductImageRepository.java
package com.dodam.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import com.dodam.product.entity.ProductEntity;
import com.dodam.product.entity.ProductImageEntity;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {

  @Modifying
  int deleteByProduct(ProductEntity product);
}
