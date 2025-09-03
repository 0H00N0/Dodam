package com.dodam.notification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.dodam.notification")
public class NotificationExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationExceptionHandler.class);
    
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotificationNotFound(NotificationNotFoundException e) {
        logger.warn("알림을 찾을 수 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "알림을 찾을 수 없습니다.", "message", e.getMessage()));
    }
    
    @ExceptionHandler(NotificationAccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleNotificationAccessDenied(NotificationAccessDeniedException e) {
        logger.warn("알림 접근 권한 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "알림에 접근할 권한이 없습니다.", "message", e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        logger.error("알림 처리 중 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 오류가 발생했습니다.", "message", "잠시 후 다시 시도해주세요."));
    }
}

// 커스텀 예외 클래스들
class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(String message) {
        super(message);
    }
    
    public NotificationNotFoundException(Long notificationId) {
        super("알림을 찾을 수 없습니다. ID: " + notificationId);
    }
}

class NotificationAccessDeniedException extends RuntimeException {
    public NotificationAccessDeniedException(String message) {
        super(message);
    }
    
    public NotificationAccessDeniedException(Long userId, Long notificationId) {
        super("사용자 " + userId + "는 알림 " + notificationId + "에 접근할 권한이 없습니다.");
    }
}