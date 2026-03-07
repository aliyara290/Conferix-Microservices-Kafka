package com.aliyara.notificationsservice.domain.enums;


public enum TypeEvenement {
    

    KEYNOTE_CREATED("Création d'un Keynote", "keynote-events"),
    CONFERENCE_CREATED("Création d'une conférence", "conference-events"),
    CONFERENCE_STATUS_CHANGED("Changement de statut de conférence", "conference-events"),
    REVIEW_SUBMITTED("Soumission d'une review", "review-events"),
    PARTICIPANT_REGISTERED("Inscription d'un participant", "inscription-events");
    
    private final String description;
    private final String topic;
    
    TypeEvenement(String description, String topic) {
        this.description = description;
        this.topic = topic;
    }
    

    public String getDescription() {
        return description;
    }

    public String getTopic() {
        return topic;
    }
}
