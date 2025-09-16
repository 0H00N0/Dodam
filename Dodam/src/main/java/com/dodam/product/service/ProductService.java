// ProductService.java
package com.dodam.product.service;

import com.dodam.product.dto.ProductDTO;
import com.dodam.product.entity.*;
import com.dodam.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
  private final ProductRepository productRepo;
  private final CategoryRepository categoryRepo;
  private final ProstateRepository prostateRepo;
  private final ProductImageRepository imageRepo;

  public Long create(ProductDTO dto) {
    var p = toEntityForCreate(dto);
    p = productRepo.save(p);
    saveImages(p, dto.getImages());
    return p.getId();
  }

  public void update(ProductDTO dto) {
    var p = productRepo.findById(dto.getId()).orElseThrow();
    applyUpdatableFields(p, dto);
    // 이미지 전량 교체(간단전략)
    imageRepo.deleteByProduct(p);
    saveImages(p, dto.getImages());
  }

  @Transactional(readOnly = true)
  public ProductDTO get(Long id, boolean includeImages) {
    var p = productRepo.findById(id).orElseThrow();
    return toDTO(p, includeImages);
  }

  @Transactional(readOnly = true)
  public Page<ProductDTO> list(String q, Long categoryId, String grade, Pageable pageable) {
    Specification<ProductEntity> spec = (root, query, cb) -> {
      var preds = new ArrayList<jakarta.persistence.criteria.Predicate>();

      if (q != null && !q.isBlank()) {
        var like = "%" + q.trim() + "%";
        preds.add(cb.or(
            cb.like(root.get("name"), like),
            cb.like(root.get("brand"), like)
        ));
      }

      if (categoryId != null) {
        preds.add(cb.equal(root.get("category").get("id"), categoryId));
      }

      if (grade != null && !grade.isBlank()) {
        preds.add(cb.equal(
            root.get("status").get("grade"),
            ProstateEntity.Grade.valueOf(grade.trim().toUpperCase()) // 소문자/공백 방어
        ));
      }

      return cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };

    return productRepo.findAll(spec, pageable)
        .map(p -> toDTO(p, false)); // 목록은 이미지 제외
  }

  public void delete(Long id) {
    productRepo.deleteById(id);
  }

  // ---------- private helpers ----------
  private ProductEntity toEntityForCreate(ProductDTO d) {
    var category = categoryRepo.getReferenceById(d.getCategoryId());
    var status = prostateRepo.getReferenceById(d.getStatusId());
    return ProductEntity.builder()
        .category(category).status(status)
        .name(d.getName()).detail(d.getDetail())
        .price(d.getPrice()).borrowPrice(d.getBorrowPrice())
        .brand(d.getBrand()).maker(d.getMaker())
        .recommendAge(d.getRecommendAge()).certification(d.getCertification())
        .releaseDate(d.getReleaseDate())
        .build();
  }

  private void applyUpdatableFields(ProductEntity p, ProductDTO d) {
    if (d.getCategoryId() != null) p.setCategory(categoryRepo.getReferenceById(d.getCategoryId()));
    if (d.getStatusId() != null)   p.setStatus(prostateRepo.getReferenceById(d.getStatusId()));
    if (d.getName() != null)       p.setName(d.getName());
    if (d.getDetail() != null)     p.setDetail(d.getDetail());
    if (d.getPrice() != null)      p.setPrice(d.getPrice());
    if (d.getBorrowPrice() != null)p.setBorrowPrice(d.getBorrowPrice());
    if (d.getBrand() != null)      p.setBrand(d.getBrand());
    if (d.getMaker() != null)      p.setMaker(d.getMaker());
    if (d.getRecommendAge() != null) p.setRecommendAge(d.getRecommendAge());
    if (d.getCertification() != null) p.setCertification(d.getCertification());
    if (d.getReleaseDate() != null) p.setReleaseDate(d.getReleaseDate());
  }

  private void saveImages(ProductEntity p, List<ProductDTO.Image> images) {
    int ord = 1;
    for (var img : Optional.ofNullable(images).orElse(List.of())) {
      imageRepo.save(ProductImageEntity.builder()
          .product(p)
          .orderNo(img.getOrderNo() != null ? img.getOrderNo() : ord++)
          .url(img.getUrl())
          .type(img.getType())
          .build());
    }
  }

  private ProductDTO toDTO(ProductEntity p, boolean includeImages) {
    var dto = ProductDTO.builder()
        .id(p.getId())
        .categoryId(p.getCategory().getId())
        .categoryName(p.getCategory().getName())
        .statusId(p.getStatus().getId())
        .grade(p.getStatus().getGrade().name())
        .name(p.getName()).detail(p.getDetail())
        .price(p.getPrice()).borrowPrice(p.getBorrowPrice())
        .brand(p.getBrand()).maker(p.getMaker())
        .recommendAge(p.getRecommendAge()).certification(p.getCertification())
        .releaseDate(p.getReleaseDate())
        .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
        .build();

    if (includeImages) {
      dto.setImages(p.getImages().stream()
          .map(i -> ProductDTO.Image.builder()
              .id(i.getId()).orderNo(i.getOrderNo())
              .url(i.getUrl()).type(i.getType())
              .build())
          .toList());
    }
    return dto;
  }
}
