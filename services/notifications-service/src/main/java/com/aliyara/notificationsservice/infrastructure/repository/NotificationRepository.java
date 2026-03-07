package com.aliyara.notificationsservice.infrastructure.repository;

import com.aliyara.notificationsservice.domain.entities.Notification;
import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.aliyara.notificationsservice.domain.enums.TypeEvenement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByDestinataire(String destinataire, Pageable pageable);
    Page<Notification> findByTypeEvenement(TypeEvenement typeEvenement, Pageable pageable);
    Page<Notification> findByStatut(StatutNotification statut, Pageable pageable);
    Page<Notification> findByReferenceId(Long referenceId, Pageable pageable);
    Page<Notification> findByTypeEvenementAndReferenceId(
        TypeEvenement typeEvenement, 
        Long referenceId, 
        Pageable pageable
    );

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.statut = :statut")
    long countByStatut(@Param("statut") StatutNotification statut);
    
    
    @Query("SELECT n FROM Notification n WHERE n.statut = 'ECHOUEE' AND n.tentatives < :maxTentatives ORDER BY n.createdAt ASC")
    List<Notification> findRetryableNotifications(@Param("maxTentatives") int maxTentatives);
    
    @Query("SELECT n FROM Notification n WHERE n.statut = 'EN_ATTENTE' AND n.createdAt < :dateLimit ORDER BY n.createdAt ASC")
    List<Notification> findPendingNotificationsOlderThan(@Param("dateLimit") LocalDateTime dateLimit);
    
    List<Notification> findTop10ByDestinataireOrderByCreatedAtDesc(String destinataire);
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.typeEvenement = :type AND n.statut = 'ENVOYEE'")
    long countEnvoyeesByType(@Param("type") TypeEvenement typeEvenement);
    
    Page<Notification> findByDestinataireAndStatut(
        String destinataire, 
        StatutNotification statut, 
        Pageable pageable
    );
    
    @Query("DELETE FROM Notification n WHERE n.createdAt < :dateLimit AND n.statut = 'ENVOYEE'")
    int deleteOldNotifications(@Param("dateLimit") LocalDateTime dateLimit);

    boolean existsByTypeEvenementAndReferenceId(TypeEvenement typeEvenement, Long referenceId);
}
