-- Migracion incremental: preserva todas las tablas y filas de V1.
ALTER TABLE usuarios ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuario_token_version CHECK (token_version >= 0);

CREATE TABLE tokens_revocados (
    jti VARCHAR(64) PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    expira_en TIMESTAMPTZ NOT NULL,
    revocado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tokens_revocados_expira ON tokens_revocados(expira_en);
