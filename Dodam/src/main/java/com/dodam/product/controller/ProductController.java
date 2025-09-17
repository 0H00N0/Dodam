// ProductController.java
package com.dodam.product.controller;

import com.dodam.product.dto.ProductDTO;
import com.dodam.product.service.ProductService;
import jakarta.validation.constraints.Positive;        // ⬅️ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated; // ⬅️ 타입 레벨 사용
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/products") // 
@RequiredArgsConstructor
@Validated // ⬅️ 경로/쿼리 파라미터 검증 활성화
public class ProductController {
  private final ProductService productService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> create(@Validated(ProductDTO.Create.class) @RequestBody ProductDTO dto) {
    Long id = productService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
  }

  @PutMapping(value="/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> update(@PathVariable @Positive Long id,  // ⬅️ 경로 변수 검증
                                  @Validated(ProductDTO.Update.class) @RequestBody ProductDTO dto) {
    dto.setId(id); // 검증 후 서비스로 전달
    productService.update(dto);
    return ResponseEntity.ok(Map.of("message","ok"));
  }

  @GetMapping
  public Page<ProductDTO> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String grade,
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    return productService.list(q, categoryId, grade, pageable);
  }

  @GetMapping("/{id}")
  public ProductDTO detail(@PathVariable @Positive Long id) { // ⬅️ 경로 변수 검증
    return productService.get(id, true);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable @Positive Long id) { // ⬅️ 경로 변수 검증
    productService.delete(id);
  }
}
