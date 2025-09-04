package com.dodam.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity //알림 센터 엔티티
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long recipientId; // 수신자 ID (멤버 ID)
    
    @Column(nullable = false)
    private String title; // 알림 제목
    
    @Column(columnDefinition = "TEXT")
    private String content; // 알림 내용
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // 알림 타입
    
    @Column(nullable = false)
    private Boolean isRead = false; // 읽음 여부
    
    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성 시간
    
    private LocalDateTime readAt; // 읽은 시간
    
    private String relatedUrl; // 관련 URL (클릭 시 이동할 페이지)
    
    private String metadata; // 추가 메타데이터 (JSON 형태)
    
    // 생성자
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Notification(Long recipientId, String title, String content, NotificationType type) {
        this();
        this.recipientId = recipientId;
        this.title = title;
        this.content = content;
        this.type = type;
    }
    
    // Getter and Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getRecipientId() {
        return recipientId;
    }
    
    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (isRead && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    public String getRelatedUrl() {
        return relatedUrl;
    }
    
    public void setRelatedUrl(String relatedUrl) {
        this.relatedUrl = relatedUrl;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
    
    // 편의 메서드
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}