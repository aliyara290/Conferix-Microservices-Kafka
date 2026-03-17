package com.aliyara.notificationsservice.infrastructure.email;

import com.aliyara.notificationsservice.domain.entities.Notification;
import com.aliyara.notificationsservice.infrastructure.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;
    
    @Value("${app.email.mode:mock}")
    private String emailMode;
    
    @Value("${app.email.from:noreply@conferix.com}")
    private String fromEmail;
    
    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;
    
    @Async
    @Transactional
    @Override
    public void sendEmail(Notification notification) {
        if (!emailEnabled || "mock".equalsIgnoreCase(emailMode)) {
            sendMockEmail(notification);
            return;
        }
        
        try {
            sendSmtpEmail(notification);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email pour notification {}: {}", 
                    notification.getId(), e.getMessage(), e);
            notification.marquerEchouee(e.getMessage());
            notificationRepository.save(notification);
        }
    }
    
    private void sendMockEmail(Notification notification) {
        log.info("MODE MOCK - Email simulé envoyé");
        log.info("Destinataire: {}", notification.getDestinataire());
        log.info("Sujet: {}", notification.getSujet());
        log.info("Contenu: {}", notification.getContenu());
        
        notification.marquerEnvoyee();
        notificationRepository.save(notification);
    }
    
    private void sendSmtpEmail(Notification notification) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(notification.getDestinataire());
        helper.setSubject(notification.getSujet());
        helper.setText(notification.getContenu(), false);
        
        mailSender.send(message);
        
        log.info("Email SMTP envoyé avec succès à {}", notification.getDestinataire());
        
        notification.marquerEnvoyee();
        notificationRepository.save(notification);
    }
    
    @Override
    public boolean isEmailConfigured() {
        return emailEnabled && !"mock".equalsIgnoreCase(emailMode);
    }
    
    @Override
    public String getMode() {
        return emailMode;
    }
}
