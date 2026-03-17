package com.aliyara.notificationsservice.application.dto;

import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.aliyara.notificationsservice.domain.enums.TypeEvenement;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record NotificationDTO(
    Long id,
    String destinataire,
    String sujet,
    String contenu,
    TypeEvenement typeEvenement,
    Long referenceId,
    StatutNotification statut,
    Integer tentatives,
    String erreurMessage,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dateEnvoi,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt
) {}
