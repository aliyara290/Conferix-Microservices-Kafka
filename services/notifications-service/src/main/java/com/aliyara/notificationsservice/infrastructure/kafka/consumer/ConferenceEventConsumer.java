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
public class ConferenceEventConsumer {
    
    private final NotificationRepository notificationRepository;
    
    @KafkaListener(
        topics = "${app.kafka.topics.conference-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeConferenceEvent(
            @Payload JsonNode payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Conference event reçu - Key: {}, Partition: {}, Offset: {}", key, partition, offset);
            log.debug("Payload: {}", payload);
            
            String eventType = payload.has("eventType") ? payload.get("eventType").asText() : "CONFERENCE_CREATED";
            Long conferenceId = payload.has("conferenceId") ? payload.get("conferenceId").asLong() : null;
            String email = payload.has("email") ? payload.get("email").asText() : null;
            String titre = payload.has("titre") ? payload.get("titre").asText() : "Nouvelle Conférence";
            
            if (email == null || conferenceId == null) {
                log.warn("Email ou conferenceId manquant dans l'événement conférence");
                acknowledgment.acknowledge();
                return;
            }
            
            Notification notification = new Notification();
            notification.setDestinataire(email);
            notification.setReferenceId(conferenceId);
            notification.setStatut(StatutNotification.EN_ATTENTE);
            
            if ("CONFERENCE_STATUS_CHANGED".equalsIgnoreCase(eventType)) {
                String nouveauStatut = payload.has("nouveauStatut") ? payload.get("nouveauStatut").asText() : "N/A";
                String ancienStatut = payload.has("ancienStatut") ? payload.get("ancienStatut").asText() : "N/A";
                
                notification.setTypeEvenement(TypeEvenement.CONFERENCE_STATUS_CHANGED);
                notification.setSujet("Changement de statut: " + titre);
                notification.setContenu(String.format(
                    "Le statut de la conférence a changé !\n\n" +
                    "Conférence: %s\n" +
                    "Ancien statut: %s\n" +
                    "Nouveau statut: %s\n\n" +
                    "Consultez les détails dans votre espace.",
                    titre, ancienStatut, nouveauStatut
                ));
                
                if (notificationRepository.existsByTypeEvenementAndReferenceId(
                        TypeEvenement.CONFERENCE_STATUS_CHANGED, conferenceId)) {
                    log.info("Notification de changement déjà existante pour conférence {}", conferenceId);
                    acknowledgment.acknowledge();
                    return;
                }
                
            } else {
                String organisateur = payload.has("organisateur") ? payload.get("organisateur").asText() : "N/A";
                
                notification.setTypeEvenement(TypeEvenement.CONFERENCE_CREATED);
                notification.setSujet("Nouvelle Conférence: " + titre);
                notification.setContenu(String.format(
                    "Une nouvelle conférence a été créée !\n\n" +
                    "Titre: %s\n" +
                    "Organisateur: %s\n\n" +
                    "Inscrivez-vous dès maintenant !",
                    titre, organisateur
                ));
                
                if (notificationRepository.existsByTypeEvenementAndReferenceId(
                        TypeEvenement.CONFERENCE_CREATED, conferenceId)) {
                    log.info("Notification déjà existante pour conférence {}", conferenceId);
                    acknowledgment.acknowledge();
                    return;
                }
            }
            
            notificationRepository.save(notification);
            log.info("Notification créée pour conférence {} - Type: {} - Destinataire: {}", 
                    conferenceId, notification.getTypeEvenement(), email);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement conférence: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement événement conférence", e);
        }
    }
}
