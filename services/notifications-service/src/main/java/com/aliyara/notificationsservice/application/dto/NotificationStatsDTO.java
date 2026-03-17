package com.aliyara.notificationsservice.application.dto;

import com.aliyara.notificationsservice.domain.enums.StatutNotification;

import java.util.Map;

public record NotificationStatsDTO(
    long totalNotifications,
    long notificationsEnvoyees,
    long notificationsEnAttente,
    long notificationsEchouees,
    Map<StatutNotification, Long> countByStatut,
    double tauxReussite
) {}
