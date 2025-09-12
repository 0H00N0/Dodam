package com.dodam.product.controller;

import com.dodam.product.dto.request.ProductRequestDto;
import com.dodam.product.dto.response.ProductResponseDto;
import com.dodam.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 상품 관리 REST API Controller
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 테스트용 상품 목록 조회 (간단 버전)
     */
    @GetMapping
    public ResponseEntity<String> getAllProducts() {
        // TODO: 실제 서비스 메소드 구현 후 연결
        return ResponseEntity.ok("Product list will be here");
    }

    /**
     * 테스트용 상품 등록 (간단 버전)
     */
    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody ProductRequestDto requestDto) {
        // TODO: 실제 서비스 메소드 구현 후 연결
        return ResponseEntity.status(HttpStatus.CREATED).body("Product created successfully!");
    }

    /**
     * 테스트용 간단한 엔드포인트
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Product Controller is working!");
    }
}