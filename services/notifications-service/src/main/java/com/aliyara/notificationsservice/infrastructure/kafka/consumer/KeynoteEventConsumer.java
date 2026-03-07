package com.aliyara.notificationsservice.infrastructure.kafka.consumer;

import com.aliyara.notificationsservice.domain.entities.Notification;
import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.aliyara.notificationsservice.domain.enums.TypeEvenement;
import com.aliyara.notificationsservice.infrastructure.repository.NotificationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeynoteEventConsumer {
    
    private final NotificationRepository notificationRepository;
    
    @KafkaListener(
        topics = "${app.kafka.topics.keynote-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeKeynoteEvent(
            @Payload JsonNode payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Keynote event reçu - Key: {}, Partition: {}, Offset: {}", key, partition, offset);
            log.debug("Payload: {}", payload);
            
            Long keynoteId = payload.has("keynoteId") ? payload.get("keynoteId").asLong() : null;
            String email = payload.has("email") ? payload.get("email").asText() : null;
            String titre = payload.has("titre") ? payload.get("titre").asText() : "Nouvelle Keynote";
            String orateur = payload.has("orateur") ? payload.get("orateur").asText() : "N/A";
            
            if (email == null || keynoteId == null) {
                log.warn("Email ou keynoteId manquant dans l'événement keynote");
                acknowledgment.acknowledge();
                return;
            }
            
            if (notificationRepository.existsByTypeEvenementAndReferenceId(TypeEvenement.KEYNOTE_CREATED, keynoteId)) {
                log.info("Notification déjà existante pour keynote {}", keynoteId);
                acknowledgment.acknowledge();
                return;
            }
            
            Notification notification = new Notification();
            notification.setDestinataire(email);
            notification.setSujet("Nouvelle Keynote: " + titre);
            notification.setContenu(String.format(
                "Une nouvelle keynote a été créée !\n\n" +
                "Titre: %s\n" +
                "Orateur: %s\n\n" +
                "Restez informé des dernières mises à jour.",
                titre, orateur
            ));
            notification.setTypeEvenement(TypeEvenement.KEYNOTE_CREATED);
            notification.setReferenceId(keynoteId);
            notification.setStatut(StatutNotification.EN_ATTENTE);
            
            notificationRepository.save(notification);
            
            log.info("Notification créée pour keynote {} - Destinataire: {}", keynoteId, email);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement keynote: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement événement keynote", e);
        }
    }
}
