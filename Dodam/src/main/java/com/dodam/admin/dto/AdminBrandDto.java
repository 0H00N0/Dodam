package com.dodam.admin.dto;

import com.dodam.product.entity.Brand;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AdminBrandDto {

    @Getter
    @NoArgsConstructor 
    public static class CreateRequest {
        private String brandName;

        public Brand toEntity() {
            return Brand.builder()
                    .brandName(this.brandName)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String brandName;
    }

    @Getter
    @Builder
    public static class Response {
        private Long brandId;
        private String brandName;

        public static Response fromEntity(Brand brand) {
            return Response.builder()
                    .brandId(brand.getBrandId())
                    .brandName(brand.getBrandName())
                    .build();
        }
    }
}
