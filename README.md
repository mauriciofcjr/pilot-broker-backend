# Pilot Broker Backend

Backend BFF (Backend for Frontend) da corretora Pilot Broker.
Integra a [Financial Modeling Prep API](https://site.financialmodelingprep.com/developer/docs) como proxy de dados de mercado, com previsão de migração futura para as [APIs da B3](https://developers.b3.com.br/apis).

---

## Stack

| Componente | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.6 |
| Segurança | Spring Security + JWT (jjwt 0.11.5) |
| Persistência | Spring Data JPA + Hibernate 6 |
| Banco DEV | MySQL 8.x |
| Banco Testes | H2 (in-memory) |
| Migrations | Flyway |
| Mensageria | RabbitMQ (Spring AMQP) |
| HTTP Client | Spring WebClient (WebFlux) |
| Mapeamento | ModelMapper |
| Documentação | SpringDoc OpenAPI (Swagger) |
| Boilerplate | Lombok |

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- MySQL 8.x rodando localmente na porta 3306
- RabbitMQ rodando localmente (ou via Docker)

```bash
# RabbitMQ via Docker
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

---

## Configuração

1. Copie o arquivo de exemplo de variáveis de ambiente:
```bash
cp .env.example .env
```

2. Edite o `.env` com seus valores:
```
FMP_API_KEY=sua_api_key_aqui
JWT_SECRET=seu_secret_seguro_de_64_chars
DB_USERNAME=root
DB_PASSWORD=
```

---

## Como rodar

```bash
# Perfil DEV (padrão)
mvn spring-boot:run

# Ou explicitamente
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Após subir, acesse:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/api-docs
- **Health:** http://localhost:8080/actuator/health

---

## Como testar

```bash
# Todos os testes
mvn test

# Com relatório de cobertura
mvn verify
```

---

## Estrutura de Pacotes

```
com.pilotbroker.backend/
├── config/          # Configurações Spring (Security, Swagger, RabbitMQ, etc.)
├── domain/
│   ├── entity/      # Entidades JPA
│   └── exception/   # Exceções de domínio
├── repository/      # Repositórios Spring Data
├── service/         # Serviços de negócio
├── web/
│   ├── controller/  # Controllers REST
│   ├── dto/         # Data Transfer Objects
│   │   └── mapper/  # ModelMapper mappers
│   └── exception/   # ApiExceptionHandler + ErrorMessage
└── PilotBrokerBackendApplication.java
```

---

## Padrões

- **Arquitetura:** BFF + DDD + Clean Code + SOLID
- **Testes:** TDD (RED → GREEN → REFACTOR) com JUnit 5 + Mockito
- **Banco:** Flyway controla o schema — `ddl-auto: validate` sempre
- **Segurança:** JWT no header `Authorization: Bearer <token>`
- **Documentação:** Swagger em Português BR

---

## Branches

| Branch | Ambiente |
|---|---|
| `main` | PROD |
| `develop` | DEV |
| `feature/*` | Feature em desenvolvimento |

