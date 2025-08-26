package com.dodam.product.service;

import com.dodam.product.dto.request.ImageUploadRequest;
import com.dodam.product.dto.response.ProductImageResponse;
import com.dodam.product.dto.response.ImageMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 상품 이미지 관리를 위한 서비스 인터페이스
 * 이미지 업로드, 삭제, 순서 관리, 썸네일 생성 등의 기능을 제공합니다.
 */
public interface ProductImageService {

    /**
     * 단일 이미지를 업로드합니다.
     * 
     * @param productId 상품 ID
     * @param file 업로드할 이미지 파일
     * @param request 이미지 업로드 요청 정보
     * @return 업로드된 이미지 정보
     */
    ProductImageResponse uploadImage(Long productId, MultipartFile file, ImageUploadRequest request);

    /**
     * 다중 이미지를 업로드합니다.
     * 
     * @param productId 상품 ID
     * @param files 업로드할 이미지 파일들
     * @param requests 각 이미지의 업로드 요청 정보들
     * @return 업로드된 이미지 정보들
     */
    List<ProductImageResponse> uploadMultipleImages(Long productId, List<MultipartFile> files, List<ImageUploadRequest> requests);

    /**
     * 이미지를 삭제합니다.
     * 
     * @param imageId 삭제할 이미지 ID
     */
    void deleteImage(Long imageId);

    /**
     * 상품의 모든 이미지를 삭제합니다.
     * 
     * @param productId 상품 ID
     */
    void deleteAllProductImages(Long productId);

    /**
     * 상품의 이미지 목록을 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 상품의 이미지 목록 (순서대로)
     */
    List<ProductImageResponse> getProductImages(Long productId);

    /**
     * 특정 타입의 이미지 목록을 조회합니다.
     * 
     * @param productId 상품 ID
     * @param imageType 이미지 타입 (MAIN, DETAIL, REVIEW 등)
     * @return 해당 타입의 이미지 목록
     */
    List<ProductImageResponse> getImagesByType(Long productId, String imageType);

    /**
     * 썸네일을 생성합니다.
     * 
     * @param imageId 원본 이미지 ID
     * @param width 썸네일 너비
     * @param height 썸네일 높이
     * @return 썸네일 이미지 정보
     */
    ProductImageResponse generateThumbnail(Long imageId, int width, int height);

    /**
     * 이미지 순서를 변경합니다.
     * 
     * @param imageId 이미지 ID
     * @param newOrder 새로운 순서
     */
    void updateImageOrder(Long imageId, int newOrder);

    /**
     * 이미지들의 순서를 일괄 변경합니다.
     * 
     * @param productId 상품 ID
     * @param imageIds 새로운 순서대로 정렬된 이미지 ID 목록
     */
    void reorderImages(Long productId, List<Long> imageIds);

    /**
     * 대표 이미지로 설정합니다.
     * 
     * @param productId 상품 ID
     * @param imageId 대표 이미지로 설정할 이미지 ID
     */
    void setMainImage(Long productId, Long imageId);

    /**
     * 대표 이미지를 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 대표 이미지 정보 (없으면 null)
     */
    ProductImageResponse getMainImage(Long productId);

    /**
     * 이미지 메타데이터를 조회합니다.
     * 
     * @param imageId 이미지 ID
     * @return 이미지 메타데이터
     */
    ImageMetadata getImageMetadata(Long imageId);

    /**
     * 이미지의 타입을 변경합니다.
     * 
     * @param imageId 이미지 ID
     * @param newType 새로운 이미지 타입
     */
    void updateImageType(Long imageId, String newType);

    /**
     * 이미지 파일의 유효성을 검증합니다.
     * 
     * @param file 검증할 파일
     * @throws IllegalArgumentException 파일이 유효하지 않은 경우
     */
    void validateImageFile(MultipartFile file);

    /**
     * 이미지 URL을 생성합니다.
     * 
     * @param filename 파일명
     * @param imageType 이미지 타입
     * @return 이미지 접근 URL
     */
    String generateImageUrl(String filename, String imageType);

    /**
     * 임시 업로드된 이미지를 정식으로 등록합니다.
     * 
     * @param productId 상품 ID
     * @param tempImageIds 임시 이미지 ID들
     * @return 정식 등록된 이미지 정보들
     */
    List<ProductImageResponse> confirmTempImages(Long productId, List<Long> tempImageIds);
}