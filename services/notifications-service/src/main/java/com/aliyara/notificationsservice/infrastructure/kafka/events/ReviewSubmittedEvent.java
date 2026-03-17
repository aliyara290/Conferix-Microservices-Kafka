package com.aliyara.notificationsservice.infrastructure.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReviewSubmittedEvent extends BaseEvent {
    
    private Long reviewId;
    private Long conferenceId;
    private String titreConference;
    private String revieweur;
    private String email;
    private Integer note;
    private String commentaire;
}
