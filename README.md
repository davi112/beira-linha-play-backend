# Beira Linha Play — API

Backend REST do [beira-linha-play-frontend](https://github.com/davi112/beira-linha-play-frontend): Spring Boot 3.4, Spring Security, Flyway e PostgreSQL, organizado em **Clean Architecture**, com nomes em **português (pt-BR)**.

Este repositório é **novo**. Nada do frontend nem do `next-gen-force-trial` foi alterado.

## O que já está pronto

- Autenticação JWT em cookies **HttpOnly** + **refresh token** rotacionado (hash SHA-256 no banco)
- Cadastro/login de aluno (apelido) e monitor (e-mail); login de admin (nome)
- Cursos, módulos, atividades, tentativas (máx. 2), ranking, medalhas, monitoramento
- Gabarito **não** é enviado ao aluno no `GET /api/atividades/{id}`
- Docker Compose para PostgreSQL local; o banco na nuvem (Supabase) usa as mesmas properties, só muda o `.env`
- Plano da guarda de autenticação do frontend em [`docs/PLANO-AUTH-FRONTEND.md`](docs/PLANO-AUTH-FRONTEND.md) (sem mudanças no repo do frontend)

## Como subir (local)

```bash
cp .env.example .env
docker compose up -d
./mvnw spring-boot:run
```

A API fica em `http://localhost:8080`. O frontend Vite deve usar `credentials: 'include'` e origem `http://localhost:5173` (já configurada em `app.allowed-hosts`).

### Usuários de demonstração (senha `123456`)

| Papel   | Identificador              |
|---------|----------------------------|
| ALUNO   | apelido `Gu`               |
| MONITOR | `maria.souza@exemplo.com`  |
| ADMIN   | nome `Administrador`       |

## Documentação

| Arquivo | Conteúdo |
|---------|----------|
| [docs/ARQUITETURA.md](docs/ARQUITETURA.md) | Camadas, pastas e o que melhorar em relação ao next-gen-force |
| [docs/PLANO-BACKEND.md](docs/PLANO-BACKEND.md) | Modelo, regras e mapa de endpoints × telas |
| [docs/PLANO-AUTH-FRONTEND.md](docs/PLANO-AUTH-FRONTEND.md) | Como ligar a guarda JWT/cookie no React Router |
| [docs/SUPABASE.md](docs/SUPABASE.md) | Banco grátis na nuvem |

## Publicar no GitHub

Este projeto nasceu como repositório Git **local** (o token do agente não pôde criar `davi112/beira-linha-play-backend`). Na sua máquina:

```bash
cd beira-linha-play-backend
gh repo create davi112/beira-linha-play-backend --public --source=. --remote=origin --push
```

Ou crie o repositório vazio no GitHub e:

```bash
git remote add origin git@github.com:davi112/beira-linha-play-backend.git
git push -u origin cursor/backend-spring-api-7b1e
```

```bash
./mvnw test
```

Os testes unitários cobrem correção de tentativas, conclusão da atividade e autenticação, **sem Docker**. Para exercitar o PostgreSQL de ponta a ponta, suba o Compose e use os endpoints `/api/auth/*`.
