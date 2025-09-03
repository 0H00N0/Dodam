package com.dodam.notification.repository;

import com.dodam.notification.entity.Notification;
import com.dodam.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // 특정 사용자의 알림 목록 조회 (최신순)
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    // 특정 사용자의 읽지 않은 알림 목록 조회
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId);
    
    // 특정 사용자의 읽지 않은 알림 개수 조회
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    
    // 특정 사용자의 특정 타입 알림 조회
    Page<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(Long recipientId, NotificationType type, Pageable pageable);
    
    // 특정 사용자의 읽은 알림만 조회
    Page<Notification> findByRecipientIdAndIsReadTrueOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    // 특정 사용자의 읽지 않은 알림만 조회
    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    
    // 특정 기간 내의 알림 조회
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndCreatedAtBetween(
            @Param("recipientId") Long recipientId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    // 특정 사용자의 모든 알림을 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId, @Param("readAt") LocalDateTime readAt);
    
    // 특정 사용자의 특정 타입 알림을 모두 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.recipientId = :recipientId AND n.type = :type AND n.isRead = false")
    int markAllAsReadByRecipientIdAndType(@Param("recipientId") Long recipientId, @Param("type") NotificationType type, @Param("readAt") LocalDateTime readAt);
    
    // 오래된 알림 삭제 (예: 30일 이상 된 읽은 알림)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // 특정 사용자의 알림 삭제
    void deleteByRecipientId(Long recipientId);
}