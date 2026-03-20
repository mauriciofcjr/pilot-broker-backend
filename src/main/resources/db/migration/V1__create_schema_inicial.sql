-- =============================================================
-- V1 — Schema Inicial
-- Configuração de charset e collation padrão do banco pilot_broker
-- Tabelas de negócio serão criadas nas migrations seguintes
-- =============================================================

ALTER DATABASE pilot_broker
    CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_unicode_ci;
