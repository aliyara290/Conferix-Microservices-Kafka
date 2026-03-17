package com.aliyara.notificationsservice.application.service;

import com.aliyara.notificationsservice.application.dto.NotificationDTO;
import com.aliyara.notificationsservice.application.dto.NotificationRequest;
import com.aliyara.notificationsservice.application.dto.NotificationResponse;
import com.aliyara.notificationsservice.application.dto.NotificationStatsDTO;
import com.aliyara.notificationsservice.application.dto.PageResponse;
import com.aliyara.notificationsservice.application.mapper.NotificationMapper;
import com.aliyara.notificationsservice.domain.entities.Notification;
import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.aliyara.notificationsservice.domain.enums.TypeEvenement;
import com.aliyara.notificationsservice.infrastructure.email.EmailService;
import com.aliyara.notificationsservice.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    
    @Value("${notification.max-retries:3}")
    private int maxRetries;
    
    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for destinataire: {}", request.destinataire());
        
        Notification notification = notificationMapper.toEntity(request);
        Notification saved = notificationRepository.save(notification);
        
        emailService.sendEmail(saved);
        
        log.info("Notification created with id: {}", saved.getId());
        return notificationMapper.toResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationDTO> getNotificationById(Long id) {
        log.debug("Fetching notification by id: {}", id);
        return notificationRepository.findById(id)
                .map(notificationMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getAllNotifications(Pageable pageable) {
        log.debug("Fetching all notifications, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Notification> page = notificationRepository.findAll(pageable);
        return buildPageResponse(page);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getNotificationsByDestinataire(String destinataire, Pageable pageable) {
        log.debug("Fetching notifications for destinataire: {}", destinataire);
        Page<Notification> page = notificationRepository.findByDestinataire(destinataire, pageable);
        return buildPageResponse(page);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getNotificationsByStatut(StatutNotification statut, Pageable pageable) {
        log.debug("Fetching notifications by statut: {}", statut);
        Page<Notification> page = notificationRepository.findByStatut(statut, pageable);
        return buildPageResponse(page);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getNotificationsByTypeEvenement(TypeEvenement typeEvenement, Pageable pageable) {
        log.debug("Fetching notifications by type evenement: {}", typeEvenement);
        Page<Notification> page = notificationRepository.findByTypeEvenement(typeEvenement, pageable);
        return buildPageResponse(page);
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationStatsDTO getStatistics() {
        log.debug("Calculating notification statistics");
        
        long total = notificationRepository.count();
        long envoyees = notificationRepository.countByStatut(StatutNotification.ENVOYEE);
        long enAttente = notificationRepository.countByStatut(StatutNotification.EN_ATTENTE);
        long echouees = notificationRepository.countByStatut(StatutNotification.ECHOUEE);
        
        Map<StatutNotification, Long> countByStatut = Arrays.stream(StatutNotification.values())
                .collect(Collectors.toMap(
                        statut -> statut,
                        statut -> notificationRepository.countByStatut(statut)
                ));
        
        double tauxReussite = total > 0 ? (double) envoyees / total * 100 : 0.0;
        
        return new NotificationStatsDTO(
                total,
                envoyees,
                enAttente,
                echouees,
                countByStatut,
                tauxReussite
        );
    }
    
    @Override
    public void retryFailedNotification(Long id) {
        log.info("Retrying failed notification with id: {}", id);
        
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        
        if (!notification.peutEtreRetentee(maxRetries)) {
            log.warn("Notification {} cannot be retried. Status: {}, Tentatives: {}", 
                    id, notification.getStatut(), notification.getTentatives());
            throw new RuntimeException("Notification cannot be retried");
        }
        
        notification.incrementerTentatives();
        notificationRepository.save(notification);
        
        emailService.sendEmail(notification);
        
        log.info("Notification {} retried successfully", id);
    }
    
    @Override
    public void deleteOldNotifications(int days) {
        log.info("Deleting notifications older than {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        var oldNotifications = notificationRepository.findPendingNotificationsOlderThan(cutoffDate);
        
        if (!oldNotifications.isEmpty()) {
            notificationRepository.deleteAll(oldNotifications);
            log.info("Deleted {} old notifications", oldNotifications.size());
        } else {
            log.debug("No old notifications to delete");
        }
    }
    
    private PageResponse<NotificationDTO> buildPageResponse(Page<Notification> page) {
        return new PageResponse<>(
                notificationMapper.toDTOList(page.getContent()),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
