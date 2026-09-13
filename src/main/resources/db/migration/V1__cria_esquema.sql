CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE usuario (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(180),
    senha       VARCHAR(100) NOT NULL,
    tipo        VARCHAR(20)  NOT NULL,
    CONSTRAINT ck_usuario_tipo CHECK (tipo IN ('ALUNO', 'MONITOR', 'ADMIN')),
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

CREATE TABLE aluno (
    id             UUID PRIMARY KEY REFERENCES usuario (id) ON DELETE CASCADE,
    apelido        VARCHAR(40) NOT NULL,
    pontos         INTEGER     NOT NULL DEFAULT 0,
    imagem_perfil  TEXT,
    CONSTRAINT uq_aluno_apelido UNIQUE (apelido),
    CONSTRAINT ck_aluno_pontos CHECK (pontos >= 0)
);

CREATE TABLE monitor (
    id            UUID PRIMARY KEY REFERENCES usuario (id) ON DELETE CASCADE,
    curso_origem  VARCHAR(120)
);

CREATE TABLE admin (
    id UUID PRIMARY KEY REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE curso (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome           VARCHAR(120) NOT NULL,
    codigo_acesso  VARCHAR(40)  NOT NULL,
    CONSTRAINT uq_curso_codigo_acesso UNIQUE (codigo_acesso)
);

CREATE TABLE inscricao_curso (
    aluno_id  UUID NOT NULL REFERENCES aluno (id) ON DELETE CASCADE,
    curso_id  UUID NOT NULL REFERENCES curso (id) ON DELETE CASCADE,
    PRIMARY KEY (aluno_id, curso_id)
);

CREATE TABLE monitoria_curso (
    monitor_id  UUID NOT NULL REFERENCES monitor (id) ON DELETE CASCADE,
    curso_id    UUID NOT NULL REFERENCES curso (id) ON DELETE CASCADE,
    PRIMARY KEY (monitor_id, curso_id)
);

CREATE TABLE modulo (
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome      VARCHAR(120) NOT NULL,
    curso_id  UUID         NOT NULL REFERENCES curso (id) ON DELETE CASCADE
);

CREATE TABLE atividade (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    titulo         VARCHAR(180) NOT NULL,
    quant_questoes INTEGER      NOT NULL DEFAULT 0,
    modulo_id      UUID         NOT NULL REFERENCES modulo (id) ON DELETE CASCADE,
    CONSTRAINT ck_atividade_quant_questoes CHECK (quant_questoes >= 0)
);

CREATE TABLE questao (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    enunciado    TEXT    NOT NULL,
    valor        INTEGER NOT NULL,
    atividade_id UUID    NOT NULL REFERENCES atividade (id) ON DELETE CASCADE,
    CONSTRAINT ck_questao_valor CHECK (valor > 0)
);

CREATE TABLE alternativa (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    descricao   TEXT    NOT NULL,
    correta     BOOLEAN NOT NULL DEFAULT FALSE,
    questao_id  UUID    NOT NULL REFERENCES questao (id) ON DELETE CASCADE
);

CREATE TABLE tentativa (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    data_envio        TIMESTAMPTZ NOT NULL,
    pontuacao_obtida  INTEGER     NOT NULL,
    aluno_id          UUID        NOT NULL REFERENCES aluno (id) ON DELETE CASCADE,
    atividade_id      UUID        NOT NULL REFERENCES atividade (id) ON DELETE RESTRICT,
    CONSTRAINT ck_tentativa_pontuacao CHECK (pontuacao_obtida >= 0)
);

CREATE UNIQUE INDEX uq_tentativa_aluno_atividade_envio
    ON tentativa (aluno_id, atividade_id, data_envio);

CREATE TABLE resposta (
    id             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    correta        BOOLEAN NOT NULL,
    tentativa_id   UUID    NOT NULL REFERENCES tentativa (id) ON DELETE CASCADE,
    questao_id     UUID    NOT NULL REFERENCES questao (id) ON DELETE RESTRICT,
    alternativa_id UUID    NOT NULL REFERENCES alternativa (id) ON DELETE RESTRICT
);

CREATE TABLE medalha (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome        VARCHAR(80) NOT NULL,
    imagem_url  TEXT        NOT NULL,
    pontos_min  INTEGER     NOT NULL,
    CONSTRAINT ck_medalha_pontos_min CHECK (pontos_min >= 0)
);

CREATE TABLE token_atualizacao (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id  UUID         NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64)     NOT NULL,
    expira_em   TIMESTAMPTZ  NOT NULL,
    revogado    BOOLEAN      NOT NULL DEFAULT FALSE,
    criado_em   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_token_atualizacao_hash UNIQUE (token_hash)
);

CREATE INDEX idx_token_atualizacao_usuario ON token_atualizacao (usuario_id);
CREATE INDEX idx_tentativa_aluno_atividade ON tentativa (aluno_id, atividade_id);
CREATE INDEX idx_modulo_curso ON modulo (curso_id);
CREATE INDEX idx_atividade_modulo ON atividade (modulo_id);
CREATE INDEX idx_questao_atividade ON questao (atividade_id);
