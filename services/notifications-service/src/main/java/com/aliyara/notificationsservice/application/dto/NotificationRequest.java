package com.aliyara.notificationsservice.application.dto;

import com.aliyara.notificationsservice.domain.enums.TypeEvenement;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
    @NotBlank(message = "Le destinataire est obligatoire")
    @Email(message = "Le destinataire doit être une adresse email valide")
    String destinataire,
    
    @NotBlank(message = "Le sujet est obligatoire")
    String sujet,
    
    @NotBlank(message = "Le contenu est obligatoire")
    String contenu,
    
    @NotNull(message = "Le type d'événement est obligatoire")
    TypeEvenement typeEvenement,
    
    String referenceId
) {}
