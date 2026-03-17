package com.aliyara.notificationsservice.infrastructure.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConferenceCreatedEvent extends BaseEvent {
    
    private Long conferenceId;
    private String titre;
    private String organisateur;
    private String email;
    private String description;
}
