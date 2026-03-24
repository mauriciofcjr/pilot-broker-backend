# Pilot Broker — Backend BFF

> Terminal eletrônico de Home Broker — Backend for Frontend (BFF)

## Visão Geral

O **Pilot Broker Backend** é uma REST API construída com Java 21 e Spring Boot 3.3.x que atua como BFF (Backend for Frontend) para o terminal eletrônico de Home Broker. Ele orquestra chamadas à [Financial Modeling Prep API](https://site.financialmodelingprep.com/developer/docs), entregando ao frontend Angular dados já agregados e formatados, sem jamais expor a API Key externa ao cliente.

---

## Tecnologias

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 21 (LTS) | Linguagem principal, Virtual Threads |
| Spring Boot | 3.3.5 (LTS) | Framework principal |
| Spring Security | embutido | Autenticação e autorização |
| Spring Data JPA | embutido | Persistência ORM |
| Flyway | embutido | Migrations de banco de dados |
| MySQL | 8.0 | Banco de dados (dev/prod) |
| H2 | embutido | Banco de dados (testes) |
| RabbitMQ | 3.x | Mensageria assíncrona |
| Caffeine | embutido | Cache in-memory |
| JWT (jjwt) | 0.11.5 | Tokens de autenticação |
| ModelMapper | 3.2.0 | Mapeamento de objetos |
| Lombok | embutido | Redução de boilerplate |
| Springdoc OpenAPI | 2.5.0 | Documentação Swagger |
| Docker | — | Containerização |

---

## Pré-requisitos

- Java 21+
- Maven 3.9+ (ou use o wrapper `./mvnw` incluído)
- Docker e Docker Compose
- MySQL 8.0 (local ou via Docker)
- RabbitMQ 3.x (local ou via Docker)

---

## Configuração do Ambiente

### 1. Variáveis de Ambiente

Copie o arquivo de exemplo e configure os valores:

```bash
cp ../.env.example .env
```

Edite o `.env` na raiz do workspace:

```env
FMP_API_KEY=sua_chave_aqui
JWT_SECRET=gere_com_openssl_rand_base64_32
DB_PASSWORD=
DB_USERNAME=root
RABBITMQ_USER=guest
RABBITMQ_PASS=guest
SPRING_PROFILES_ACTIVE=dev
APP_PORT=8080
```

> **Gerar JWT_SECRET seguro:**
> ```bash
> openssl rand -base64 32
> ```

### 2. Banco de Dados e Infraestrutura via Docker

Suba apenas MySQL e RabbitMQ para desenvolvimento local:

```bash
cd ..  # raiz do workspace (onde está o docker-compose.yml)
docker-compose up -d mysql rabbitmq
```

Aguarde os containers ficarem saudáveis:

```bash
docker-compose ps
# mysql e rabbitmq devem aparecer como "healthy"
```

---

## Executando a Aplicação

### Modo Desenvolvimento (local)

```bash
cd pilot-broker-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

A aplicação sobe em: **http://localhost:8080**

### Documentação da API (Swagger UI)

Acesse após subir a aplicação:

```
http://localhost:8080/swagger-ui.html
```

---

## Executando os Testes

Os testes utilizam H2 (in-memory) e não precisam de MySQL ou RabbitMQ rodando.

```bash
# Todos os testes
./mvnw test

# Testes de uma classe específica
./mvnw test -Dtest=AuthControllerIT

# Testes com relatório detalhado
./mvnw test -Dsurefire.useFile=false
```

> **Padrão TDD:** Todos os testes seguem o ciclo **RED → GREEN → REFACTOR**.

---

## Estrutura de Pacotes

```
src/main/java/com/pilotbroker/
├── web/
│   ├── controller/                # AuthController, UsuarioController, MarketController...
│   ├── dto/                       # DTOs de request/response organizados por entidade
│   │   ├── login/                 # LoginRequestDto, LoginResponseDto
│   │   └── usuario/               # UsuarioCreateDto, UsuarioResponseDto, UsuarioSenhaDto
│   └── mapper/                    # UsuarioMapper (ModelMapper)
├── model/                         # Entities JPA (Usuario, Order...)
├── repository/                    # Spring Data repositories
├── service/                       # Services (UsuarioService, AuthService...)
├── exception/                     # Exceções de domínio (UsernameUniqueViolationException...)
├── messaging/                     # RabbitMQ producers/consumers/config
└── shared/                        # Configurações e utilitários compartilhados
    ├── config/                    # SecurityConfig, OpenApiConfig, SpringJpaAuditingConfig, AppConfig
    ├── exception/                 # ApiExceptionHandler, ErrorMessage
    ├── interceptor/               # FmpApiKeyInterceptor
    └── security/                  # JwtTokenProvider, JwtAuthenticationFilter, JwtUserDetailsService
```

## Estrutura de Testes

A estrutura de testes espelha exatamente a de `src/main`. Cada arquivo de teste reside no mesmo pacote da classe que testa.

```
src/test/java/com/pilotbroker/
├── service/                       # Testes unitários de services
│   ├── UsuarioServiceTest.java
│   └── AuthServiceTest.java
├── shared/
│   └── security/                  # Testes unitários de segurança
│       └── JwtTokenProviderTest.java
└── web/
    └── controller/                # Testes de integração de controllers
        └── AuthControllerIT.java
```

---

## Endpoints da API

### Autenticação

| Método | Endpoint | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Pública | Cadastro de novo usuário |
| `POST` | `/api/v1/auth/login` | Pública | Login, retorna JWT |

### Usuários

| Método | Endpoint | Role | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/users/{id}` | ADMIN ou dono | Busca usuário por ID |
| `GET` | `/api/v1/users` | ADMIN | Lista todos os usuários |
| `PUT` | `/api/v1/users/{id}/password` | Dono | Altera senha |

### Mercado

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/dashboard` | Índices, ativos em destaque e calendário econômico |

### Ativos

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/v1/stocks/search?query=` | Busca por símbolo ou nome |
| `GET` | `/api/v1/stocks/screener` | Filtro avançado de ações |
| `GET` | `/api/v1/stocks/{symbol}` | Detalhes do ativo (cotação + gráfico + peers) |
| `GET` | `/api/v1/stocks/{symbol}/fundamentals` | Análise fundamentalista |
| `GET` | `/api/v1/stocks/{symbol}/governance` | Governança, dividendos e earnings |

### Negociação

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/v1/trade/order` | Envia ordem (async via RabbitMQ) |
| `GET` | `/api/v1/trade/orders` | Histórico de ordens do usuário |

> Todos os endpoints (exceto `/auth/**`) exigem header: `Authorization: Bearer <token>`

---

## Autenticação

O sistema usa **JWT (JSON Web Token)** com duração de 24 horas.

**Fluxo:**
1. `POST /api/v1/auth/login` com `{ "username": "email@exemplo.com", "password": "123456" }`
2. Resposta: `{ "token": "eyJ..." }`
3. Use o token em todas as requisições: `Authorization: Bearer eyJ...`

**Perfis de usuário:**
- `ROLE_ADMIN` — acesso total
- `ROLE_CLIENTE` — acesso apenas aos próprios dados

---

## Docker — Build Completo

Para subir toda a stack via Docker Compose (a partir da raiz do workspace):

```bash
cd ..
docker-compose up --build
```

Serviços disponíveis:

| Serviço | URL |
|---|---|
| API Backend | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| RabbitMQ Admin | http://localhost:15672 (guest/guest) |
| MySQL | localhost:3306 |

---

## Profiles

| Profile | Banco | Flyway | Log Level | Uso |
|---|---|---|---|---|
| `dev` | MySQL local | ✅ ativo | DEBUG | Desenvolvimento local |
| `prod` | MySQL prod | ✅ ativo | WARN | Produção |
| `test` | H2 in-memory | ❌ desabilitado | — | Testes automatizados |

---

## Arquitetura — Fluxo de Dados

```
Angular v20
    │
    │ HTTP/JSON (Bearer JWT)
    ▼
BFF Spring Boot  ──── FmpApiKeyInterceptor (injeta apikey)
    │                         │
    │                         ▼
    │                  FMP REST API
    │                  (financialmodelingprep.com)
    │
    ├── Caffeine Cache (@Cacheable, TTL por endpoint)
    ├── RabbitMQ (order.queue + quote.agg.queue)
    └── MySQL (usuários + ordens)
```

**Virtual Threads (Java 21):** Chamadas paralelas à FMP API para buscar cotação, gráfico e perfil simultaneamente, sem bloqueio de threads da JVM.

---

## Migrations (Flyway)

As migrations ficam em `src/main/resources/db/migration/`:

| Arquivo | Descrição |
|---|---|
| `V1__create_users.sql` | Tabela `usuarios` com auditoria |
| `V2__create_orders.sql` | Tabela `orders` para negociação |

---

## Padrões de Projeto

- **DDD** — Organização por domínio de negócio (auth, market, stock, trade)
- **TDD** — Testes escritos antes da implementação (RED → GREEN → REFACTOR)
- **SOLID** — Princípios aplicados em toda a base de código
- **Clean Code** — Nomenclatura clara, funções pequenas e coesas
- **BFF Pattern** — Backend orquestra e agrega dados para o frontend específico

---

## Git Flow

```
main          ← PROD
  └── develop ← Integração contínua
        └── feature/US-XX-nome-da-feature
```

Cada User Story tem sua própria branch. O merge para `develop` ocorre após aprovação dos testes manuais.

---

## Licença

Projeto privado — uso exclusivo do Pilot Broker.
