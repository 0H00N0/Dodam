package com.dodam.admin.controller;

import com.dodam.admin.dto.AdminProductDto;
import com.dodam.admin.dto.ApiResponseDTO;
import com.dodam.admin.service.AdminProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminProductController {

    private final AdminProductService adminProductService;

    @PostMapping
    public ResponseEntity<AdminProductDto.Response> createProduct(@RequestBody AdminProductDto.CreateRequest requestDto) {
        AdminProductDto.Response responseDto = adminProductService.createProduct(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AdminProductDto.Response>> getAllProducts() {
        List<AdminProductDto.Response> products = adminProductService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<AdminProductDto.Response> getProductById(@PathVariable Long productId) {
        AdminProductDto.Response product = adminProductService.getProduct(productId);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<AdminProductDto.Response> updateProduct(@PathVariable Long productId, @RequestBody AdminProductDto.UpdateRequest requestDto) {
        AdminProductDto.Response updatedProduct = adminProductService.updateProduct(productId, requestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponseDTO> deleteProduct(@PathVariable Long productId) {
        adminProductService.deleteProduct(productId);
        ApiResponseDTO response = new ApiResponseDTO(true, "Product deleted successfully.");
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDTO> handleEntityNotFoundException(EntityNotFoundException ex) {
        ApiResponseDTO response = new ApiResponseDTO(false, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO> handleGlobalException(Exception ex) {
        ApiResponseDTO response = new ApiResponseDTO(false, "An error occurred during server processing: " + ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
