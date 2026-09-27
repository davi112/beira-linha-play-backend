ALTER TABLE aluno ADD COLUMN log_importacao_id UUID REFERENCES log_importacao (id);

DO $$
BEGIN
    IF to_regclass('public.log_importacao_aluno') IS NOT NULL THEN
        UPDATE aluno
        SET log_importacao_id = rel.log_id
        FROM log_importacao_aluno rel
        WHERE rel.usuario_id = aluno.id;

        DROP TABLE log_importacao_aluno;
    END IF;
END $$;
