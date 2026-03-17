package com.aliyara.notificationsservice;

import com.aliyara.notificationsservice.domain.entities.Notification;
import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.aliyara.notificationsservice.domain.enums.TypeEvenement;
import com.aliyara.notificationsservice.infrastructure.email.EmailService;
import com.aliyara.notificationsservice.infrastructure.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class NotificationsServiceIntegrationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Test
    void contextLoads() {
        assertThat(notificationRepository).isNotNull();
        assertThat(emailService).isNotNull();
    }

    @Test
    void testNotificationCreationAndPersistence() {
        Notification notification = new Notification();
        notification.setDestinataire("test@example.com");
        notification.setSujet("Test Notification");
        notification.setContenu("This is a test notification");
        notification.setTypeEvenement(TypeEvenement.KEYNOTE_CREATED);
        notification.setReferenceId(1L);
        notification.setStatut(StatutNotification.EN_ATTENTE);

        Notification saved = notificationRepository.save(notification);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDestinataire()).isEqualTo("test@example.com");
        assertThat(saved.getStatut()).isEqualTo(StatutNotification.EN_ATTENTE);
    }

    @Test
    void testEmailServiceMode() {
        String mode = emailService.getMode();
        assertThat(mode).isNotNull();
    }
}
