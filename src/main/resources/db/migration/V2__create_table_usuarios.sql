-- =============================================================
-- V2 — Tabela de Usuários
-- =============================================================

CREATE TABLE usuarios (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    username         VARCHAR(100) NOT NULL,
    password         VARCHAR(255) NOT NULL,
    role             VARCHAR(25)  NOT NULL,
    data_criacao     DATETIME     NOT NULL,
    data_modificacao DATETIME,
    criado_por       VARCHAR(100) NOT NULL,
    modificado_por   VARCHAR(100),

    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
