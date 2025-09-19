package com.dodam.notification.controller;

import com.dodam.notification.entity.NotificationType;
import com.dodam.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/notifications")
public class NotificationAdminController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public String viewPage() {
        return "admin/notifications/notifications";
    }
}

@RestController
@RequestMapping("/api/admin/notifications")
@CrossOrigin(origins = "*")
class NotificationApiController {
    
    @Autowired
    private NotificationService notificationService;
    
    // 시스템 전체 공지사항 발송
    @PostMapping("/system-announcement")
    public ResponseEntity<Map<String, String>> sendSystemAnnouncement(@RequestBody SystemAnnouncementRequest request) {
        notificationService.sendBulkNotification(
                request.getRecipientIds(),
                request.getTitle(),
                request.getContent(),
                NotificationType.SYSTEM,
                request.getRelatedUrl()
        );
        
        return ResponseEntity.ok(Map.of("message", "시스템 공지사항이 발송되었습니다."));
    }
    
    // 관리자 알림 발송
    @PostMapping("/admin-notification")
    public ResponseEntity<Map<String, String>> sendAdminNotification(@RequestBody AdminNotificationRequest request) {
        notificationService.sendBulkNotification(
                request.getRecipientIds(),
                request.getTitle(),
                request.getContent(),
                NotificationType.ADMIN,
                request.getRelatedUrl()
        );
        
        return ResponseEntity.ok(Map.of("message", "관리자 알림이 발송되었습니다."));
    }
    
    // 이벤트 알림 발송
    @PostMapping("/event-notification")
    public ResponseEntity<Map<String, String>> sendEventNotification(@RequestBody EventNotificationRequest request) {
        notificationService.sendBulkNotification(
                request.getRecipientIds(),
                request.getTitle(),
                request.getContent(),
                NotificationType.EVENT,
                request.getRelatedUrl()
        );
        
        return ResponseEntity.ok(Map.of("message", "이벤트 알림이 발송되었습니다."));
    }
    
    // 경고 알림 발송
    @PostMapping("/warning-notification")
    public ResponseEntity<Map<String, String>> sendWarningNotification(@RequestBody WarningNotificationRequest request) {
        notificationService.sendBulkNotification(
                request.getRecipientIds(),
                "[경고] " + request.getTitle(),
                request.getContent(),
                NotificationType.WARNING,
                request.getRelatedUrl()
        );
        
        return ResponseEntity.ok(Map.of("message", "경고 알림이 발송되었습니다."));
    }
    
    // 오래된 알림 정리
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldNotifications(@RequestParam(defaultValue = "30") int daysAgo) {
        int deletedCount = notificationService.cleanupOldNotifications(daysAgo);
        
        return ResponseEntity.ok(Map.of(
                "message", daysAgo + "일 이전의 읽은 알림을 정리했습니다.",
                "deletedCount", deletedCount
        ));
    }
    
    // 요청 DTO 클래스들
    public static class SystemAnnouncementRequest {
        private List<Long> recipientIds;
        private String title;
        private String content;
        private String relatedUrl;
        
        // Getter and Setter
        public List<Long> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<Long> recipientIds) { this.recipientIds = recipientIds; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getRelatedUrl() { return relatedUrl; }
        public void setRelatedUrl(String relatedUrl) { this.relatedUrl = relatedUrl; }
    }
    
    public static class AdminNotificationRequest {
        private List<Long> recipientIds;
        private String title;
        private String content;
        private String relatedUrl;
        
        // Getter and Setter
        public List<Long> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<Long> recipientIds) { this.recipientIds = recipientIds; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getRelatedUrl() { return relatedUrl; }
        public void setRelatedUrl(String relatedUrl) { this.relatedUrl = relatedUrl; }
    }
    
    public static class EventNotificationRequest {
        private List<Long> recipientIds;
        private String title;
        private String content;
        private String relatedUrl;
        
        // Getter and Setter
        public List<Long> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<Long> recipientIds) { this.recipientIds = recipientIds; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getRelatedUrl() { return relatedUrl; }
        public void setRelatedUrl(String relatedUrl) { this.relatedUrl = relatedUrl; }
    }
    
    public static class WarningNotificationRequest {
        private List<Long> recipientIds;
        private String title;
        private String content;
        private String relatedUrl;
        
        // Getter and Setter
        public List<Long> getRecipientIds() { return recipientIds; }
        public void setRecipientIds(List<Long> recipientIds) { this.recipientIds = recipientIds; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getRelatedUrl() { return relatedUrl; }
        public void setRelatedUrl(String relatedUrl) { this.relatedUrl = relatedUrl; }
    }
}