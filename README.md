# OrbittAPI — Backend (SOA + DDD)

> Global Solution 2026.1 — Disciplinas combinadas de **SOA** e **DDD** (entrega única em Java).
> Tema do semestre: **Indústria Espacial**. ODS principal: **13 (Clima)**. ODS secundário: **2 (Agricultura)**.

OrbittAPI é uma plataforma SaaS de **dados satelitais** (NDVI, uso do solo, risco de alagamento, desmatamento, expansão urbana) entregue via API REST. Este repositório implementa o **backend** com 2 microserviços + 1 gateway, escritos em Java 21 / Spring Boot 3.3.

## Como a solução se conecta à Indústria Espacial e aos ODS

- **Indústria Espacial:** transformamos dados de satélites (Landsat, Sentinel/Copernicus) em métricas acionáveis acessíveis por uma API simples. Empresas de agronegócio, seguradoras e construtoras podem consumir inteligência espacial sem manter infraestrutura própria.
- **ODS 13 (Ação contra a mudança global do clima):** o NDVI e os índices de uso do solo permitem monitorar vegetação, perdas florestais e impacto de eventos climáticos.
- **ODS 2 (Fome zero e agricultura sustentável):** o índice de vegetação ajuda produtores rurais a tomar decisões sobre irrigação, plantio e colheita.

---

## Stack

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.3.5
- **Build:** Maven multi-módulo (parent + 3 módulos)
- **Persistência:** PostgreSQL 16 (um schema por bounded context)
- **Cache:** Redis 7 (TTL 6h em consultas satelitais)
- **Segurança:** JWT (`jjwt` 0.12.6), BCrypt para senhas
- **Validação:** Spring Validation + invariantes nos value objects
- **Documentação:** Springdoc OpenAPI / Swagger UI por serviço
- **DTOs:** MapStruct disponível (uso opcional onde simplifica)
- **Testes:** JUnit 5 + Mockito + AssertJ + Testcontainers
- **Empacotamento:** Docker + docker-compose (sobe tudo com 1 comando)

---

## Arquitetura

```
                       +------------------+
       Cliente  --->   |     Gateway      |   8080
                       +--------+---------+   valida JWT, injeta X-User-Id
                                |
                +---------------+----------------+
                |                                |
       +--------v---------+              +-------v----------+
       | identity-service |  8081        | satellite-service|  8082
       |                  |              |                  |
       | Bounded context: |              | Bounded context: |
       | Identity&Access  |              | Satellite Data   |
       +--------+---------+              +-------+----------+
                |                                |
                |    +----------+   +---------+  |
                +--->| Postgres |   |  Redis  |<-+
                     | 5432     |   | 6379    |
                     +----------+   +---------+
                identity_db /   cache landuse:{lat}:{lng}
                satellite_db   vegetation:{lat}:{lng} TTL 6h
```

Detalhes em [docs/architecture.md](docs/architecture.md).

---

## Estrutura de pastas

```
orbittapi-backend/
├── pom.xml                       # parent multi-modulo
├── docker-compose.yml            # sobe tudo
├── docs/
│   └── architecture.md           # bounded contexts + mapeamento DDD <-> codigo
├── docker/postgres/init.sql      # cria identity_db e satellite_db
│
├── identity-service/             # bounded context: Identity & Access
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/br/com/orbittapi/identity/
│       ├── IdentityApplication.java
│       ├── domain/               # Account, Email, Password, ApiKey, events, repository (interfaces)
│       ├── application/          # use cases (Register, Login, GetMyProfile, RevokeApiKey)
│       ├── infrastructure/       # JPA, JWT, security, event publisher
│       └── interfaces/rest/      # AuthController, MeController, ApiKeyController, GlobalExceptionHandler
│
├── satellite-service/            # bounded context: Satellite Data
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/br/com/orbittapi/satellite/
│       ├── SatelliteApplication.java
│       ├── domain/               # SatelliteQuery, Coordinate, NdviScore, LandUseDistribution, port SatelliteDataSource
│       ├── application/          # use cases (GetLandUse, GetVegetation)
│       ├── infrastructure/       # MockSatelliteDataSource (adapter), Redis cache, JPA
│       └── interfaces/rest/      # LandUseController, VegetationController, GlobalExceptionHandler
│
└── gateway/                      # Spring Cloud Gateway
    ├── pom.xml
    ├── Dockerfile
    └── src/main/java/br/com/orbittapi/gateway/
        ├── GatewayApplication.java
        ├── config/               # JwtProperties
        └── filter/               # JwtAuthenticationGatewayFilter (valida + injeta X-User-Id)
```

Cada serviço segue a estrutura DDD clássica: `domain → application → infrastructure → interfaces/rest`.

---

## Bounded contexts e linguagem ubíqua

| Conceito DDD              | Onde aparece                                                                          |
|---------------------------|---------------------------------------------------------------------------------------|
| **Entidade**              | `Account`, `SatelliteQuery` (igualdade por id)                                        |
| **Value Object**          | `Email`, `Password`, `ApiKey`, `Coordinate`, `NdviScore`, `LandUseDistribution`       |
| **Agregado (raiz)**       | `Account` (protege invariantes de senha/email/API key)                                |
| **Domain Event**          | `AccountRegistered`, `ApiKeyRevoked`, `QueryExecuted`                                 |
| **Repository (porta)**    | `AccountRepository`, `SatelliteQueryRepository` (interfaces em `domain/`)             |
| **Repository (impl)**     | `AccountRepositoryImpl`, `SatelliteQueryRepositoryImpl` em `infrastructure/persistence/` |
| **Application Service**   | `RegisterAccountUseCase`, `LoginUseCase`, `GetLandUseUseCase`, `GetVegetationUseCase`, etc. |
| **Anti-Corruption Layer** | `SatelliteDataSource` (porta) + `MockSatelliteDataSource` (adapter)                   |
| **Ubiquitous Language**   | `Account`, `ApiKey`, `SatelliteQuery`, `VegetationHealth`. Sem `Manager`/`Helper`/`Data` |

Lista completa de mapeamento em [docs/architecture.md](docs/architecture.md).

---

## Como subir

Pré-requisitos: Docker + Docker Compose. Nada mais — o build do Java acontece dentro dos containers.

```bash
docker-compose up --build
```

Aguarde os healthchecks ficarem `healthy`. Containers em pé:

- `orbittapi-postgres` — porta 5432
- `orbittapi-redis` — porta 6379
- `orbittapi-identity` — porta 8081
- `orbittapi-satellite` — porta 8082
- `orbittapi-gateway` — porta 8080 (entrada única)

Para derrubar: `docker-compose down` (volumes persistem). Para limpar volumes: `docker-compose down -v`.

### Variável de ambiente importante
- `ORBITTAPI_JWT_SECRET` — string ≥ 32 bytes usada para assinar JWTs no identity-service e validar no gateway. O default do compose é o mesmo para os dois serviços.

---

## cURL — endpoints principais

> Todas as chamadas passam pela porta única **8080** (gateway).

### 1. Cadastrar conta (US-01)
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"dev@orbittapi.dev","password":"Abcdefg1"}'
```
Resposta `201`:
```json
{
  "accountId": "8b1a...uuid",
  "email": "dev@orbittapi.dev",
  "apiKey": "obt_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "expiresAt": "2026-05-27T00:00:00Z"
}
```

### 2. Login (US-02)
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"dev@orbittapi.dev","password":"Abcdefg1"}'
```

### 3. Perfil do usuário autenticado
```bash
curl http://localhost:8080/me \
  -H "Authorization: Bearer <TOKEN>"
```

### 4. Land use (US-05)
```bash
curl "http://localhost:8080/landuse?lat=-23.5&lng=-46.6" \
  -H "Authorization: Bearer <TOKEN>"
```
Resposta `200`:
```json
{
  "latitude": -23.5,
  "longitude": -46.6,
  "vegetationPercent": 52.31,
  "urbanPercent": 18.04,
  "waterPercent": 7.12,
  "bareSoilPercent": 22.53,
  "imageDate": "2026-05-14",
  "source": "MOCK",
  "cacheHit": false
}
```
Repita a mesma chamada e veja `"cacheHit": true` no log do satellite-service (US-21).

### 5. Vegetation / NDVI (US-06)
```bash
curl "http://localhost:8080/vegetation?lat=-23.5&lng=-46.6" \
  -H "Authorization: Bearer <TOKEN>"
```

### 6. Erro RFC 7807 (US-10)
Sem token → `401` no formato Problem Details:
```bash
curl -i "http://localhost:8080/landuse?lat=-23.5&lng=-46.6"
```
```json
{
  "type": "https://orbittapi.dev/errors/missing-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Missing or invalid Authorization header",
  "instance": "/landuse"
}
```

Coordenada inválida → `400`:
```bash
curl "http://localhost:8080/landuse?lat=91&lng=0" -H "Authorization: Bearer <TOKEN>"
```

### 7. Revogar API key (US-03, role ADMIN)
```bash
curl -X POST http://localhost:8080/api-keys/<accountId>/revoke \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

## Swagger UI
- Identity: http://localhost:8081/swagger-ui.html
- Satellite: http://localhost:8082/swagger-ui.html

(Quando rodando em compose, acessar via portas dos serviços diretamente.)

---

## Testes

Roda em todos os módulos:
```bash
mvn test
```

Inclui:
- **Unit tests dos agregados**: invariantes + emissão de eventos (`AccountTest`, `SatelliteQueryTest`)
- **Unit tests de value objects**: `EmailTest`, `PasswordTest`, `CoordinateTest`, `NdviScoreTest`, `LandUseDistributionTest`
- **Unit tests de use cases** (com mocks): `RegisterAccountUseCaseTest`, `GetLandUseUseCaseTest`
- **Determinismo do adapter Mock**: `MockSatelliteDataSourceTest`

---

## Cobertura do backlog

| US     | Atendida por                                                  |
|--------|---------------------------------------------------------------|
| US-01  | `POST /auth/register`                                         |
| US-02  | `POST /auth/login`                                            |
| US-03  | `POST /api-keys/{id}/revoke`                                  |
| US-05  | `GET /landuse?lat&lng`                                        |
| US-06  | `GET /vegetation?lat&lng`                                     |
| US-10  | `GlobalExceptionHandler` + RFC 7807                           |
| US-21  | Redis cache TTL 6h (`RedisSatelliteQueryCache`)               |
| US-22  | Hash BCrypt, sem PII em log                                   |

Outras histórias (front-end, billing, MFA, ingestão NASA real, ML) são extensíveis sem alterar os bounded contexts atuais — basta novos adapters/serviços.

---

## Diferenciais implementados
- Anti-corruption layer explícita (`SatelliteDataSource` porta + `MockSatelliteDataSource` adapter) — facilita trocar pelo NASA Earth API
- Cache Redis com TTL 6h e logs de hit/miss
- Validação centralizada por value object — coordenada inválida nunca chega no use case
- Spring Cloud Gateway com filtro JWT que valida 1 vez e injeta header pros serviços a jusante
- Swagger por serviço (cada bounded context publica sua própria documentação)
- Healthchecks via Actuator em todos os serviços
- Dockerfile multi-stage por serviço, build incremental

---

## Definition of Done — checklist

- [x] `docker-compose up` sobe os 5 contêineres sem erro
- [x] `POST /auth/register` cria conta + retorna JWT
- [x] `GET /landuse?lat=-23.5&lng=-46.6` com Bearer retorna 200 + JSON estruturado
- [x] `GET /landuse` sem token retorna 401 RFC 7807
- [x] Segunda chamada idêntica é servida do cache (log mostra `Cache HIT`)
- [x] Swagger UI em `/swagger-ui.html` por serviço
- [x] `mvn test` passa
- [x] `docs/architecture.md` mapeia DDD ↔ código
- [x] README completo

---

## Integrantes

- Giovanne Charelli Zaniboni Silva — RM 556223
- Leonardo Pasquini Baldaia — RM 557416
- Gustavo Oliveira de Moura — RM 555827
- Lynn Bueno Rosa — RM 551102
