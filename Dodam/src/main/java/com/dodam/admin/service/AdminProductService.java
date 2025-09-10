package com.dodam.admin.service;

import com.dodam.admin.dto.AdminProductDto;
import com.dodam.product.entity.Brand;
import com.dodam.product.entity.Category;
import com.dodam.product.entity.Product;
import com.dodam.product.repository.BrandRepository;
import com.dodam.product.repository.CategoryRepository;
import com.dodam.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors; 
  
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public AdminProductDto.Response createProduct(AdminProductDto.CreateRequest requestDto) {
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + requestDto.getCategoryId()));
        Brand brand = brandRepository.findById(requestDto.getBrandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + requestDto.getBrandId()));

        Product product = requestDto.toEntity(category, brand);
        Product savedProduct = productRepository.save(product);
        return AdminProductDto.Response.fromEntity(savedProduct);
    }

    // Read All
    public List<AdminProductDto.Response> getAllProducts() {
        return productRepository.findAll().stream()
                .map(AdminProductDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    // Read One
    public AdminProductDto.Response getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
        return AdminProductDto.Response.fromEntity(product);
    }

    // Update
    @Transactional
    public AdminProductDto.Response updateProduct(Long productId, AdminProductDto.UpdateRequest requestDto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
 
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + requestDto.getCategoryId()));
        Brand brand = brandRepository.findById(requestDto.getBrandId())
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + requestDto.getBrandId()));

        product.setProductName(requestDto.getProductName());
        product.setPrice(requestDto.getPrice());
        product.setDescription(requestDto.getDescription());
        product.setStockQuantity(requestDto.getStockQuantity());
        product.setCategory(category);
        product.setBrand(brand);
        product.setStatus(requestDto.getStatus());

        Product updatedProduct = productRepository.save(product);
        return AdminProductDto.Response.fromEntity(updatedProduct);
    }

    // Delete
    @Transactional
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }
}
