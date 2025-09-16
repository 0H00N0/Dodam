// ProductDTO.java
package com.dodam.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {

  // Validation Groups
  public interface Create {}
  public interface Update {}

  // 키 & FK
  private Long id; // pronum (경로 변수로 검증하므로 Update에서 NotNull 제거)
  @NotNull(groups = Create.class) private Long categoryId; // catenum
  private String categoryName;                             // 응답용
  @NotNull(groups = Create.class) private Long statusId;   // prosnum
  private String grade;                                    // 응답용: S/A/B/C

  // 기본 정보
  @NotBlank(groups = Create.class) @Size(max = 200) private String name;
  private String detail;

  // price는 DB에서 NOT NULL → Create 시 필수
  @NotNull(groups = Create.class) @PositiveOrZero
  private BigDecimal price;

  @PositiveOrZero private BigDecimal borrowPrice;
  @Size(max = 100) private String brand;
  @Size(max = 100) private String maker;
  @Min(0) @Max(200) private Integer recommendAge;
  @Size(max = 100) private String certification;
  @PastOrPresent private LocalDate releaseDate;

  // 메타 (응답용)
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  // 이미지 URL 목록
  private List<Image> images;

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class Image {
    private Long id;
    private Integer orderNo;
    @NotBlank(groups = Create.class) private String url;
    private String type; // 'THUMB','DETAIL'...
  }
}
