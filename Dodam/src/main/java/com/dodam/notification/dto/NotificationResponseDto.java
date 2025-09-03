package com.dodam.notification.dto;

import com.dodam.notification.entity.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

// 알림 응답 DTO
public class NotificationResponseDto {
    private Long id;
    private Long recipientId;
    private String title;
    private String content;
    private NotificationType type;
    private Boolean isRead;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;
    
    private String relatedUrl;
    private String metadata;
    
    // 기본 생성자
    public NotificationResponseDto() {}
    
    // 전체 생성자
    public NotificationResponseDto(Long id, Long recipientId, String title, String content, 
                                 NotificationType type, Boolean isRead, LocalDateTime createdAt, 
                                 LocalDateTime readAt, String relatedUrl, String metadata) {
        this.id = id;
        this.recipientId = recipientId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.relatedUrl = relatedUrl;
        this.metadata = metadata;
    }
    
    // Getter and Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    
    public String getRelatedUrl() { return relatedUrl; }
    public void setRelatedUrl(String relatedUrl) { this.relatedUrl = relatedUrl; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}

// 알림 생성 요청 DTO
class NotificationCreateDto {
    private Long recipientId;
    private String title;
    private String content;
    private NotificationType type;
    private String relatedUrl;
    private String metadata;
    
    // 기본 생성자
    public NotificationCreateDto() {}
    
    // 생성자
    public NotificationCreateDto(Long recipientId, String title, String content, NotificationType type) {
        this.recipientId = recipientId;
        this.title = title;
        this.content = content;
        this.type = type;
    }
    
    // Getter and Setter
    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    
    public String getRelatedUrl() { return relatedUrl; }
    public void setRelatedUrl(String relatedUrl) { this.relatedUrl = relatedUrl; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}

// 알림 통계 DTO
class NotificationStatsDto {
    private long totalCount;
    private long unreadCount;
    private long readCount;
    
    public NotificationStatsDto(long totalCount, long unreadCount, long readCount) {
        this.totalCount = totalCount;
        this.unreadCount = unreadCount;
        this.readCount = readCount;
    }
    
    // Getter and Setter
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    
    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
    
    public long getReadCount() { return readCount; }
    public void setReadCount(long readCount) { this.readCount = readCount; }
}