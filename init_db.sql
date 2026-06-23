-- ═══════════════════════════════════════════════════════════════
--  BiblioManager — Script d'initialisation de la base de données
--  SGBD : MySQL 8+
-- ═══════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS bibliotheque_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bibliotheque_db;

-- ─── TABLE livres ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS livres (
                                      id                  INT          AUTO_INCREMENT PRIMARY KEY,
                                      titre               VARCHAR(255) NOT NULL,
    auteur              VARCHAR(150) NOT NULL,
    isbn                VARCHAR(20)  DEFAULT NULL,
    categorie           VARCHAR(80)  DEFAULT 'Autre',
    description         TEXT         DEFAULT NULL,
    quantite            INT          NOT NULL DEFAULT 1,
    prix                DECIMAL(8,2) DEFAULT 0.00,
    date_ajout          DATE         NOT NULL,
    disponible          BOOLEAN      NOT NULL DEFAULT TRUE,
    couleur_etiquette   VARCHAR(50)  DEFAULT '#4A90D9',
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

-- ─── TABLE emprunts ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS emprunts (
                                        id                      INT          AUTO_INCREMENT PRIMARY KEY,
                                        livre_id                INT          NOT NULL,
                                        emprunteur              VARCHAR(150) NOT NULL,
    email                   VARCHAR(150) DEFAULT NULL,
    date_emprunt            DATE         NOT NULL,
    date_retour_prevue      DATE         NOT NULL,
    date_retour_effective   DATE         DEFAULT NULL,
    statut                  ENUM('EN_COURS','RENDU','EN_RETARD') NOT NULL DEFAULT 'EN_COURS',
    remarques               TEXT         DEFAULT NULL,
    penalite_appliquee      BOOLEAN      NOT NULL DEFAULT FALSE,
    nombre_renouvellements  INT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_emprunt_livre
    FOREIGN KEY (livre_id) REFERENCES livres(id)
                                                                   ON UPDATE CASCADE
                                                                   ON DELETE RESTRICT
    ) ENGINE=InnoDB;

-- ─── DONNÉES DE TEST ──────────────────────────────────────────
INSERT INTO livres (titre, auteur, isbn, categorie, description, quantite, prix, date_ajout, disponible, couleur_etiquette) VALUES
                                                                                                                                ('Le Petit Prince',        'Antoine de Saint-Exupéry', '978-2-07-040850-4', 'Roman',       'Conte philosophique et poétique.',     3,  49.00, '2024-01-10', TRUE,  '#F6AD55'),
                                                                                                                                ('Clean Code',             'Robert C. Martin',         '978-0-13-235088-4', 'Informatique','Écrire un code propre et maintenable.', 2,  189.00,'2024-01-15', TRUE,  '#68D391'),
                                                                                                                                ('Sapiens',                'Yuval Noah Harari',        '978-2-07-273678-6', 'Histoire',    'Brève histoire de l\'humanité.',        4,  99.00, '2024-02-01', TRUE,  '#4A90D9'),
('L\'Art de la Guerre',    'Sun Tzu',                  '978-2-07-036976-2', 'Philosophie', 'Traité militaire et stratégique.',     2,  45.00, '2024-02-10', FALSE, '#FC8181'),
                                                                                                                                ('Harry Potter T1',        'J.K. Rowling',             '978-2-07-054090-1', 'Roman',       'Le sorcier à l\'école de Poudlard.',   5,  79.00, '2024-03-05', TRUE,  '#B794F4');

INSERT INTO emprunts (livre_id, emprunteur, email, date_emprunt, date_retour_prevue, statut, remarques) VALUES
(1, 'Youssef Alami',   'youssef@email.ma', '2025-06-01', '2025-06-15', 'EN_COURS',  'Première lecture'),
(2, 'Fatima Benali',   'fatima@email.ma',  '2025-05-20', '2025-06-03', 'EN_RETARD', 'Relancer par email'),
(3, 'Ahmed El Fassi',  'ahmed@email.ma',   '2025-05-10', '2025-05-24', 'RENDU',     NULL),
(5, 'Sara Oukili',     'sara@email.ma',    '2025-06-10', '2025-06-24', 'EN_COURS',  NULL);