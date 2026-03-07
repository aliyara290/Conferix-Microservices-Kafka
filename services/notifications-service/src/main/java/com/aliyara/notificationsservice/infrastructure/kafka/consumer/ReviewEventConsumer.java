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
public class ReviewEventConsumer {
    
    private final NotificationRepository notificationRepository;
    
    @KafkaListener(
        topics = "${app.kafka.topics.review-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeReviewEvent(
            @Payload JsonNode payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Review event reçu - Key: {}, Partition: {}, Offset: {}", key, partition, offset);
            log.debug("Payload: {}", payload);
            
            Long reviewId = payload.has("reviewId") ? payload.get("reviewId").asLong() : null;
            Long conferenceId = payload.has("conferenceId") ? payload.get("conferenceId").asLong() : null;
            String email = payload.has("email") ? payload.get("email").asText() : null;
            String titreConference = payload.has("titreConference") ? payload.get("titreConference").asText() : "Conférence";
            String revieweur = payload.has("revieweur") ? payload.get("revieweur").asText() : "N/A";
            Integer note = payload.has("note") ? payload.get("note").asInt() : null;
            
            if (email == null || reviewId == null) {
                log.warn("Email ou reviewId manquant dans l'événement review");
                acknowledgment.acknowledge();
                return;
            }
            
            if (notificationRepository.existsByTypeEvenementAndReferenceId(TypeEvenement.REVIEW_SUBMITTED, reviewId)) {
                log.info("Notification déjà existante pour review {}", reviewId);
                acknowledgment.acknowledge();
                return;
            }
            
            Notification notification = new Notification();
            notification.setDestinataire(email);
            notification.setSujet("Nouvelle Review reçue: " + titreConference);
            notification.setContenu(String.format(
                "Une nouvelle review a été soumise !\n\n" +
                "Conférence: %s\n" +
                "Revieweur: %s\n" +
                "%s" +
                "\nConsultez les détails dans votre espace organisateur.",
                titreConference, 
                revieweur,
                note != null ? "Note: " + note + "/5\n" : ""
            ));
            notification.setTypeEvenement(TypeEvenement.REVIEW_SUBMITTED);
            notification.setReferenceId(reviewId);
            notification.setStatut(StatutNotification.EN_ATTENTE);
            
            notificationRepository.save(notification);
            
            log.info("Notification créée pour review {} - Conférence: {} - Destinataire: {}", 
                    reviewId, conferenceId, email);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement review: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement événement review", e);
        }
    }
}
