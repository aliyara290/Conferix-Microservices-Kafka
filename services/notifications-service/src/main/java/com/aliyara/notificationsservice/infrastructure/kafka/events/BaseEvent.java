package com.aliyara.notificationsservice.infrastructure.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    
    protected String eventId;
    protected LocalDateTime timestamp;
    protected String eventType;
}
