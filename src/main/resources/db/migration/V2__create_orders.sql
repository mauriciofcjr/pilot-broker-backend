CREATE TABLE orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   BIGINT         NOT NULL,
    symbol       VARCHAR(10)    NOT NULL,
    tipo         VARCHAR(10)    NOT NULL,
    quantidade   INT            NOT NULL,
    preco        DECIMAL(15,2)  NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    data_criacao DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_orders_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
