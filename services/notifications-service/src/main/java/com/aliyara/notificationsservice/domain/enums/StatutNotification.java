package com.aliyara.notificationsservice.domain.enums;

public enum StatutNotification {
  
    EN_ATTENTE("En attente d'envoi"),
    ENVOYEE("Envoyée avec succès"),
    ECHOUEE("Échec d'envoi");
    
    private final String description;
    
    StatutNotification(String description) {
        this.description = description;
    }
  
    public String getDescription() {
        return description;
    }
    public boolean isRetryable() {
        return this == ECHOUEE || this == EN_ATTENTE;
    }
}
