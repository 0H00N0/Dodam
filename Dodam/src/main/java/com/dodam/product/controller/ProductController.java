package com.dodam.product.controller;

import com.dodam.product.dto.ProductDTO;
import com.dodam.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000") // (포트 3000에서 오는 요청 허용)
@RestController
@RequestMapping("/api/productsPage")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductDTO> list(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) Long catenum,
        @RequestParam(required = false) Long prosnum,
        @RequestParam(required = false) String prograde, // S/A/B/C
        @PageableDefault(size = 20, sort = "pronum", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return productService.searchByColumns(q, catenum, prosnum, prograde, pageable);
    }

    @GetMapping("/{pronum}")
    public ProductDTO get(@PathVariable Long pronum) {
        return productService.get(pronum);
    }

    @PostMapping
    public ProductDTO create(@RequestBody ProductDTO dto) {
        return productService.create(dto);
    }

    @PutMapping("/{pronum}")
    public ProductDTO update(@PathVariable Long pronum, @RequestBody ProductDTO dto) {
        dto.setPronum(pronum);
        return productService.update(dto);
    }

    @DeleteMapping("/{pronum}")
    public void delete(@PathVariable Long pronum) {
        productService.delete(pronum);
    }
}
