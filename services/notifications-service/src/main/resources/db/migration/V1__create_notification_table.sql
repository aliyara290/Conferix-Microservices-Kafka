-- Flyway Migration V1: Create notification table
-- Author: ConferenceHub Team
-- Date: 2026-03-07
-- Description: Create notifications table with indexes for notification-service

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Recipient information
    destinataire VARCHAR(255) NOT NULL COMMENT 'Email du destinataire',
    
    -- Email content
    sujet VARCHAR(500) NOT NULL COMMENT 'Sujet de l''email',
    contenu TEXT NOT NULL COMMENT 'Corps du message',
    
    -- Event metadata
    type_evenement VARCHAR(50) NOT NULL COMMENT 'Type d''événement Kafka (KEYNOTE_CREATED, CONFERENCE_CREATED, etc.)',
    reference_id BIGINT COMMENT 'ID de l''entité source (conferenceId, keynoteId, etc.)',
    
    -- Notification status
    statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE' COMMENT 'Statut: EN_ATTENTE, ENVOYEE, ECHOUEE',
    date_envoi DATETIME COMMENT 'Date et heure d''envoi effectif',
    tentatives INT NOT NULL DEFAULT 0 COMMENT 'Nombre de tentatives d''envoi',
    erreur_message TEXT COMMENT 'Message d''erreur en cas d''échec',
    
    -- Audit fields
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Date de création',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Date de dernière modification',
    
    -- Constraints
    CONSTRAINT chk_statut CHECK (statut IN ('EN_ATTENTE', 'ENVOYEE', 'ECHOUEE')),
    CONSTRAINT chk_type_evenement CHECK (type_evenement IN (
        'KEYNOTE_CREATED',
        'CONFERENCE_CREATED',
        'CONFERENCE_STATUS_CHANGED',
        'REVIEW_SUBMITTED',
        'PARTICIPANT_REGISTERED'
    )),
    CONSTRAINT chk_tentatives CHECK (tentatives >= 0)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Table des notifications événementielles';

-- Index pour améliorer les performances des requêtes
CREATE INDEX idx_destinataire ON notifications(destinataire) COMMENT 'Index pour recherche par email destinataire';
CREATE INDEX idx_type_evenement ON notifications(type_evenement) COMMENT 'Index pour filtrage par type d''événement';
CREATE INDEX idx_statut ON notifications(statut) COMMENT 'Index pour filtrage par statut';
CREATE INDEX idx_reference_id ON notifications(reference_id) COMMENT 'Index pour recherche par référence source';
CREATE INDEX idx_date_envoi ON notifications(date_envoi) COMMENT 'Index pour tri chronologique';
CREATE INDEX idx_created_at ON notifications(created_at) COMMENT 'Index pour tri par date de création';

-- Index composite pour les requêtes fréquentes
CREATE INDEX idx_statut_tentatives ON notifications(statut, tentatives) COMMENT 'Index composite pour retry logic';
CREATE INDEX idx_type_reference ON notifications(type_evenement, reference_id) COMMENT 'Index composite pour recherche événement+source';
