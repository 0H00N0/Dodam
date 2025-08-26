package com.dodam.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.List;
import java.util.Arrays;

/**
 * 파일 저장 설정 Properties
 * application.yml에서 file.storage.* 설정값들을 바인딩합니다.
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
@Validated
public class FileStorageProperties {

    /**
     * 파일 업로드 루트 디렉토리
     * 예: /uploads, C:/uploads, ./uploads
     */
    @NotBlank(message = "업로드 디렉토리는 필수입니다")
    private String uploadDir = "uploads";

    /**
     * 이미지 저장 경로 (업로드 디렉토리 하위)
     */
    private String imagePath = "images";

    /**
     * 썸네일 저장 경로 (이미지 경로 하위)
     */
    private String thumbnailPath = "thumbnails";

    /**
     * 임시 파일 저장 경로
     */
    private String tempPath = "temp";

    /**
     * CDN 베이스 URL (있는 경우)
     */
    private String cdnBaseUrl;

    /**
     * 로컬 서버 베이스 URL
     */
    private String serverBaseUrl = "http://localhost:8080";

    /**
     * 최대 파일 크기 (bytes)
     * 기본값: 10MB
     */
    @NotNull
    @Min(value = 1024, message = "최대 파일 크기는 1KB 이상이어야 합니다")
    @Max(value = 100 * 1024 * 1024, message = "최대 파일 크기는 100MB 이하여야 합니다")
    private Long maxFileSize = 10 * 1024 * 1024L; // 10MB

    /**
     * 한 번에 업로드 가능한 최대 파일 개수
     */
    @Min(value = 1, message = "최대 파일 개수는 1개 이상이어야 합니다")
    @Max(value = 50, message = "최대 파일 개수는 50개 이하여야 합니다")
    private Integer maxFileCount = 10;

    /**
     * 허용되는 이미지 MIME 타입들
     */
    private List<String> allowedMimeTypes = Arrays.asList(
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/gif",
        "image/webp",
        "image/bmp",
        "image/tiff"
    );

    /**
     * 허용되는 파일 확장자들
     */
    private List<String> allowedExtensions = Arrays.asList(
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "tiff"
    );

    /**
     * 이미지 품질 설정
     */
    private ImageQuality imageQuality = new ImageQuality();

    /**
     * 썸네일 설정
     */
    private ThumbnailConfig thumbnail = new ThumbnailConfig();

    /**
     * 워터마크 설정
     */
    private WatermarkConfig watermark = new WatermarkConfig();

    /**
     * 정리 작업 설정
     */
    private CleanupConfig cleanup = new CleanupConfig();

    /**
     * 클라우드 스토리지 설정
     */
    private CloudStorage cloudStorage = new CloudStorage();

    @Data
    public static class ImageQuality {
        /**
         * JPEG 압축 품질 (1-100)
         */
        @Min(value = 1) @Max(value = 100)
        private Integer jpegQuality = 85;

        /**
         * PNG 압축 레벨 (0-9)
         */
        @Min(value = 0) @Max(value = 9)
        private Integer pngCompressionLevel = 6;

        /**
         * 자동 최적화 여부
         */
        private Boolean autoOptimize = true;

        /**
         * 최대 이미지 크기 (리사이징 기준)
         */
        private Integer maxWidth = 2048;
        private Integer maxHeight = 2048;
    }

    @Data
    public static class ThumbnailConfig {
        /**
         * 썸네일 자동 생성 여부
         */
        private Boolean autoGenerate = true;

        /**
         * 기본 썸네일 크기들
         */
        private List<ThumbnailSize> sizes = Arrays.asList(
            new ThumbnailSize("small", 150, 150),
            new ThumbnailSize("medium", 300, 300),
            new ThumbnailSize("large", 600, 600)
        );

        /**
         * 썸네일 품질
         */
        @Min(value = 1) @Max(value = 100)
        private Integer quality = 80;

        /**
         * 크기 조정 방식 (CROP, SCALE, FIT)
         */
        private String resizeMode = "CROP";
    }

    @Data
    public static class ThumbnailSize {
        private String name;
        private Integer width;
        private Integer height;

        public ThumbnailSize() {}

        public ThumbnailSize(String name, Integer width, Integer height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }
    }

    @Data
    public static class WatermarkConfig {
        /**
         * 워터마크 사용 여부
         */
        private Boolean enabled = false;

        /**
         * 워터마크 이미지 경로
         */
        private String imagePath;

        /**
         * 워터마크 텍스트
         */
        private String text = "© DODAM";

        /**
         * 워터마크 위치 (TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER)
         */
        private String position = "BOTTOM_RIGHT";

        /**
         * 워터마크 투명도 (0.0 ~ 1.0)
         */
        @Min(value = 0) @Max(value = 1)
        private Float opacity = 0.7f;

        /**
         * 워터마크 크기 (원본 이미지 대비 비율)
         */
        @Min(value = 0) @Max(value = 1)
        private Float scale = 0.1f;
    }

    @Data
    public static class CleanupConfig {
        /**
         * 임시 파일 정리 활성화
         */
        private Boolean enabled = true;

        /**
         * 임시 파일 보관 시간 (시간)
         */
        @Min(value = 1)
        private Integer tempFileRetentionHours = 24;

        /**
         * 미사용 파일 정리 활성화
         */
        private Boolean cleanupUnusedFiles = true;

        /**
         * 미사용 파일 보관 시간 (일)
         */
        @Min(value = 1)
        private Integer unusedFileRetentionDays = 30;

        /**
         * 정리 작업 실행 주기 (cron 표현식)
         */
        private String schedule = "0 0 2 * * ?"; // 매일 새벽 2시
    }

    @Data
    public static class CloudStorage {
        /**
         * 클라우드 스토리지 사용 여부
         */
        private Boolean enabled = false;

        /**
         * 스토리지 타입 (AWS_S3, GOOGLE_CLOUD, AZURE_BLOB)
         */
        private String type = "AWS_S3";

        /**
         * 버킷/컨테이너 이름
         */
        private String bucketName;

        /**
         * 리전
         */
        private String region;

        /**
         * 액세스 키
         */
        private String accessKey;

        /**
         * 시크릿 키
         */
        private String secretKey;

        /**
         * 엔드포인트 URL (커스텀 S3 호환 스토리지용)
         */
        private String endpoint;

        /**
         * 공개 URL 생성 여부
         */
        private Boolean generatePublicUrls = true;

        /**
         * 로컬 백업 유지 여부
         */
        private Boolean keepLocalBackup = false;
    }

    // 유틸리티 메서드

    /**
     * 전체 업로드 경로 반환
     */
    public String getFullUploadPath() {
        return uploadDir + "/" + imagePath;
    }

    /**
     * 전체 썸네일 경로 반환
     */
    public String getFullThumbnailPath() {
        return uploadDir + "/" + imagePath + "/" + thumbnailPath;
    }

    /**
     * 전체 임시 경로 반환
     */
    public String getFullTempPath() {
        return uploadDir + "/" + tempPath;
    }

    /**
     * 이미지 URL 생성
     */
    public String buildImageUrl(String filename) {
        String baseUrl = (cdnBaseUrl != null && !cdnBaseUrl.isEmpty()) ? cdnBaseUrl : serverBaseUrl;
        return baseUrl + "/" + imagePath + "/" + filename;
    }

    /**
     * 썸네일 URL 생성
     */
    public String buildThumbnailUrl(String filename) {
        String baseUrl = (cdnBaseUrl != null && !cdnBaseUrl.isEmpty()) ? cdnBaseUrl : serverBaseUrl;
        return baseUrl + "/" + imagePath + "/" + thumbnailPath + "/" + filename;
    }

    /**
     * MIME 타입 허용 여부 확인
     */
    public boolean isAllowedMimeType(String mimeType) {
        return allowedMimeTypes.contains(mimeType.toLowerCase());
    }

    /**
     * 파일 확장자 허용 여부 확인
     */
    public boolean isAllowedExtension(String extension) {
        return allowedExtensions.contains(extension.toLowerCase());
    }
}