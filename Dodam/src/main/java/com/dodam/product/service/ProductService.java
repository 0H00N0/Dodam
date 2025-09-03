package com.dodam.product.service;

import com.dodam.product.entity.CategoryEntity;
import com.dodam.product.entity.ProductEntity;
import com.dodam.product.repository.CategoryRepository;
import com.dodam.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductEntity> findAll() {
        return productRepository.findAll();
    }

    public Optional<ProductEntity> findById(Long id) {
        return productRepository.findById(id);
    }

    public ProductEntity save(ProductEntity product) {
        if (product.getCategory() != null && product.getCategory().getCatenum() != null) {
            CategoryEntity category = categoryRepository.findById(product.getCategory().getCatenum())
                    .orElseThrow(() -> new RuntimeException("카테고리 없음"));
            product.setCategory(category);
        }
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public List<CategoryEntity> getAllCategories() {
        return categoryRepository.findAll();
    }
}