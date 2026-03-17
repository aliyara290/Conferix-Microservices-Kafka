package com.aliyara.notificationsservice.infrastructure.email;

import com.aliyara.notificationsservice.domain.entities.Notification;
import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.aliyara.notificationsservice.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    @Value("${app.notification.max-retries:3}")
    private int maxRetries;
    
    @Scheduled(fixedDelayString = "${app.notification.delay-seconds:5}000")
    @Transactional
    public void processePendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository
                .findByStatut(StatutNotification.EN_ATTENTE, PageRequest.of(0, 100))
                .getContent();
        
        if (!pendingNotifications.isEmpty()) {
            log.info("Traitement de {} notifications en attente", pendingNotifications.size());
            
            pendingNotifications.forEach(notification -> {
                try {
                    emailService.sendEmail(notification);
                } catch (Exception e) {
                    log.error("Erreur lors de l'envoi de la notification {}: {}", 
                            notification.getId(), e.getMessage());
                }
            });
        }
    }
    
    @Scheduled(fixedDelayString = "${app.notification.retry-interval:300}000")
    @Transactional
    public void retryFailedNotifications() {
        List<Notification> retryableNotifications = notificationRepository
                .findRetryableNotifications(maxRetries);
        
        if (!retryableNotifications.isEmpty()) {
            log.info("Réessai de {} notifications échouées", retryableNotifications.size());
            
            retryableNotifications.forEach(notification -> {
                try {
                    notification.incrementerTentatives();
                    notificationRepository.save(notification);
                    emailService.sendEmail(notification);
                } catch (Exception e) {
                    log.error("Erreur lors du réessai de la notification {}: {}", 
                            notification.getId(), e.getMessage());
                }
            });
        }
    }
    
    @Scheduled(cron = "${app.notification.cleanup-cron:0 0 2 * * *}")
    @Transactional
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Notification> oldNotifications = notificationRepository
                .findPendingNotificationsOlderThan(cutoffDate);
        
        if (!oldNotifications.isEmpty()) {
            log.info("Nettoyage de {} anciennes notifications", oldNotifications.size());
            notificationRepository.deleteAll(oldNotifications);
        }
    }
}
