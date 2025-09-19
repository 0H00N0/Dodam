package com.dodam.notification.scheduler;

import com.dodam.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    
    @Autowired
    private NotificationService notificationService;
    
    // 매일 오전 2시에 30일 이상 된 읽은 알림 정리
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldNotifications() {
        try {
            logger.info("오래된 알림 정리 작업 시작");
            int deletedCount = notificationService.cleanupOldNotifications(30);
            logger.info("오래된 알림 정리 완료: {} 개 알림 삭제", deletedCount);
        } catch (Exception e) {
            logger.error("오래된 알림 정리 중 오류 발생", e);
        }
    }
    
    // 매주 일요일 오전 3시에 90일 이상 된 읽은 알림 정리 (더 오래된 것들)
    @Scheduled(cron = "0 0 3 * * SUN")
    public void deepCleanupOldNotifications() {
        try {
            logger.info("심화 알림 정리 작업 시작");
            int deletedCount = notificationService.cleanupOldNotifications(90);
            logger.info("심화 알림 정리 완료: {} 개 알림 삭제", deletedCount);
        } catch (Exception e) {
            logger.error("심화 알림 정리 중 오류 발생", e);
        }
    }
}