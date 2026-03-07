package com.aliyara.notificationsservice.domain.entities;

import com.aliyara.notificationsservice.domain.enums.StatutNotification;
import com.aliyara.notificationsservice.domain.enums.TypeEvenement;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_destinataire", columnList = "destinataire"),
    @Index(name = "idx_type_evenement", columnList = "type_evenement"),
    @Index(name = "idx_statut", columnList = "statut"),
    @Index(name = "idx_reference_id", columnList = "reference_id"),
    @Index(name = "idx_date_envoi", columnList = "date_envoi"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_statut_tentatives", columnList = "statut, tentatives"),
    @Index(name = "idx_type_reference", columnList = "type_evenement, reference_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "L'adresse email du destinataire doit être valide")
    @NotBlank(message = "Le destinataire est obligatoire")
    @Column(nullable = false, length = 255)
    private String destinataire;
    
    @NotBlank(message = "Le sujet est obligatoire")
    @Column(nullable = false, length = 500)
    private String sujet;
    
    @NotBlank(message = "Le contenu est obligatoire")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    @NotNull(message = "Le type d'événement est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_evenement", nullable = false, length = 50)
    private TypeEvenement typeEvenement;

    @Column(name = "reference_id")
    private Long referenceId;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutNotification statut = StatutNotification.EN_ATTENTE;

    @Column(name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @PositiveOrZero(message = "Le nombre de tentatives ne peut pas être négatif")
    @Column(nullable = false)
    @Builder.Default
    private Integer tentatives = 0;
  
    @Column(name = "erreur_message", columnDefinition = "TEXT")
    private String erreurMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public void marquerEnvoyee() {
        this.statut = StatutNotification.ENVOYEE;
        this.dateEnvoi = LocalDateTime.now();
        this.tentatives++;
    }
    
    public void marquerEchouee(String erreurMessage) {
        this.statut = StatutNotification.ECHOUEE;
        this.erreurMessage = erreurMessage;
        this.tentatives++;
    }
    
    public boolean peutEtreRetentee(int maxTentatives) {
        return this.statut.isRetryable() && this.tentatives < maxTentatives;
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.tentatives == null) {
            this.tentatives = 0;
        }
        if (this.statut == null) {
            this.statut = StatutNotification.EN_ATTENTE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
