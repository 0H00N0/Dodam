package com.dodam.product.controller;

import com.dodam.product.entity.ProductEntity;
import com.dodam.product.service.ProductService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("product", productService.findAll());
        return "admin/product/list";
    }

    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long pronum) {
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
                        product.setProcreat(Integer.parseInt(cookie.getValue()));
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
    public ResponseEntity<Map<String, Object>> edit(@PathVariable Long id, @ModelAttribute ProductEntity product) {
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
    public String delete(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/admin/product";
    }
}