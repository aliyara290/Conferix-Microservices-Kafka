package com.aliyara.notificationsservice.application.dto;

import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    String destinataire,
    String sujet,
    StatutNotification statut,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dateEnvoi
) {}
