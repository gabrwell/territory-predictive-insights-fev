-- Script de criacao do banco (H2). Executado automaticamente na inicializacao.
CREATE TABLE IF NOT EXISTS medicao (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id   VARCHAR(20)  NOT NULL,
    tipo        VARCHAR(30)  NOT NULL,
    valor       DOUBLE       NOT NULL,
    data_hora   TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_medicao_sensor ON medicao (sensor_id, data_hora);

CREATE TABLE IF NOT EXISTS alerta (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sensor_id       VARCHAR(20)   NOT NULL,
    tipo            VARCHAR(30)   NOT NULL,
    nivel           VARCHAR(20)   NOT NULL,
    valor_previsto  DOUBLE        NOT NULL,
    limite          DOUBLE        NOT NULL,
    estrategia      VARCHAR(60)   NOT NULL,
    motivo          VARCHAR(1000) NOT NULL,
    data_hora       TIMESTAMP     NOT NULL
);
