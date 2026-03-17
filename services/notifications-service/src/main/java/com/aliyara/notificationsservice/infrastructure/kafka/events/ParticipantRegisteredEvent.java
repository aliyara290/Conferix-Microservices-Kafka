package com.aliyara.notificationsservice.infrastructure.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ParticipantRegisteredEvent extends BaseEvent {
    
    private Long inscriptionId;
    private Long conferenceId;
    private String titreConference;
    private String participantNom;
    private String participantPrenom;
    private String email;
}
