package com.dodam.product.service;

import com.dodam.product.dto.request.ImageUploadRequest;
import com.dodam.product.dto.response.ProductImageResponse;
import com.dodam.product.dto.response.ImageMetadata;
import com.dodam.product.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 상품 이미지 관리 서비스 구현체
 * 파일 업로드, 썸네일 생성, 이미지 처리 등의 기능을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final FileStorageProperties fileStorageProperties;
    
    // 비동기 작업을 위한 스레드 풀
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    // 임시로 사용할 더미 데이터 저장소 (실제로는 JPA Repository 사용)
    // 동시성 안전성을 위해 ConcurrentHashMap과 AtomicLong 사용
    private final Map<Long, ProductImageResponse> imageStorage = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> productImageMapping = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public ProductImageResponse uploadImage(Long productId, MultipartFile file, ImageUploadRequest request) {
        log.info("이미지 업로드 시작: productId={}, fileName={}", productId, file.getOriginalFilename());
        
        try {
            // 1. 파일 유효성 검증
            validateImageFile(file);
            
            // 2. 파일 저장
            String savedFileName = saveFile(file, request);
            
            // 3. 이미지 메타데이터 추출
            ImageMetadata metadata = extractImageMetadata(file, savedFileName);
            
            // 4. 썸네일 생성 (비동기)
            String thumbnailFileName = null;
            if (request.getGenerateThumbnail()) {
                thumbnailFileName = generateThumbnailAsync(savedFileName, request.getThumbnailWidth(), request.getThumbnailHeight());
            }
            
            // 5. 응답 객체 생성
            ProductImageResponse response = createImageResponse(productId, file, request, savedFileName, thumbnailFileName, metadata);
            
            // 6. 저장 (실제로는 JPA Repository 사용)
            imageStorage.put(response.getId(), response);
            productImageMapping.computeIfAbsent(productId, k -> new CopyOnWriteArrayList<>()).add(response.getId());
            
            // 7. 대표 이미지 설정
            if (request.getIsMain()) {
                setMainImage(productId, response.getId());
            }
            
            log.info("이미지 업로드 완료: imageId={}", response.getId());
            return response;
            
        } catch (Exception e) {
            log.error("이미지 업로드 실패: productId={}, error={}", productId, e.getMessage(), e);
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductImageResponse> uploadMultipleImages(Long productId, List<MultipartFile> files, List<ImageUploadRequest> requests) {
        log.info("다중 이미지 업로드 시작: productId={}, fileCount={}", productId, files.size());
        
        if (files.size() != requests.size()) {
            throw new IllegalArgumentException("파일 개수와 요청 정보 개수가 일치하지 않습니다");
        }
        
        if (files.size() > fileStorageProperties.getMaxFileCount()) {
            throw new IllegalArgumentException("최대 " + fileStorageProperties.getMaxFileCount() + "개까지 업로드 가능합니다");
        }
        
        List<ProductImageResponse> responses = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            try {
                ProductImageResponse response = uploadImage(productId, files.get(i), requests.get(i));
                responses.add(response);
            } catch (Exception e) {
                log.error("다중 업로드 중 오류 발생: index={}, error={}", i, e.getMessage());
                // 이미 업로드된 파일들 정리
                responses.forEach(r -> deleteImage(r.getId()));
                throw new RuntimeException("다중 이미지 업로드 실패: " + e.getMessage(), e);
            }
        }
        
        log.info("다중 이미지 업로드 완료: uploadedCount={}", responses.size());
        return responses;
    }

    @Override
    public void deleteImage(Long imageId) {
        log.info("이미지 삭제 시작: imageId={}", imageId);
        
        ProductImageResponse image = imageStorage.get(imageId);
        if (image == null) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다: " + imageId);
        }
        
        try {
            // 1. 물리적 파일 삭제
            deletePhysicalFile(image.getFileName());
            
            // 2. 썸네일 파일 삭제
            if (image.getThumbnailUrl() != null) {
                String thumbnailFileName = extractFileNameFromUrl(image.getThumbnailUrl());
                deletePhysicalFile(thumbnailFileName);
            }
            
            // 3. 데이터 삭제
            imageStorage.remove(imageId);
            productImageMapping.get(image.getProductId()).remove(imageId);
            
            log.info("이미지 삭제 완료: imageId={}", imageId);
            
        } catch (Exception e) {
            log.error("이미지 삭제 실패: imageId={}, error={}", imageId, e.getMessage());
            throw new RuntimeException("이미지 삭제에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAllProductImages(Long productId) {
        log.info("상품의 모든 이미지 삭제 시작: productId={}", productId);
        
        List<Long> imageIds = productImageMapping.get(productId);
        if (imageIds != null) {
            new ArrayList<>(imageIds).forEach(this::deleteImage);
        }
        
        log.info("상품의 모든 이미지 삭제 완료: productId={}", productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(Long productId) {
        List<Long> imageIds = productImageMapping.getOrDefault(productId, new CopyOnWriteArrayList<>());
        return imageIds.stream()
                .map(imageStorage::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ProductImageResponse::getOrderIndex))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getImagesByType(Long productId, String imageType) {
        return getProductImages(productId).stream()
                .filter(image -> imageType.equals(image.getImageType()))
                .toList();
    }

    @Override
    public ProductImageResponse generateThumbnail(Long imageId, int width, int height) {
        log.info("썸네일 생성 시작: imageId={}, size={}x{}", imageId, width, height);
        
        ProductImageResponse image = imageStorage.get(imageId);
        if (image == null) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다: " + imageId);
        }
        
        try {
            String thumbnailFileName = generateThumbnailSync(image.getFileName(), width, height);
            
            // 이미지 정보 업데이트
            image.setThumbnailUrl(fileStorageProperties.buildThumbnailUrl(thumbnailFileName));
            image.setThumbnailWidth(width);
            image.setThumbnailHeight(height);
            
            log.info("썸네일 생성 완료: imageId={}, thumbnailFile={}", imageId, thumbnailFileName);
            return image;
            
        } catch (Exception e) {
            log.error("썸네일 생성 실패: imageId={}, error={}", imageId, e.getMessage());
            throw new RuntimeException("썸네일 생성에 실패했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateImageOrder(Long imageId, int newOrder) {
        ProductImageResponse image = imageStorage.get(imageId);
        if (image == null) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다: " + imageId);
        }
        
        image.setOrderIndex(newOrder);
        log.info("이미지 순서 변경: imageId={}, newOrder={}", imageId, newOrder);
    }

    @Override
    public void reorderImages(Long productId, List<Long> imageIds) {
        log.info("이미지 순서 일괄 변경: productId={}, imageCount={}", productId, imageIds.size());
        
        for (int i = 0; i < imageIds.size(); i++) {
            updateImageOrder(imageIds.get(i), i + 1);
        }
    }

    @Override
    public void setMainImage(Long productId, Long imageId) {
        log.info("대표 이미지 설정: productId={}, imageId={}", productId, imageId);
        
        // 기존 대표 이미지 해제
        getProductImages(productId).forEach(image -> image.setIsMain(false));
        
        // 새 대표 이미지 설정
        ProductImageResponse image = imageStorage.get(imageId);
        if (image != null && image.getProductId().equals(productId)) {
            image.setIsMain(true);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageResponse getMainImage(Long productId) {
        return getProductImages(productId).stream()
                .filter(ProductImageResponse::getIsMain)
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageMetadata getImageMetadata(Long imageId) {
        ProductImageResponse image = imageStorage.get(imageId);
        return image != null ? image.getMetadata() : null;
    }

    @Override
    public void updateImageType(Long imageId, String newType) {
        ProductImageResponse image = imageStorage.get(imageId);
        if (image == null) {
            throw new IllegalArgumentException("존재하지 않는 이미지입니다: " + imageId);
        }
        
        image.setImageType(newType);
        log.info("이미지 타입 변경: imageId={}, newType={}", imageId, newType);
    }

    @Override
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 없거나 비어있습니다");
        }
        
        // 파일 크기 검증
        if (file.getSize() > fileStorageProperties.getMaxFileSize()) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 크기: " + 
                fileStorageProperties.getMaxFileSize() / (1024 * 1024) + "MB");
        }
        
        // MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !fileStorageProperties.isAllowedMimeType(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + contentType);
        }
        
        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename);
            if (!fileStorageProperties.isAllowedExtension(extension)) {
                throw new IllegalArgumentException("지원하지 않는 파일 확장자입니다: " + extension);
            }
        }
    }

    @Override
    public String generateImageUrl(String filename, String imageType) {
        if ("THUMBNAIL".equals(imageType)) {
            return fileStorageProperties.buildThumbnailUrl(filename);
        }
        return fileStorageProperties.buildImageUrl(filename);
    }

    @Override
    public List<ProductImageResponse> confirmTempImages(Long productId, List<Long> tempImageIds) {
        log.info("임시 이미지 확정: productId={}, tempImageCount={}", productId, tempImageIds.size());
        
        List<ProductImageResponse> confirmedImages = new ArrayList<>();
        
        for (Long imageId : tempImageIds) {
            ProductImageResponse image = imageStorage.get(imageId);
            if (image != null && image.getIsTemporary()) {
                image.setIsTemporary(false);
                image.setModifiedAt(LocalDateTime.now());
                confirmedImages.add(image);
            }
        }
        
        log.info("임시 이미지 확정 완료: confirmedCount={}", confirmedImages.size());
        return confirmedImages;
    }

    // === Private 유틸리티 메서드들 ===

    /**
     * 파일을 디스크에 저장
     */
    private String saveFile(MultipartFile file, ImageUploadRequest request) throws IOException {
        // UUID를 사용한 고유 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        
        String filename = Optional.ofNullable(request.getFilenamePrefix()).orElse("") +
                         uuid +
                         Optional.ofNullable(request.getFilenameSuffix()).orElse("") +
                         "." + extension;
        
        // 저장 경로 생성
        Path uploadPath = Paths.get(fileStorageProperties.getFullUploadPath());
        if (request.getIsTemporary()) {
            uploadPath = Paths.get(fileStorageProperties.getFullTempPath());
        }
        
        Files.createDirectories(uploadPath);
        
        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.debug("파일 저장 완료: {}", filePath.toString());
        return filename;
    }

    /**
     * 이미지 메타데이터 추출
     */
    private ImageMetadata extractImageMetadata(MultipartFile file, String savedFileName) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            
            return ImageMetadata.builder()
                    .fileName(savedFileName)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .width(bufferedImage != null ? bufferedImage.getWidth() : null)
                    .height(bufferedImage != null ? bufferedImage.getHeight() : null)
                    .checksum(calculateChecksum(file))
                    .createdAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            log.warn("메타데이터 추출 실패: {}", e.getMessage());
            return ImageMetadata.builder()
                    .fileName(savedFileName)
                    .originalFileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .createdAt(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 응답 객체 생성
     */
    private ProductImageResponse createImageResponse(Long productId, MultipartFile file, 
                                                   ImageUploadRequest request, String savedFileName,
                                                   String thumbnailFileName, ImageMetadata metadata) {
        return ProductImageResponse.builder()
                .id(idGenerator.getAndIncrement())
                .productId(productId)
                .fileName(savedFileName)
                .originalFileName(file.getOriginalFilename())
                .imageType(request.getImageType())
                .imageUrl(generateImageUrl(savedFileName, request.getImageType()))
                .thumbnailUrl(thumbnailFileName != null ? generateImageUrl(thumbnailFileName, "THUMBNAIL") : null)
                .orderIndex(request.getOrderIndex())
                .description(request.getDescription())
                .title(request.getTitle())
                .isMain(request.getIsMain())
                .isTemporary(request.getIsTemporary())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .width(metadata.getWidth())
                .height(metadata.getHeight())
                .thumbnailWidth(request.getThumbnailWidth())
                .thumbnailHeight(request.getThumbnailHeight())
                .checksum(metadata.getChecksum())
                .tags(request.getTags())
                .uploadedAt(LocalDateTime.now())
                .metadata(metadata)
                .build();
    }

    /**
     * 비동기 썸네일 생성
     */
    private String generateThumbnailAsync(String originalFileName, int width, int height) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return generateThumbnailSync(originalFileName, width, height);
                } catch (Exception e) {
                    log.error("비동기 썸네일 생성 실패: {}", e.getMessage());
                    return null;
                }
            }, executorService).get();
        } catch (Exception e) {
            log.error("썸네일 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 동기 썸네일 생성
     */
    private String generateThumbnailSync(String originalFileName, int width, int height) throws IOException {
        Path originalPath = Paths.get(fileStorageProperties.getFullUploadPath()).resolve(originalFileName);
        Path thumbnailDir = Paths.get(fileStorageProperties.getFullThumbnailPath());
        Files.createDirectories(thumbnailDir);
        
        String thumbnailFileName = "thumb_" + originalFileName;
        Path thumbnailPath = thumbnailDir.resolve(thumbnailFileName);
        
        BufferedImage originalImage = ImageIO.read(originalPath.toFile());
        BufferedImage thumbnailImage = createThumbnail(originalImage, width, height);
        
        String format = getFileExtension(originalFileName);
        ImageIO.write(thumbnailImage, format, thumbnailPath.toFile());
        
        return thumbnailFileName;
    }

    /**
     * 썸네일 이미지 생성 (비율 유지하며 크롭)
     */
    private BufferedImage createThumbnail(BufferedImage original, int targetWidth, int targetHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        
        // 비율 계산
        double scaleX = (double) targetWidth / originalWidth;
        double scaleY = (double) targetHeight / originalHeight;
        double scale = Math.max(scaleX, scaleY);
        
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        
        // 스케일링
        BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();
        
        // 크롭
        int x = (scaledWidth - targetWidth) / 2;
        int y = (scaledHeight - targetHeight) / 2;
        
        return scaledImage.getSubimage(x, y, targetWidth, targetHeight);
    }

    /**
     * 물리적 파일 삭제
     */
    private void deletePhysicalFile(String filename) {
        try {
            Path filePath = Paths.get(fileStorageProperties.getFullUploadPath()).resolve(filename);
            Files.deleteIfExists(filePath);
            
            // 썸네일도 같이 삭제 시도
            Path thumbnailPath = Paths.get(fileStorageProperties.getFullThumbnailPath()).resolve("thumb_" + filename);
            Files.deleteIfExists(thumbnailPath);
            
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}, error={}", filename, e.getMessage());
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String url) {
        if (url == null) return null;
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 파일 체크섬 계산 (SHA-256)
     */
    private String calculateChecksum(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(file.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            log.warn("체크섬 계산 실패: {}", e.getMessage());
            return null;
        }
    }
}