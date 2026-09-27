CREATE TABLE log_importacao (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome_evento         VARCHAR(200) NOT NULL,
    url_evento          VARCHAR(500),
    quantidade_alunos   INTEGER      NOT NULL,
    quantidade_cursos   INTEGER      NOT NULL,
    data_importacao     TIMESTAMP    NOT NULL,
    admin_id            UUID         NOT NULL REFERENCES usuario (id),
    CONSTRAINT ck_log_importacao_alunos CHECK (quantidade_alunos >= 0),
    CONSTRAINT ck_log_importacao_cursos CHECK (quantidade_cursos >= 0)
);
