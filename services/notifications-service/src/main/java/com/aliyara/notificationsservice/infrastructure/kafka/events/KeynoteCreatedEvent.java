package com.aliyara.notificationsservice.infrastructure.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KeynoteCreatedEvent extends BaseEvent {
    
    private Long keynoteId;
    private String titre;
    private String orateur;
    private String email;
    private String description;
}
