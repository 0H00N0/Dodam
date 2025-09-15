package com.dodam.admin.controller;

import com.dodam.admin.dto.AdminBrandDto;
import com.dodam.admin.dto.ApiResponseDTO;
import com.dodam.admin.service.AdminBrandService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/brands")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminBrandController {

    private final AdminBrandService adminBrandService;

    @PostMapping
    public ResponseEntity<AdminBrandDto.Response> createBrand(@RequestBody AdminBrandDto.CreateRequest requestDto) {
        AdminBrandDto.Response responseDto = adminBrandService.createBrand(requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AdminBrandDto.Response>> getAllBrands() {
        List<AdminBrandDto.Response> brands = adminBrandService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<AdminBrandDto.Response> getBrandById(@PathVariable Long brandId) {
        AdminBrandDto.Response brand = adminBrandService.getBrand(brandId);
        return ResponseEntity.ok(brand);
    }

    @PutMapping("/{brandId}")
    public ResponseEntity<AdminBrandDto.Response> updateBrand(@PathVariable Long brandId, @RequestBody AdminBrandDto.UpdateRequest requestDto) {
        AdminBrandDto.Response updatedBrand = adminBrandService.updateBrand(brandId, requestDto);
        return ResponseEntity.ok(updatedBrand);
    }

    @DeleteMapping("/{brandId}")
    public ResponseEntity<ApiResponseDTO> deleteBrand(@PathVariable Long brandId) {
        adminBrandService.deleteBrand(brandId);
        ApiResponseDTO response = new ApiResponseDTO(true, "Brand deleted successfully.");
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
