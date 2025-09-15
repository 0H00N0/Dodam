package com.dodam.admin.service;

import com.dodam.admin.dto.AdminBrandDto;
import com.dodam.product.entity.Brand;
import com.dodam.product.repository.BrandRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBrandService {

    private final BrandRepository brandRepository;

    @Transactional
    public AdminBrandDto.Response createBrand(AdminBrandDto.CreateRequest requestDto) {
        Brand brand = requestDto.toEntity();
        Brand savedBrand = brandRepository.save(brand);
        return AdminBrandDto.Response.fromEntity(savedBrand);
    }

    public List<AdminBrandDto.Response> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(AdminBrandDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    public AdminBrandDto.Response getBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + brandId));
        return AdminBrandDto.Response.fromEntity(brand);
    }

    @Transactional
    public AdminBrandDto.Response updateBrand(Long brandId, AdminBrandDto.UpdateRequest requestDto) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + brandId));
        brand.setBrandName(requestDto.getBrandName());
        Brand updatedBrand = brandRepository.save(brand);
        return AdminBrandDto.Response.fromEntity(updatedBrand);
    }

    @Transactional
    public void deleteBrand(Long brandId) {
        brandRepository.deleteById(brandId);
    }
}
