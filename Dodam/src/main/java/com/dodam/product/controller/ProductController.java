<<<<<<< HEAD
package com.dodam.product.controller;

import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.repository.AdminRepository;
import com.dodam.product.entity.ProductEntity;
import com.dodam.product.service.ProductBulkService;
import com.dodam.product.service.ProductBulkService.BulkImportResult;
import com.dodam.product.service.ProductService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductBulkService productBulkService;
    private final AdminRepository adminRepository;
    
    private final ProductService productService;


    @GetMapping
    public String list(Model model) {
        model.addAttribute("product", productService.findAll());
        return "admin/product/list";
    }

    @GetMapping("/form")
    public String form(Model model, @RequestParam(name = "pronum", required = false) Long pronum) {
        ProductEntity product = pronum == null ? new ProductEntity() : productService.findById(pronum).orElse(new ProductEntity());
        model.addAttribute("product", product);
        model.addAttribute("categories", productService.getAllCategories());
        return "admin/product/form";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> add(@ModelAttribute ProductEntity product, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("username".equals(cookie.getName())) {
                        Optional<AdminEntity> admin = adminRepository.findByUsername(cookie.getValue());
                        if (admin.isPresent()) {
                            product.setProcreat(admin.get().getId().intValue());
                        }
                    }




                }
            }
            ProductEntity savedProduct = productService.save(product);
            response.put("success", true);
            response.put("message", "상품이 성공적으로 등록되었습니다.");
            response.put("id", savedProduct.getPronum());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "등록 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/edit/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> edit(@PathVariable("id") Long id, @ModelAttribute ProductEntity product) {
        Map<String, Object> response = new HashMap<>();
        try {
            product.setPronum(id);
            productService.save(product);
            response.put("success", true);
            response.put("message", "상품이 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "수정 실패: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        productService.deleteById(id);
        return "redirect:/admin/product";
    }

    // 업로드 페이지
    @GetMapping("/bulk")
    public String bulkPage() { return "admin/product/bulk"; }

    // 템플릿 다운로드
    @GetMapping("/bulk/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() {
        ByteArrayInputStream in = productBulkService.makeTemplateXlsx();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=product_template.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(new InputStreamResource(in));
    }

    // 업로드 처리
    @PostMapping("/bulk-upload")
    public String bulkUpload(@RequestParam("file") MultipartFile file,
                             HttpServletRequest request,
                             Model model) throws Exception {

        // 등록자 가져오기 (쿠키 username → AdminEntity.id)
        Long adminId = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("username".equals(c.getName())) {
                    Optional<AdminEntity> admin = adminRepository.findByUsername(c.getValue());
                    adminId = admin.map(a -> a.getId()).orElse(null);
                    break;
                }
            }
        }

        BulkImportResult result = productBulkService.importXlsx(file, adminId);
        model.addAttribute("result", result);
        return "admin/product/bulk"; // 결과를 같은 페이지에서 보여줌
    }
=======
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
>>>>>>> refs/remotes/origin/chan787
}
