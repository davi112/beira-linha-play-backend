ALTER TABLE usuario ADD COLUMN acesso_expira_em DATE;

CREATE TABLE log_importacao_aluno (
    log_id      UUID NOT NULL REFERENCES log_importacao (id) ON DELETE CASCADE,
    usuario_id  UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    PRIMARY KEY (log_id, usuario_id)
);
