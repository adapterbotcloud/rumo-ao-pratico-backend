# 📚 Rumo ao Prático - Backend

> API REST para um sistema de estudo e quiz, construída com **Spring Boot 3.3** e **Java 21**. Permite criar questões, organizar por tópicos, gerar quizzes e acompanhar estatísticas de desempenho.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|--------|-----------|
| **Linguagem** | Java 21 |
| **Framework** | Spring Boot 3.3.5 |
| **Segurança** | Spring Security + JWT (jjwt 0.12.6) |
| **Banco de Dados** | PostgreSQL 16 |
| **ORM** | Spring Data JPA / Hibernate |
| **Migrações** | Flyway |
| **Documentação** | SpringDoc OpenAPI (Swagger UI) |
| **Build** | Maven |
| **Containers** | Docker & Docker Compose |

---

## 📋 Pré-requisitos

### Rodar com Docker (recomendado)
- [Docker](https://docs.docker.com/get-docker/) 20+
- [Docker Compose](https://docs.docker.com/compose/) v2+

### Rodar localmente
- Java 21 (JDK)
- Maven 3.9+
- PostgreSQL 16+

---

## 🚀 Como Executar

### 🐳 Com Docker Compose (modo mais fácil)

```bash
# Clonar o repositório
git clone https://github.com/CabraBot/rumo-ao-pratico-backend.git
cd rumo-ao-pratico-backend

# Subir tudo (banco + aplicação)
docker compose up -d

# Verificar logs
docker compose logs -f app
```

A API estará disponível em: `http://localhost:8080/api/v1`
Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`

### 💻 Localmente (desenvolvimento)

```bash
# 1. Subir apenas o PostgreSQL
docker compose up -d db

# 2. Configurar variáveis de ambiente (opcional, já tem defaults)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=rumo_ao_pratico
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# 3. Compilar e executar
mvn spring-boot:run
```

Ou compilar o JAR:

```bash
mvn clean package -DskipTests
java -jar target/rumo-ao-pratico-backend-1.0.0.jar
```

---

## ⚙️ Variáveis de Ambiente

| Variável | Default | Descrição |
|----------|---------|-----------|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `rumo_ao_pratico` | Nome do banco de dados |
| `DB_USERNAME` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |
| `JWT_SECRET` | (base64 embutido) | Chave secreta para assinar tokens JWT |
| `JWT_EXPIRATION` | `86400000` | Tempo de expiração do access token (ms) – 24h |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Tempo de expiração do refresh token (ms) – 7 dias |

---

## 📡 Endpoints da API

Todos os endpoints estão sob o prefixo `/api/v1`. Endpoints autenticados requerem o header:
```
Authorization: Bearer <token>
```

### 🔐 Autenticação (`/auth`)

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| `POST` | `/auth/register` | Registrar novo usuário | ❌ |
| `POST` | `/auth/login` | Login (retorna JWT) | ❌ |
| `POST` | `/auth/refresh` | Renovar access token | ❌ |
| `POST` | `/auth/forgot-password` | Recuperação de senha (mock) | ❌ |

### 👤 Usuários (`/users`)

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| `GET` | `/users/me` | Perfil do usuário autenticado | ✅ |
| `PUT` | `/users/me` | Atualizar perfil | ✅ |

### 📂 Tópicos (`/topics`)

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| `GET` | `/topics` | Listar tópicos (paginado) | ✅ |
| `POST` | `/topics` | Criar tópico | ✅ |
| `GET` | `/topics/{id}` | Buscar tópico por ID | ✅ |
| `PUT` | `/topics/{id}` | Atualizar tópico | ✅ |
| `DELETE` | `/topics/{id}` | Excluir tópico | ✅ |

### ❓ Questões (`/questions`)

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| `GET` | `/questions` | Listar questões (filtros + paginação) | ✅ |
| `POST` | `/questions` | Criar questão | ✅ |
| `GET` | `/questions/{id}` | Buscar questão por ID | ✅ |
| `PUT` | `/questions/{id}` | Atualizar questão | ✅ |
| `DELETE` | `/questions/{id}` | Excluir questão (soft delete) | ✅ |
| `POST` | `/questions/import` | Importar questões em lote (JSON) | ✅ |

**Filtros disponíveis em `GET /questions`:**
- `topicId` – UUID do tópico
- `type` – `MULTIPLE_CHOICE`, `TRUE_FALSE`, `FLASHCARD`, `COMMENTED_PHRASE`
- `difficulty` – `EASY`, `MEDIUM`, `HARD`
- `search` – busca textual no enunciado

### 🎯 Quiz (`/quiz`)

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| `POST` | `/quiz/generate` | Gerar novo quiz | ✅ |
| `GET` | `/quiz/attempts` | Listar tentativas (paginado) | ✅ |
| `GET` | `/quiz/attempts/{id}` | Detalhes de uma tentativa | ✅ |
| `POST` | `/quiz/attempts/{id}/answer` | Responder uma questão do quiz | ✅ |
| `POST` | `/quiz/attempts/{id}/finish` | Finalizar tentativa | ✅ |

**Modos de quiz:** `EVALUATION` (avaliação) | `STUDY` (estudo)

### 📊 Estatísticas (`/stats`)

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| `GET` | `/stats/dashboard` | Dashboard com estatísticas gerais | ✅ |

---

## 🗄️ Modelo de Dados

```
users
 ├── topics (hierárquico, com parent_id)
 │    └── questions
 │         └── question_options
 ├── quiz_attempts
 │    └── quiz_answers
```

### Tipos de Questão
- **MULTIPLE_CHOICE** – Múltipla escolha com opções
- **TRUE_FALSE** – Verdadeiro ou Falso
- **FLASHCARD** – Cartão de memorização
- **COMMENTED_PHRASE** – Frase comentada

### Níveis de Dificuldade
- **EASY** – Fácil
- **MEDIUM** – Médio
- **HARD** – Difícil

---

## 📁 Estrutura do Projeto

```
src/main/java/com/rumoaopratico/
├── config/                   # Configurações (Security, CORS, Swagger, Jackson)
│   ├── CorsConfig.java
│   ├── JacksonConfig.java
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
├── controller/               # Controllers REST
│   ├── AuthController.java
│   ├── QuestionController.java
│   ├── QuizController.java
│   ├── StatsController.java
│   ├── TopicController.java
│   └── UserController.java
├── dto/                      # Data Transfer Objects
│   ├── auth/                 # Login, Register, Token, Refresh, ForgotPassword
│   ├── question/             # Request, Response, Option, Import
│   ├── quiz/                 # Generate, Answer, Attempt
│   ├── stats/                # DashboardStats
│   ├── topic/                # Request, Response
│   └── user/                 # Update, Response
├── exception/                # Exceções customizadas + handler global
│   ├── BadRequestException.java
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
├── model/                    # Entidades JPA
│   ├── Difficulty.java
│   ├── Question.java
│   ├── QuestionOption.java
│   ├── QuestionType.java
│   ├── QuizAnswer.java
│   ├── QuizAttempt.java
│   ├── QuizMode.java
│   ├── Topic.java
│   └── User.java
├── repository/               # Spring Data JPA Repositories
│   ├── QuestionOptionRepository.java
│   ├── QuestionRepository.java
│   ├── QuizAnswerRepository.java
│   ├── QuizAttemptRepository.java
│   ├── TopicRepository.java
│   └── UserRepository.java
├── security/                 # JWT + Spring Security
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── SecurityUser.java
│   └── UserDetailsServiceImpl.java
├── service/                  # Lógica de negócio
│   ├── AuthService.java
│   ├── QuestionService.java
│   ├── QuizService.java
│   ├── StatsService.java
│   ├── TopicService.java
│   └── UserService.java
└── RumoAoPraticoApplication.java

src/main/resources/
├── application.yml           # Configuração principal
└── db/migration/
    ├── V1__create_tables.sql # Criação das tabelas
    └── V2__seed_data.sql     # Dados iniciais

src/test/
├── java/com/rumoaopratico/
│   ├── RumoAoPraticoApplicationTests.java
│   └── service/
│       ├── AuthServiceTest.java
│       └── QuestionServiceTest.java
└── resources/
    └── application-test.yml  # Config de testes (H2)
```

---

## 🧪 Testes

```bash
# Executar testes (usa H2 em memória)
mvn test

# Com cobertura
mvn test jacoco:report
```

---

## 📖 Documentação da API (Swagger)

Com a aplicação rodando, acesse:

- **Swagger UI:** http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api/v1/api-docs

Para testar endpoints autenticados no Swagger:
1. Use `POST /auth/register` ou `POST /auth/login` para obter um token
2. Clique em "Authorize" (🔒) no topo da página
3. Cole o token no formato: `Bearer <seu-token>`

---

## 🔒 Segurança

- Senhas são hasheadas com **BCrypt**
- Autenticação via **JWT** (JSON Web Tokens)
- Access Token expira em **24 horas** (configurável)
- Refresh Token expira em **7 dias** (configurável)
- Rotas públicas: `/auth/**`, Swagger UI, health checks
- Todas as outras rotas requerem token válido
- Dados isolados por usuário (multi-tenant por design)

---

## 📜 Licença

Este projeto é de uso privado/educacional.
