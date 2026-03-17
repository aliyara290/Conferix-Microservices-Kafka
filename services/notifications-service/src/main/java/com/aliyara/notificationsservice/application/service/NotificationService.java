package com.aliyara.notificationsservice.application.service;

import com.aliyara.notificationsservice.application.dto.NotificationDTO;
import com.aliyara.notificationsservice.application.dto.NotificationRequest;
import com.aliyara.notificationsservice.application.dto.NotificationResponse;
import com.aliyara.notificationsservice.application.dto.NotificationStatsDTO;
import com.aliyara.notificationsservice.application.dto.PageResponse;
import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.aliyara.notificationsservice.domain.enums.TypeEvenement;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationService {
    
    NotificationResponse createNotification(NotificationRequest request);
    
    Optional<NotificationDTO> getNotificationById(Long id);
    
    PageResponse<NotificationDTO> getAllNotifications(Pageable pageable);
    
    PageResponse<NotificationDTO> getNotificationsByDestinataire(String destinataire, Pageable pageable);
    
    PageResponse<NotificationDTO> getNotificationsByStatut(StatutNotification statut, Pageable pageable);
    
    PageResponse<NotificationDTO> getNotificationsByTypeEvenement(TypeEvenement typeEvenement, Pageable pageable);
    
    NotificationStatsDTO getStatistics();
    
    void retryFailedNotification(Long id);
    
    void deleteOldNotifications(int days);
}
