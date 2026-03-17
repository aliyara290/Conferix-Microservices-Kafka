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
public class InscriptionEventConsumer {
    
    private final NotificationRepository notificationRepository;
    
    @KafkaListener(
        topics = "${app.kafka.topics.inscription-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeInscriptionEvent(
            @Payload JsonNode payload,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Inscription event reçu - Key: {}, Partition: {}, Offset: {}", key, partition, offset);
            log.debug("Payload: {}", payload);
            
            Long inscriptionId = payload.has("inscriptionId") ? payload.get("inscriptionId").asLong() : null;
            Long conferenceId = payload.has("conferenceId") ? payload.get("conferenceId").asLong() : null;
            String email = payload.has("email") ? payload.get("email").asText() : null;
            String titreConference = payload.has("titreConference") ? payload.get("titreConference").asText() : "Conférence";
            String participantNom = payload.has("participantNom") ? payload.get("participantNom").asText() : "";
            String participantPrenom = payload.has("participantPrenom") ? payload.get("participantPrenom").asText() : "";
            
            if (email == null || inscriptionId == null) {
                log.warn("Email ou inscriptionId manquant dans l'événement inscription");
                acknowledgment.acknowledge();
                return;
            }
            
            if (notificationRepository.existsByTypeEvenementAndReferenceId(
                    TypeEvenement.PARTICIPANT_REGISTERED, inscriptionId)) {
                log.info("Notification déjà existante pour inscription {}", inscriptionId);
                acknowledgment.acknowledge();
                return;
            }
            
            String nomComplet = (participantPrenom + " " + participantNom).trim();
            if (nomComplet.isEmpty()) {
                nomComplet = email;
            }
            
            Notification notification = new Notification();
            notification.setDestinataire(email);
            notification.setSujet("Inscription confirmée: " + titreConference);
            notification.setContenu(String.format(
                "Bonjour %s,\n\n" +
                "Votre inscription à la conférence a été confirmée avec succès !\n\n" +
                "Conférence: %s\n\n" +
                "Vous recevrez plus d'informations prochainement.\n" +
                "Merci de votre participation !",
                nomComplet,
                titreConference
            ));
            notification.setTypeEvenement(TypeEvenement.PARTICIPANT_REGISTERED);
            notification.setReferenceId(inscriptionId);
            notification.setStatut(StatutNotification.EN_ATTENTE);
            
            notificationRepository.save(notification);
            
            log.info("Notification créée pour inscription {} - Conférence: {} - Destinataire: {}", 
                    inscriptionId, conferenceId, email);
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Erreur lors du traitement de l'événement inscription: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur traitement événement inscription", e);
        }
    }
}
