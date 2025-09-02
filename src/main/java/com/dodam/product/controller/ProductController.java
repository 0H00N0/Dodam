package com.dodam.product.controller;

import com.dodam.admin.entity.AdminEntity;
import com.dodam.admin.repository.AdminRepository;
import com.dodam.product.service.ProductBulkService;
import com.dodam.product.service.ProductBulkService.BulkImportResult;
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
import java.util.Optional;

@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductBulkService productBulkService;
    private final AdminRepository adminRepository;

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
}
