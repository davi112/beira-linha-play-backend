ALTER TABLE usuario DROP CONSTRAINT uq_usuario_email;

CREATE UNIQUE INDEX uq_usuario_email_nao_aluno
    ON usuario (email)
    WHERE tipo <> 'ALUNO';
