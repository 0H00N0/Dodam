package com.dodam.notification.service;

import com.dodam.notification.dto.NotificationResponseDto;
import com.dodam.notification.entity.Notification;
import com.dodam.notification.entity.NotificationType;
import com.dodam.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    // 알림 생성
    public NotificationResponseDto createNotification(Long recipientId, String title, String content, 
                                                    NotificationType type, String relatedUrl, String metadata) {
        Notification notification = new Notification(recipientId, title, content, type);
        notification.setRelatedUrl(relatedUrl);
        notification.setMetadata(metadata);
        
        Notification saved = notificationRepository.save(notification);
        return convertToDto(saved);
    }
    
    // 단순 알림 생성 (필수 정보만)
    public NotificationResponseDto createNotification(Long recipientId, String title, String content, NotificationType type) {
        return createNotification(recipientId, title, content, type, null, null);
    }
    
    // 특정 사용자의 알림 목록 조회 (페이징)
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotifications(Long recipientId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId, pageable);
        return notifications.map(this::convertToDto);
    }
    
    // 특정 사용자의 읽지 않은 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotifications(Long recipientId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(recipientId);
        return notifications.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    // 읽지 않은 알림 개수 조회
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long recipientId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
    }
    
    // 특정 타입의 알림 조회
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotificationsByType(Long recipientId, NotificationType type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByRecipientIdAndTypeOrderByCreatedAtDesc(recipientId, type, pageable);
        return notifications.map(this::convertToDto);
    }
    
    // 알림 상세 조회
    @Transactional(readOnly = true)
    public Optional<NotificationResponseDto> getNotification(Long notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        return notification.map(this::convertToDto);
    }
    
    // 알림 읽음 처리
    public boolean markAsRead(Long notificationId, Long recipientId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            
            // 해당 사용자의 알림인지 확인
            if (!notification.getRecipientId().equals(recipientId)) {
                return false;
            }
            
            if (!notification.getIsRead()) {
                notification.markAsRead();
                notificationRepository.save(notification);
            }
            return true;
        }
        return false;
    }
    
    // 모든 알림 읽음 처리
    public int markAllAsRead(Long recipientId) {
        return notificationRepository.markAllAsReadByRecipientId(recipientId, LocalDateTime.now());
    }
    
    // 특정 타입의 모든 알림 읽음 처리
    public int markAllAsReadByType(Long recipientId, NotificationType type) {
        return notificationRepository.markAllAsReadByRecipientIdAndType(recipientId, type, LocalDateTime.now());
    }
    
    // 알림 삭제
    public boolean deleteNotification(Long notificationId, Long recipientId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            
            // 해당 사용자의 알림인지 확인
            if (!notification.getRecipientId().equals(recipientId)) {
                return false;
            }
            
            notificationRepository.delete(notification);
            return true;
        }
        return false;
    }
    
    // 사용자의 모든 알림 삭제
    public void deleteAllNotifications(Long recipientId) {
        notificationRepository.deleteByRecipientId(recipientId);
    }
    
    // 오래된 읽은 알림 정리 (관리자용)
    public int cleanupOldNotifications(int daysAgo) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysAgo);
        return notificationRepository.deleteOldReadNotifications(cutoffDate);
    }
    
    // 대량 알림 발송 (관리자용)
    public void sendBulkNotification(List<Long> recipientIds, String title, String content, 
                                   NotificationType type, String relatedUrl) {
        List<Notification> notifications = recipientIds.stream()
                .map(recipientId -> {
                    Notification notification = new Notification(recipientId, title, content, type);
                    notification.setRelatedUrl(relatedUrl);
                    return notification;
                })
                .collect(Collectors.toList());
        
        notificationRepository.saveAll(notifications);
    }
    
    // Entity to DTO 변환
    private NotificationResponseDto convertToDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getRecipientId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getType(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getRelatedUrl(),
                notification.getMetadata()
        );
    }
}
