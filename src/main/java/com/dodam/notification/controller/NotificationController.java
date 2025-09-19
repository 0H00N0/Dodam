package com.dodam.notification.controller;

import com.dodam.notification.dto.NotificationResponseDto;
import com.dodam.notification.entity.NotificationType;
import com.dodam.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    // 특정 사용자의 알림 목록 조회 (페이징)
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponseDto>> getNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<NotificationResponseDto> notifications = notificationService.getNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }
    
    // 특정 사용자의 읽지 않은 알림 목록 조회
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadNotifications(@PathVariable Long userId) {
        List<NotificationResponseDto> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    // 읽지 않은 알림 개수 조회
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    // 특정 타입의 알림 조회
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByType(
            @PathVariable Long userId,
            @PathVariable NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByType(userId, type, page, size);
        return ResponseEntity.ok(notifications);
    }
    
    // 알림 상세 조회
    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponseDto> getNotification(@PathVariable Long notificationId) {
        Optional<NotificationResponseDto> notification = notificationService.getNotification(notificationId);
        
        if (notification.isPresent()) {
            return ResponseEntity.ok(notification.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 알림 읽음 처리
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        
        boolean success = notificationService.markAsRead(notificationId, userId);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "알림이 읽음 처리되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "알림을 찾을 수 없거나 권한이 없습니다."));
        }
    }
    
    // 모든 알림 읽음 처리
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(@PathVariable Long userId) {
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of(
                "message", "모든 알림이 읽음 처리되었습니다.",
                "updatedCount", updatedCount
        ));
    }
    
    // 특정 타입의 모든 알림 읽음 처리
    @PutMapping("/user/{userId}/type/{type}/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsReadByType(
            @PathVariable Long userId,
            @PathVariable NotificationType type) {
        
        int updatedCount = notificationService.markAllAsReadByType(userId, type);
        return ResponseEntity.ok(Map.of(
                "message", type.getDescription() + " 알림이 모두 읽음 처리되었습니다.",
                "updatedCount", updatedCount
        ));
    }
    
    // 알림 삭제
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        
        boolean success = notificationService.deleteNotification(notificationId, userId);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "알림이 삭제되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "알림을 찾을 수 없거나 권한이 없습니다."));
        }
    }
    
    // 사용자의 모든 알림 삭제
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteAllNotifications(@PathVariable Long userId) {
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok(Map.of("message", "모든 알림이 삭제되었습니다."));
    }
    
    // 알림 생성 (개발/테스트용)
    @PostMapping("/create")
    public ResponseEntity<NotificationResponseDto> createNotification(@RequestBody CreateNotificationRequest request) {
        NotificationResponseDto notification = notificationService.createNotification(
                request.getRecipientId(),
                request.getTitle(),
                request.getContent(),
                request.getType(),
                request.getRelatedUrl(),
                request.getMetadata()
        );
        
        return ResponseEntity.ok(notification);
    }
    
    // 대량 알림 발송 (관리자용)
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, String>> sendBulkNotification(@RequestBody BulkNotificationRequest request) {
        notificationService.sendBulkNotification(
                request.getRecipientIds(),
                request.getTitle(),
                request.getContent(),
                request.getType(),
                request.getRelatedUrl()
        );
        
        return ResponseEntity.ok(Map.of("message", "대량 알림이 발송되었습니다."));
    }
    
    // 요청 DTO 클래스들
    public static class CreateNotificationRequest {
        private Long recipientId;
        private String title;
        private String content;
        private NotificationType type;
        private String relatedUrl;
        private String metadata;
        
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
    
    public static class BulkNotificationRequest {
        private List<Long> recipientIds;
        private String title;
        private String content;
        private NotificationType type;
        private String relatedUrl;
        
        // Getter and Setter
        public List<Long> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<Long> recipientIds) { this.recipientIds = recipientIds; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }
        
        public String getRelatedUrl() { return relatedUrl; }
        public void setRelatedUrl(String relatedUrl) { this.relatedUrl = relatedUrl; }
    }
}