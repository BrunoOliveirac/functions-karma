# Karma CRM — Backend

> Projeto de estudo em Java/Spring Boot. API REST para um CRM simples, pensado para praticar autenticação, persistência de dados, autorização por perfil e organização de código.

Painel web que consome esta API: [panel-karma](https://github.com/BrunoOliveirac/panel-karma).

[Português (pt-BR)](#português-pt-br) · [English](#english)

---

## Português (pt-BR)

### Sumário

- [O que é este projeto?](#o-que-é-este-projeto)
- [Tipos de usuário e acessos](#tipos-de-usuário-e-acessos)
- [O que já funciona hoje](#o-que-já-funciona-hoje)
- [Segurança](#segurança)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Configuração do ambiente](#configuração-do-ambiente)
- [Como rodar](#como-rodar)
- [Documentação da API (Swagger)](#documentação-da-api-swagger)
- [Estrutura resumida](#estrutura-resumida)
- [Observações](#observações)

### O que é este projeto?

O **Karma CRM** é um **projeto de aprendizado** — não é um produto comercial nem um sistema completo. A ideia é experimentar, na prática, como funciona o **backend** (a parte “invisível” que roda no servidor) de um CRM: receber pedidos de um aplicativo ou site, validar quem está logado, aplicar permissões por tipo de usuário e salvar informações no banco de dados.

### Tipos de usuário e acessos

A API reconhece quatro tipos. O cadastro público (`POST /auth/register`) sempre cria uma conta **USER**. Os demais tipos são criados por fluxos internos (admin cria suporte; usuário cria ou vincula membro).

| Tipo | Quem é | O que pode usar |
| ---- | ------ | --------------- |
| **ADMIN** | Administrador da plataforma | Login, logout, **perfil** e gestão de **suportes** |
| **USER** | Dono da conta / tenant | Login, logout, cadastro, **perfil**, **notificações**, **membros**, **clientes**, **projetos** e **setores** |
| **MEMBER** | Colaborador vinculado a um USER | Login, logout, **perfil**, **notificações**, **clientes**, **projetos** e **setores** |
| **SUPPORT** | Equipe de atendimento | Login, logout, **perfil** e **notificações** |

#### Matriz de acesso

| Recurso | Admin | Usuário | Membro | Suporte |
| ------- | :---: | :-----: | :----: | :-----: |
| **Login / logout** | Sim | Sim | Sim | Sim |
| **Cadastro** | Público (cria USER) | Público (cria USER) | Público (cria USER) | Público (cria USER) |
| **Perfil** | Sim | Sim | Sim | Sim |
| **Notificações** | — | Sim | Sim | Sim |
| **Suportes** | Sim | — | — | — |
| **Membros** | — | Sim | — | — |
| **Clientes** | — | Sim | Sim | — |
| **Projetos** | — | Sim | Sim | — |
| **Setores** | — | Sim | Sim | — |

Rotas protegidas exigem `Authorization: Bearer <JWT>`. Swagger (`/swagger-ui/**`) e `/auth/**` são públicos.

### O que já funciona hoje

#### Autenticação (público)

| Fluxo | O que faz |
| ----- | --------- |
| **Login** (`POST /auth/login`) | Valida e-mail e senha. Devolve um JWT. Conta inativa não entra. Após **5 tentativas** falhas, a conta trava por **5 minutos** (`429`). |
| **Cadastro** (`POST /auth/register`) | Cria usuário do tipo USER e devolve um JWT. |
| **Logout** (`POST /auth/logout`) | Revoga o JWT atual (idempotente). |

O token vale até o **início do dia seguinte** (fuso local). Tokens revogados entram em lista negra; um job diário (00:05) limpa os expirados.

#### Perfil — todos os tipos

| Fluxo | O que faz |
| ----- | --------- |
| **Consultar** (`GET /profile`) | Nome, e-mail, avatar e tipo do usuário logado. |
| **Atualizar** (`PUT /profile`) | Nome, e-mail, avatar (png/jpeg/webp em data-URL) e senha opcional. Se e-mail ou senha mudam, o JWT antigo é revogado e a API devolve um token novo. |

#### Notificações — USER, MEMBER e SUPPORT

| Fluxo | O que faz |
| ----- | --------- |
| **Últimas** (`GET /notifications/latest/{userId}`) | 3 mais recentes + flag de não lidas (sino do painel). |
| **Lista** (`GET /notifications/list/{userId}`) | Paginação (10 por página), busca, filtro todas/não lidas/lidas e contadores. |
| **Tempo real** (`GET /notifications/stream`) | SSE: avisa quando chega notificação nova; keepalive a cada 15s. |
| **Marcar lida** | Uma (`PATCH .../mark-as-read/{id}`) ou todas (`PATCH .../mark-all-as-read/{userId}`). |
| **Excluir** (`DELETE /notifications/{id}`) | Exclusão lógica. |

Eventos gerados hoje (ao gerenciar membros):

| Código | Quando |
| ------ | ------ |
| `linked_by_user` | Um USER vincula um membro à conta |
| `unlinked_by_user` | Um USER desvincula um membro |
| `linked_to_project` | Um membro é associado a um ou mais projetos |

#### Admin — suportes

| Fluxo | O que faz |
| ----- | --------- |
| **Suportes** | Listar, criar/editar, checar e-mail, ativar/desativar, trocar senha e excluir (exclusão lógica + anonimização do e-mail). |

#### Usuário — membros

| Fluxo | O que faz |
| ----- | --------- |
| **Membros** | Listar membros vinculados (com busca e paginação), criar membro, vincular um existente por e-mail, sincronizar projetos do membro, desvincular, trocar senha e excluir. |

O e-mail do membro pode estar **disponível**, **pronto para vincular**, **já vinculado** ou **em uso** por outro tipo de conta.

#### Usuário e membro — CRM

| Fluxo | O que faz |
| ----- | --------- |
| **Clientes** | Listar, consultar, criar/editar (nome, e-mail, telefone, observações, orçamento, setor, favorito), favoritar e excluir. |
| **Projetos** | Listar (todos ou só ativos), consultar, criar/editar (exige cliente), ativar/desativar e excluir. |
| **Setores** | Listar (todos ou só ativos), consultar, criar/editar (incluindo o flag `active`) e excluir. |

Essas rotas exigem token. A exclusão é lógica (`deleted_at`).

### Segurança

- API **stateless** (sem sessão no servidor); CSRF desligado; JWT no header.
- CORS liberado para `http://localhost:3000`.
- Strings de query/formulário passam por sanitização HTML (Jsoup).
- E-mails validados com anotação própria (`@ValidEmail`).
- `/error` é público para erros MVC não virarem `403`.

### Tecnologias

- **Java 21** — linguagem principal
- **Spring Boot** — framework que facilita criar APIs web
- **PostgreSQL** — banco de dados relacional
- **Flyway** — aplica scripts de criação/atualização das tabelas automaticamente
- **JWT** — tokens para manter o usuário autenticado sem sessão no servidor
- **Swagger (OpenAPI)** — documentação interativa dos endpoints

### Pré-requisitos

Para rodar localmente, você precisa ter instalado:

1. **JDK 21** — [Adoptium](https://adoptium.net/) ou outra distribuição compatível
2. **PostgreSQL** — [postgresql.org/download](https://www.postgresql.org/download/)
3. **Git** — para clonar o repositório

Não é obrigatório instalar Gradle manualmente: o projeto inclui o **Gradle Wrapper** (`gradlew` / `gradlew.bat`).

### Configuração do ambiente

O arquivo `src/main/resources/application.properties` define o perfil padrão como **development**:

```properties
spring.profiles.default=development
```

As configurações sensíveis (senha do banco, segredo do JWT etc.) ficam em arquivos separados por ambiente, por exemplo:

`src/main/resources/application-development.properties`

Esses arquivos **não vão para o Git** (estão no `.gitignore`), para não expor credenciais em um repositório público.

**Como configurar:**

1. Copie o arquivo de exemplo:

   ```bash
   cp src/main/resources/application-development.properties.example src/main/resources/application-development.properties
   ```

   No Windows (PowerShell):

   ```powershell
   Copy-Item src/main/resources/application-development.properties.example src/main/resources/application-development.properties
   ```

2. Edite `application-development.properties` com **seus** dados:

   | Propriedade | Significado |
   | ----------- | ----------- |
   | `jwt.secret` | Chave secreta usada para assinar os tokens. Em produção, use um valor longo e aleatório. |
   | `spring.datasource.url` | Endereço do PostgreSQL. O padrão do exemplo aponta para `localhost:5432` e banco `karma_homol`. |
   | `spring.datasource.username` | Usuário do PostgreSQL (ex.: `postgres`). |
   | `spring.datasource.password` | Senha do PostgreSQL. |

3. Crie o banco de dados no PostgreSQL (se ainda não existir):

   ```sql
   CREATE DATABASE karma_homol;
   ```

Ao subir a aplicação, o **Flyway** executa as migrações em `src/main/resources/db/migration/` e cria/atualiza as tabelas.

### Como rodar

Na raiz do projeto:

**Linux / macOS:**

```bash
./gradlew bootRun
```

**Windows:**

```powershell
.\gradlew.bat bootRun
```

Se tudo estiver certo, a API sobe em **http://localhost:8080**.

Para usar outro perfil (ex.: `staging`), defina a variável de ambiente `SPRING_PROFILES_ACTIVE` antes de rodar.

### Documentação da API (Swagger)

Com a aplicação rodando, abra no navegador:

**http://localhost:8080/swagger-ui/index.html**

Lá você vê todos os endpoints, pode testar o login e usar o botão **Authorize** para colar o token JWT nas rotas protegidas.

### Estrutura resumida

```
src/main/java/com/crm/karma/
├── controllers/   → rotas HTTP (Auth, Profile, Notifications, …)
├── services/      → regras de negócio
├── repositories/  → acesso ao banco
├── models/        → entidades (User, Client, Notification, …)
├── requests/      → DTOs de entrada
├── responses/     → DTOs de saída
├── filters/       → filtro JWT
├── validations/   → validação de e-mail
└── configs/       → segurança, CORS, OpenAPI, sanitização
```

### Observações

- Projeto **em evolução** — escopo pequeno de propósito, para estudo.
- CORS está liberado para `http://localhost:3000`, o endereço padrão do painel local.
- Dúvidas ou sugestões: abra uma **Issue** no GitHub.

---

## English

### Contents

- [What is this project?](#what-is-this-project)
- [User types and access](#user-types-and-access)
- [What works today](#what-works-today)
- [Security](#security)
- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Environment configuration](#environment-configuration)
- [How to run](#how-to-run)
- [API documentation (Swagger)](#api-documentation-swagger)
- [Project layout (brief)](#project-layout-brief)
- [Notes](#notes)

### What is this project?

**Karma CRM** is a **learning project** — not a commercial product or a full-featured system. It explores how the **backend** of a simple CRM works: receiving requests from an app or website, verifying who is logged in, applying role-based authorization, and storing data in a database.

### User types and access

The API recognizes four types. Public registration (`POST /auth/register`) always creates a **USER** account. Other types come from internal flows (admin creates support; a user creates or links a member).

| Type | Who they are | What they can use |
| ---- | ------------ | ----------------- |
| **ADMIN** | Platform administrator | Login, logout, **profile**, and **support staff** management |
| **USER** | Account / tenant owner | Login, logout, register, **profile**, **notifications**, **members**, **clients**, **projects**, and **sectors** |
| **MEMBER** | Collaborator linked to a USER | Login, logout, **profile**, **notifications**, **clients**, **projects**, and **sectors** |
| **SUPPORT** | Support staff | Login, logout, **profile**, and **notifications** |

#### Access matrix

| Feature | Admin | User | Member | Support |
| ------- | :---: | :--: | :----: | :-----: |
| **Login / logout** | Yes | Yes | Yes | Yes |
| **Register** | Public (creates USER) | Public (creates USER) | Public (creates USER) | Public (creates USER) |
| **Profile** | Yes | Yes | Yes | Yes |
| **Notifications** | — | Yes | Yes | Yes |
| **Support staff** | Yes | — | — | — |
| **Members** | — | Yes | — | — |
| **Clients** | — | Yes | Yes | — |
| **Projects** | — | Yes | Yes | — |
| **Sectors** | — | Yes | Yes | — |

Protected routes require `Authorization: Bearer <JWT>`. Swagger (`/swagger-ui/**`) and `/auth/**` are public.

### What works today

#### Authentication (public)

| Flow | Description |
| ---- | ----------- |
| **Login** (`POST /auth/login`) | Validates email and password. Returns a JWT. Inactive accounts cannot sign in. After **5 failed attempts**, the account locks for **5 minutes** (`429`). |
| **Register** (`POST /auth/register`) | Creates a USER account and returns a JWT. |
| **Logout** (`POST /auth/logout`) | Revokes the current JWT (idempotent). |

The token lasts until the **start of the next calendar day** (local timezone). Revoked tokens are blacklisted; a daily job (00:05) purges expired ones.

#### Profile — all types

| Flow | Description |
| ---- | ----------- |
| **Get** (`GET /profile`) | Name, email, avatar, and type of the logged-in user. |
| **Update** (`PUT /profile`) | Name, email, avatar (png/jpeg/webp data-URL), and optional password. If email or password changes, the old JWT is revoked and the API returns a new token. |

#### Notifications — USER, MEMBER, and SUPPORT

| Flow | Description |
| ---- | ----------- |
| **Latest** (`GET /notifications/latest/{userId}`) | Last 3 items plus an unread flag (panel bell). |
| **List** (`GET /notifications/list/{userId}`) | Pagination (10 per page), search, all/unread/read filter, and tab counts. |
| **Realtime** (`GET /notifications/stream`) | SSE: signals a new notification; keepalive every 15s. |
| **Mark as read** | One (`PATCH .../mark-as-read/{id}`) or all (`PATCH .../mark-all-as-read/{userId}`). |
| **Delete** (`DELETE /notifications/{id}`) | Soft delete. |

Events generated today (from member management):

| Code | When |
| ---- | ---- |
| `linked_by_user` | A USER links a member to the account |
| `unlinked_by_user` | A USER unlinks a member |
| `linked_to_project` | A member is assigned to one or more projects |

#### Admin — support staff

| Flow | Description |
| ---- | ----------- |
| **Support staff** | List, create/update, check email, activate/deactivate, change password, and delete (soft delete + email anonymization). |

#### User — members

| Flow | Description |
| ---- | ----------- |
| **Members** | List linked members (search and pagination), create a member, link an existing one by email, sync the member's projects, unlink, change password, and delete. |

A member email can be **available**, **ready to link**, **already linked**, or **in use** by another account type.

#### User and member — CRM

| Flow | Description |
| ---- | ----------- |
| **Clients** | List, get, create/update (name, email, phone, notes, budget, sector, favorite), favorite, and delete. |
| **Projects** | List (all or active only), get, create/update (requires a client), toggle active, and delete. |
| **Sectors** | List (all or active only), get, create/update (including the `active` flag), and delete. |

These routes require a token. Deletes are soft (`deleted_at`).

### Security

- **Stateless** API (no server session); CSRF off; JWT in the header.
- CORS allows `http://localhost:3000`.
- Query/form strings are HTML-sanitized (Jsoup).
- Emails are validated with a custom annotation (`@ValidEmail`).
- `/error` is public so MVC errors do not become `403`.

### Tech stack

- **Java 21**
- **Spring Boot**
- **PostgreSQL**
- **Flyway** — database migrations
- **JWT** — stateless authentication
- **Swagger (OpenAPI)** — interactive API docs

### Prerequisites

1. **JDK 21**
2. **PostgreSQL**
3. **Git**

Gradle is bundled via the **Gradle Wrapper** — no separate Gradle install required.

### Environment configuration

`src/main/resources/application.properties` sets the default profile to **development**:

```properties
spring.profiles.default=development
```

Sensitive settings (database password, JWT secret, etc.) live in environment-specific files such as:

`src/main/resources/application-development.properties`

Those files are **not committed** (listed in `.gitignore`) so credentials stay out of a public repo.

**Setup steps:**

1. Copy the example file:

   ```bash
   cp src/main/resources/application-development.properties.example src/main/resources/application-development.properties
   ```

   On Windows (PowerShell):

   ```powershell
   Copy-Item src/main/resources/application-development.properties.example src/main/resources/application-development.properties
   ```

2. Edit `application-development.properties` with **your** values:

   | Property | Meaning |
   | -------- | ------- |
   | `jwt.secret` | Secret key for signing tokens. Use a long random value in production. |
   | `spring.datasource.url` | PostgreSQL JDBC URL (example uses `localhost:5432`, database `karma_homol`). |
   | `spring.datasource.username` | PostgreSQL user (e.g. `postgres`). |
   | `spring.datasource.password` | PostgreSQL password. |

3. Create the database if needed:

   ```sql
   CREATE DATABASE karma_homol;
   ```

On startup, **Flyway** runs migrations from `src/main/resources/db/migration/`.

### How to run

From the project root:

**Linux / macOS:**

```bash
./gradlew bootRun
```

**Windows:**

```powershell
.\gradlew.bat bootRun
```

The API listens on **http://localhost:8080**.

To use another profile (e.g. `staging`), set the `SPRING_PROFILES_ACTIVE` environment variable before running.

### API documentation (Swagger)

With the app running, open:

**http://localhost:8080/swagger-ui/index.html**

You can try endpoints there and use **Authorize** to paste the JWT for protected routes.

### Project layout (brief)

```
src/main/java/com/crm/karma/
├── controllers/   → HTTP routes (Auth, Profile, Notifications, …)
├── services/      → business logic
├── repositories/  → database access
├── models/        → entities (User, Client, Notification, …)
├── requests/      → inbound DTOs
├── responses/     → outbound DTOs
├── filters/       → JWT filter
├── validations/   → email validation
└── configs/       → security, CORS, OpenAPI, sanitization
```

### Notes

- **Work in progress** — intentionally small scope for learning.
- CORS allows `http://localhost:3000`, the default local panel URL.
- Questions or feedback: open a **GitHub Issue**.
