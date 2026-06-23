# Karma CRM — Backend

> Projeto de estudo em Java/Spring Boot. API REST para um CRM simples, pensado para praticar autenticação, persistência de dados e organização de código.

---

## Português (pt-BR)

### O que é este projeto?

O **Karma CRM** é um **projeto de aprendizado** — não é um produto comercial nem um sistema completo. A ideia é experimentar, na prática, como funciona o **backend** (a parte “invisível” que roda no servidor) de um CRM: receber pedidos de um aplicativo ou site, validar quem está logado e salvar informações no banco de dados.

Em termos simples: imagine um caderno digital onde cada usuário cadastra seus **clientes**. Este repositório cuida de **registrar usuários**, **fazer login** e **gerenciar clientes** de forma segura.

### O que já funciona hoje?

Por enquanto existem **dois fluxos principais**:

| Fluxo            | O que faz                                                                                                                                                                         |
|------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Autenticação** | Criar conta (`/auth/register`) e entrar no sistema (`/auth/login`). Após o login, a API devolve um **token** (como um “passe”) que identifica o usuário nas próximas requisições. |
| **Clientes**     | Listar, criar, editar, favoritar e excluir clientes vinculados a um usuário. Essas rotas exigem o token de login.                                                                 |
| **Projetos**     | Listar, criar, editar, ativar/desativar e excluir projetos vinculados a um usuário. Essas rotas exigem o token de login.                                                          |
| **Setores**      | Listar, criar, editar, ativar/desativar e excluir setores vinculados a um usuário. Essas rotas exigem o token de login.                                                           |

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

   | Propriedade                  | Significado                                                                                     |
   | ---------------------------- | ----------------------------------------------------------------------------------------------- |
   | `jwt.secret`                 | Chave secreta usada para assinar os tokens. Em produção, use um valor longo e aleatório.        |
   | `spring.datasource.url`      | Endereço do PostgreSQL. O padrão do exemplo aponta para `localhost:5432` e banco `karma_homol`. |
   | `spring.datasource.username` | Usuário do PostgreSQL (ex.: `postgres`).                                                        |
   | `spring.datasource.password` | Senha do PostgreSQL.                                                                            |

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
├── controllers/   → rotas HTTP (Auth, Clients, …)
├── services/      → regras de negócio
├── repositories/  → acesso ao banco
├── models/        → entidades (User, Client, …)
└── configs/       → segurança, CORS, OpenAPI
```

### Observações

- Projeto **em evolução** — escopo pequeno de propósito, para estudo.
- CORS está liberado para `http://localhost:3000`, caso exista um front-end local no futuro.
- Dúvidas ou sugestões: abra uma **Issue** no GitHub.

---

## English

### What is this project?

**Karma CRM** is a **learning project** — not a commercial product or a full-featured system. It explores how the **backend** of a simple CRM works: receiving requests from an app or website, verifying who is logged in, and storing data in a database.

In plain terms: each user keeps a list of **clients**. This repository handles **sign-up**, **login**, and **client management** in a secure way.

### What works today?

There are **two main flows** so far:

| Flow               | Description                                                                                                                                            |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Authentication** | Create an account (`/auth/register`) and sign in (`/auth/login`). After login, the API returns a **token** that identifies the user on later requests. |
| **Clients**        | List, create, update, favorite, and delete clients for a user. These routes require a valid login token.                                               |
| **Projects**       | List, create, update, delete, and activate/deactivate projects for a user. These routes require a valid login token.                                   |
| **Sectors**        | List, create, update, delete, and activate/deactivate sector for a user. These routes require a valid login token.                                     |

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

   | Property                     | Meaning                                                                      |
   | ---------------------------- | ---------------------------------------------------------------------------- |
   | `jwt.secret`                 | Secret key for signing tokens. Use a long random value in production.        |
   | `spring.datasource.url`      | PostgreSQL JDBC URL (example uses `localhost:5432`, database `karma_homol`). |
   | `spring.datasource.username` | PostgreSQL user (e.g. `postgres`).                                           |
   | `spring.datasource.password` | PostgreSQL password.                                                         |

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
├── controllers/   → HTTP routes (Auth, Clients, …)
├── services/      → business logic
├── repositories/  → database access
├── models/        → entities (User, Client, …)
└── configs/       → security, CORS, OpenAPI
```

### Notes

- **Work in progress** — intentionally small scope for learning.
- CORS allows `http://localhost:3000` for a possible local front-end.
- Questions or feedback: open a **GitHub Issue**.
