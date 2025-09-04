package com.dodam.product.service;

import com.dodam.product.dto.response.BrandResponse;
import com.dodam.product.dto.response.ProductResponse;
import com.dodam.product.entity.Brand;
import com.dodam.product.repository.BrandRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 브랜드 서비스 구현체
 * 
 * <p>브랜드 관련 비즈니스 로직을 구현합니다.</p>
 * 
 * @since 1.0.0
 */
@Service
@Transactional(readOnly = true)
public class BrandServiceImpl implements BrandService {
    
    private final BrandRepository brandRepository;
    private final ProductService productService;
    
    public BrandServiceImpl(BrandRepository brandRepository, ProductService productService) {
        this.brandRepository = brandRepository;
        this.productService = productService;
    }
    
    @Override
    public List<BrandResponse> findAllActiveBrands() {
        List<Brand> brands = brandRepository.findByIsActiveTrueOrderByBrandNameAsc();
        
        return brands.stream()
            .map(brand -> {
                long productCount = productService.countByBrand(brand.getBrandId(), null);
                return BrandResponse.of(brand, productCount);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public BrandResponse findById(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
            .orElseThrow(() -> new IllegalArgumentException("브랜드를 찾을 수 없습니다. ID: " + brandId));
        
        // 해당 브랜드의 상품 개수 조회
        long productCount = productService.countByBrand(brandId, null);
        
        return BrandResponse.of(brand, productCount);
    }
    
    @Override
    public List<BrandResponse> findByBrandNameContaining(String brandName) {
        List<Brand> brands = brandRepository.findByBrandNameContainingAndIsActiveTrueOrderByBrandNameAsc(brandName);
        
        return brands.stream()
            .map(brand -> {
                long productCount = productService.countByBrand(brand.getBrandId(), null);
                return BrandResponse.of(brand, productCount);
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ProductResponse> findProductsByBrand(Long brandId, Pageable pageable) {
        // 브랜드 존재 여부 확인
        if (!existsById(brandId)) {
            throw new IllegalArgumentException("브랜드를 찾을 수 없습니다. ID: " + brandId);
        }
        
        return productService.findByBrand(brandId, pageable);
    }
    
    @Override
    public boolean existsById(Long brandId) {
        return brandRepository.existsById(brandId);
    }
    
    @Override
    public boolean isActiveBrand(Long brandId) {
        return brandRepository.findById(brandId)
            .map(Brand::isActive)
            .orElse(false);
    }
}