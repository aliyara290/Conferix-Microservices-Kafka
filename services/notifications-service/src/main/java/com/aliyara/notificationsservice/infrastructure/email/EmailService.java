package com.aliyara.notificationsservice.infrastructure.email;

import com.aliyara.notificationsservice.domain.entities.Notification;

public interface EmailService {
    
    void sendEmail(Notification notification);
    
    boolean isEmailConfigured();
    
    String getMode();
}
