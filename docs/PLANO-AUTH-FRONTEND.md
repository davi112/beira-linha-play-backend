# Plano: guarda de autenticação no frontend

O `RequireAuth` de hoje só olha `user` no contexto e o `UserProvider` inicia já logado como `mockLoggedAluno`. Com JWT em cookie HttpOnly o JS **não lê o token** — a sessão vive no cookie e no `GET /api/auth/me`.

## Objetivo

- Visitante sem cookie válido → `/login`
- Sessão válida → rotas privadas
- Access token expirado → refresh automático, sem kick imediato
- Logout chama a API e limpa o estado
- Papéis (ALUNO / MONITOR / ADMIN) continuam no contexto, agora vindos da API

## 1. Cliente HTTP

Criar algo como `src/services/api.ts`:

```ts
const API = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export const api = async (path: string, init: RequestInit = {}) => {
  const response = await fetch(`${API}${path}`, {
    ...init,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(init.headers ?? {}) },
  });
  if (response.status !== 401 || path === '/api/auth/refresh') return response;
  const refresh = await fetch(`${API}/api/auth/refresh`, {
    method: 'POST',
    credentials: 'include',
  });
  if (!refresh.ok) return response;
  return fetch(`${API}${path}`, { ...init, credentials: 'include', headers: init.headers });
};
```

O cookie `blp_refresh` tem `Path=/api/auth`, então só login/cadastro/refresh/logout o enviam. O access cookie vai em todas as rotas `/`.

## 2. UserProvider: boot com loading

Deixar de usar `initialUser = mockLoggedAluno`.

Estados:

- `status: 'loading' | 'anonimo' | 'autenticado'`
- `user: UsuarioType | null`

No mount: `GET /api/auth/me`. 200 → autenticado. 401 → tenta `POST /api/auth/refresh`; se falhar, anônimo.

**Não renderizar o router privado até `status !== 'loading'`**, senão o `RequireAuth` manda todo mundo para `/login` no primeiro paint.

Tipos: a API **não devolve `senha`**. Ajuste `UsuarioType` para omitir senha (melhoria de segurança). IDs vêm como string.

## 3. RequireAuth

Hoje:

```tsx
if (!user) return <Navigate to="/login" replace />;
```

Passar a:

```tsx
if (status === 'loading') return <Splash ou null />;
if (status === 'anonimo') return <Navigate to="/login" replace />;
return <Outlet />;
```

Opcional: `RequirePapel` para rotas só de monitor (`nova-atividade`, `monitoramento`) e só de admin (criar curso). O backend já recusa; a guarda no front só evita flash de tela.

## 4. Login / cadastro / logout

`useLogin` / `useRegister`: `POST /api/auth/login` e `/api/auth/cadastro` com o mesmo body dos forms (`tipo`, `apelido`/`email`, `senha`, `nome`). 200/201 → `setUser(json)` e navega (`/` aluno, `/cursos` monitor).

Admin: o login da API aceita `{ tipo: 'ADMIN', nome, senha }`. O form atual não tem essa opção — dá para acrescentar depois.

Logout: o dialog hoje não chama `setUser(null)`. Plano:

```ts
await api('/api/auth/logout', { method: 'POST' });
setUser(null);
navigate('/login', { replace: true });
```

## 5. Rotas públicas vs privadas

Manter `/login` e `/cadastro` **fora** de `RequireAuth`. Se `status === 'autenticado'` nessas rotas, redirecionar (aluno → `/`, outros → `/cursos`) para ninguém “relogar” por engano.

## 6. Cookies em dev

Front `http://localhost:5173`, API `http://localhost:8080`: same-site. `SameSite=Lax` + `Secure=false` funciona.

Quando o front for outro domínio (Vercel etc.), a API precisa de:

```
APP_COOKIE_SECURE=true
APP_COOKIE_SAME_SITE=None
APP_FRONTEND_ORIGIN=https://seu-front
```

e HTTPS nos dois lados.

## 7. Checklist de telas depois da guarda

1. Login / cadastro / me / logout
2. Lista e detalhe de cursos + inscrição com código (persistida)
3. Módulos e atividades
4. Quiz: enviar tentativas; usar o campo `correta` só no feedback da API (não no GET da atividade)
5. Monitoramento, ranking, medalhas, editar conta

## 8. Testes no frontend

- `RequireAuth` com `status=loading` não navega
- `status=anonimo` → `/login`
- `status=autenticado` renderiza outlet
- Interceptor: 401 seguido de refresh 200 repete o GET
