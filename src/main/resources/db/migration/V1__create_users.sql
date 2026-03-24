-- V1__create_users.sql
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(200) NOT NULL,
    role VARCHAR(25) NOT NULL DEFAULT 'ROLE_CLIENTE',
    data_criacao DATETIME(6),
    data_modificacao DATETIME(6),
    criado_por VARCHAR(100),
    modificado_por VARCHAR(100),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Usuário admin inicial (senha: 123456 — BCrypt)
INSERT INTO usuarios (username, password, role, data_criacao, criado_por)
VALUES (
    'admin@pilotbroker.com',
    '$2a$12$KFVGqjBFGBqBh.nBFj9RzeQR9H8GqZtV5Bz4FQnf6DFTF8p4Hf6K',
    'ROLE_ADMIN',
    NOW(),
    'SYSTEM'
);
