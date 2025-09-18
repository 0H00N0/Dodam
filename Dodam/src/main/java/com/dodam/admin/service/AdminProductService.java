package com.dodam.admin.service;

import com.dodam.admin.dto.AdminProductRequestDTO;
import com.dodam.admin.dto.ProductDetailResponseDTO;
import com.dodam.admin.dto.ProductListResponseDTO;
import com.dodam.product.entity.CategoryEntity;
import com.dodam.product.entity.ProductEntity;
import com.dodam.product.entity.ProductImageEntity;
import com.dodam.product.entity.ProstateEntity;
import com.dodam.product.repository.CategoryRepository;
import com.dodam.product.repository.ProductRepository;
import com.dodam.product.repository.ProstateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProstateRepository prostateRepository;

    @Transactional
    public ProductEntity createProduct(AdminProductRequestDTO requestDTO) {
        // 1. 연관 엔티티 조회
        CategoryEntity category = categoryRepository.findById(requestDTO.getCatenum())
                .orElseThrow(() -> new EntityNotFoundException("해당 카테고리를 찾을 수 없습니다. ID: " + requestDTO.getCatenum()));

        ProstateEntity prostate = prostateRepository.findById(requestDTO.getProsnum())
                .orElseThrow(() -> new EntityNotFoundException("해당 상품 상태를 찾을 수 없습니다. ID: " + requestDTO.getProsnum()));

        // 2. DTO -> Entity 변환 (모든 필드 포함)
        ProductEntity newProduct = ProductEntity.builder()
                .proname(requestDTO.getProname()) // 👈 이 부분이 누락되었을 가능성이 매우 높습니다.
                .prodetail(requestDTO.getProdetail())
                .proprice(requestDTO.getProprice())
                .proborrow(requestDTO.getProborrow())
                .probrand(requestDTO.getProbrand())
                .promade(requestDTO.getPromade())
                .proage(requestDTO.getProage())
                .procertif(requestDTO.getProcertif())
                .prodate(requestDTO.getProdate())
                .resernum(requestDTO.getResernum())
                .ctnum(requestDTO.getCtnum())
                .category(category)
                .prostate(prostate)
                .images(new ArrayList<>())
                .build();

        // 3. 이미지 정보 처리
        if (requestDTO.getImageName() != null && !requestDTO.getImageName().isEmpty()) {
            ProductImageEntity productImage = ProductImageEntity.builder()
                    .proimageorder(1)
                    .prourl(requestDTO.getImageName())
                    .prodetailimage(requestDTO.getImageName())
                    .product(newProduct)
                    .category(category)
                    .build();
            newProduct.getImages().add(productImage);
        }

        // 4. DB에 저장
        return productRepository.save(newProduct);
        
    }
    /**
     * 모든 상품 목록을 조회하여 DTO 리스트로 반환합니다.
     * @return 상품 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<ProductListResponseDTO> findAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductListResponseDTO::new)
                .collect(Collectors.toList());
    }
    /**
     * ID로 특정 상품의 상세 정보를 조회합니다.
     * @param productId 조회할 상품의 ID
     * @return 상품 상세 정보 DTO
     */
    @Transactional(readOnly = true)
    public ProductDetailResponseDTO findProductById(Long productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 상품을 찾을 수 없습니다: " + productId));
        return new ProductDetailResponseDTO(product);
    }
    
    
}