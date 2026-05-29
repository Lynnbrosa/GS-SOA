# Arquitetura — OrbittAPI Backend

## 1. Visão geral
OrbittAPI é uma plataforma SaaS de dados satelitais. Este repositório implementa o backend da Global Solution (SOA + DDD) com **2 microserviços de domínio + 1 API gateway + 1 SOAP gateway opcional**, escritos em Java 21 / Spring Boot 3.3, persistindo em PostgreSQL e usando Redis para cache.

```
                                                              +-------------+
                                                              | SOAP client |
                                                              | (SoapUI...) |
                                                              +------+------+
                       +------------------+                          |
       REST client --->|     Gateway      | porta 8080               | SOAP
                       |  (valida JWT,    |                          v
                       |   injeta         |                  +-----------------+
                       |   X-User-Id)     |                  |  soap-service   | 8083
                       +--------+---------+                  |  (profile soap, |
                                |                            |   contract-first)|
                +---------------+----------------+           +-------+---------+
                |                                |                   |
       +--------v---------+              +-------v----------+        | REST interna
       | identity-service |              | satellite-service|<-------+ (X-User-Id)
       |  (porta 8081)    |              |  (porta 8082)    |
       +--------+---------+              +-------+----------+
                |                                |
                |    +----------+   +---------+  |
                +--->| Postgres |   |  Redis  |<-+
                     | 5432     |   | 6379    |
                     +----------+   +---------+
                  identity_db /    cache landuse:/
                  satellite_db     vegetation: TTL 6h
```

> O `soap-service` é opcional. `docker compose up` sobe apenas os 5 contêineres
> originais; `docker compose --profile soap up` adiciona o SOAP. Isso garante
> compatibilidade total com clientes REST existentes (ex.: app mobile).

## 2. Bounded contexts (DDD)

### 2.1 Identity & Access (`identity-service`)
Responsável por cadastro, login, perfil e gestão de API keys.

- **Agregado raiz:** `Account`
- **Value objects:** `Email`, `Password`, `ApiKey`
- **Eventos de domínio:** `AccountRegistered`, `ApiKeyRevoked`
- **Invariantes:**
  - E-mail único (validado no `RegisterAccountUseCase` via `AccountRepository.existsByEmail`)
  - Senha forte: ≥ 8 chars, ≥ 1 dígito, ≥ 1 maiúscula (validado em `Password.fromRaw`)
  - Senha sempre persistida como hash BCrypt — nunca em texto plano
  - API key gerada com 24 bytes de entropia (`SecureRandom`) e prefixo `obt_`
  - Não é possível revogar uma API key já revogada (lança `ApiKeyAlreadyRevokedException`)

### 2.2 Satellite Data (`satellite-service`)
Responsável por servir métricas de satélite (uso do solo, NDVI).

- **Agregado raiz:** `SatelliteQuery` (auditoria de cada consulta)
- **Value objects:** `Coordinate`, `NdviScore`, `LandUseDistribution`
- **Eventos de domínio:** `QueryExecuted` (usado para auditoria e billing futuro)
- **Porta (ACL):** `SatelliteDataSource` no `domain/port/`. Adapter `MockSatelliteDataSource` em `infrastructure/adapter/` retorna dados determinísticos por coordenada — facilita testes e demos. Trocar pelo adapter real (NASA/ESA) não exige alteração no domínio.
- **Cache:** `SatelliteQueryCache` (porta) + `RedisSatelliteQueryCache` (adapter). Chaves `landuse:{lat}:{lng}` e `vegetation:{lat}:{lng}`, TTL 6h (US-21 do backlog).
- **Invariantes:**
  - `Coordinate`: lat ∈ [-90, 90], lng ∈ [-180, 180]
  - `NdviScore`: valor ∈ [-1, 1]
  - `LandUseDistribution`: percentuais ∈ [0, 100] e somam 100 (± 0.05)

### 2.3 Gateway (`gateway`)
Spring Cloud Gateway. Roteamento e validação JWT centralizada. Rotas em `gateway/src/main/resources/application.yml`. Filtro `JwtAuthenticationGatewayFilter` extrai `userId` do token e injeta `X-User-Id`/`X-User-Role` antes de propagar pro serviço destino — assim o satellite-service não precisa revalidar o JWT.

### 2.4 SOAP gateway (`soap-service`, opcional)
Atende ao requisito da disciplina de SOA. Sobe na porta 8083 quando ativado por `--profile soap`. É **contract-first**:

- O contrato é o XSD `soap-service/src/main/resources/satellite.xsd`.
- O plugin `jaxb2-maven-plugin` gera as classes Java em `br.com.orbittapi.soap.generated` na fase `generate-sources` do Maven.
- O `MessageDispatcherServlet` é registrado em `/ws/*` e o WSDL é publicado em `/ws/satellite.wsdl` por um `DefaultWsdl11Definition`.
- O `@Endpoint` `SatelliteEndpoint` expõe duas operações (`consultarVegetacao` e `registrarConsulta`).
- O `SoapFaultMappingExceptionResolver` (em `SoapFaultConfig`) traduz `InvalidCoordinateSoapException` em `Client` Fault e `SatelliteUnavailableSoapException` em `Server` Fault.

#### Integração REST ↔ SOAP

O `SatelliteEndpoint` **não reimplementa** o domínio. Ele delega para o `SatelliteRestClient` (um `RestClient` do Spring 6) que chama o satellite-service via HTTP, passando o header `X-User-Id`:

```
SOAP Body recebido        ->  @Endpoint SatelliteEndpoint
                          ->  SatelliteRestClient
                              GET  http://satellite-service:8082/vegetation?lat&lng
                              POST http://satellite-service:8082/queries  (header X-User-Id)
                          <-  resposta JSON convertida em ConsultarVegetacaoResponse
                              / RegistrarConsultaResponse (classes JAXB)
SOAP Body devolvido
```

Esse é o padrão exigido pela disciplina: **Web Service SOAP consumindo API REST**. O domínio fica em um único lugar (satellite-service) e a camada SOAP é apenas um *transport adapter*.

## 3. Mapeamento DDD ↔ código

| Conceito DDD            | Onde aparece                                                                                          |
|-------------------------|-------------------------------------------------------------------------------------------------------|
| **Entidade**            | `Account` (`identity/domain/model/Account.java`), `SatelliteQuery` (`satellite/domain/model/SatelliteQuery.java`) — possuem id, igualdade por id |
| **Value Object**        | `Email`, `Password`, `ApiKey`, `Coordinate`, `NdviScore`, `LandUseDistribution` — imutáveis, equals por valor, validação no construtor |
| **Agregado**            | `Account` protege invariantes de senha/email/API key; `SatelliteQuery` é raiz pequena de auditoria   |
| **Domain Event**        | `AccountRegistered`, `ApiKeyRevoked`, `AccountEmailChanged`, `AccountDeleted`, `QueryExecuted` (`*/domain/event/`) — publicados via `ApplicationEventPublisher` no `SpringDomainEventPublisher` |
| **Repository (port)**   | `AccountRepository`, `SatelliteQueryRepository` — interfaces em `*/domain/repository/`                |
| **Repository (impl)**   | `AccountRepositoryImpl`, `SatelliteQueryRepositoryImpl` em `*/infrastructure/persistence/` (JPA)      |
| **Application Service** | `RegisterAccountUseCase`, `LoginUseCase`, `GetMyProfileUseCase`, `UpdateAccountEmailUseCase`, `RevokeApiKeyUseCase`, `DeleteAccountUseCase`, `GetLandUseUseCase`, `GetVegetationUseCase`, `RegisterQueryUseCase` |
| **Anti-Corruption Layer** | `SatelliteDataSource` (porta) + `MockSatelliteDataSource` (adapter) — futuro adapter NASA não vaza modelo externo pro domínio |
| **Ubiquitous Language** | `Account`, `ApiKey`, `SatelliteQuery`, `LandUseDistribution`, `NdviScore`, `VegetationHealth`. Sem `Manager`, `Helper`, `Data`, `Info` |

## 4. Camadas em cada serviço (DDD clássica)

```
br.com.orbittapi.<servico>/
├── domain/              <- modelo puro, sem Spring nem JPA
│   ├── model/           <- entidades + value objects
│   ├── event/           <- domain events
│   ├── repository/      <- interfaces (portas de persistencia)
│   ├── port/            <- outras portas (ex: SatelliteDataSource)
│   └── exception/       <- DomainException + filhas
├── application/         <- orquestração de use cases
│   ├── usecase/         <- @Service, casos de uso
│   ├── dto/             <- comandos de entrada e respostas
│   └── port/            <- portas usadas pelos use cases (TokenProvider, DomainEventPublisher)
├── infrastructure/      <- adapters
│   ├── persistence/     <- JPA entities + mapper + impl do repository
│   ├── adapter/         <- adapters de portas externas (Mock satellite)
│   ├── cache/           <- Redis adapter
│   ├── security/        <- JWT provider, filtros
│   └── config/          <- @Configuration, OpenAPI
└── interfaces/rest/     <- controllers + GlobalExceptionHandler (RFC 7807)
```

## 5. Fluxo de uma requisição autenticada

```
1. POST /auth/register      -> gateway propaga -> identity-service
   - RegisterAccountUseCase
     - Email VO (validacao)
     - Account.register() (gera ApiKey + emite AccountRegistered)
     - AccountRepositoryImpl.save() (JPA)
     - SpringDomainEventPublisher.publishAll()
     - JwtTokenProvider.issue() retorna token
   - Resposta 201: { accountId, email, apiKey, token, expiresAt }

2. GET /landuse?lat=-23.5&lng=-46.6  com  Authorization: Bearer <jwt>
   - Gateway: JwtAuthenticationGatewayFilter valida assinatura + issuer
   - Gateway injeta X-User-Id no request
   - satellite-service.LandUseController recebe accountId via @RequestHeader
   - GetLandUseUseCase:
     a) Coordinate VO (validacao de range)
     b) RedisSatelliteQueryCache.getLandUse() -> hit/miss
     c) Se miss: MockSatelliteDataSource.fetchLandUse() + cache.put
     d) SatelliteQuery.execute() (emite QueryExecuted)
     e) save + publishAll
   - Resposta 200: { lat, lng, vegetation%, urban%, water%, bareSoil%, imageDate, source, cacheHit }
```

## 6. Erros — RFC 7807
Todo erro retorna `application/problem+json` no formato:
```json
{
  "type": "https://orbittapi.dev/errors/invalid-coordinate",
  "title": "Invalid coordinate",
  "status": 400,
  "detail": "Latitude must be between -90 and 90, got 91.0",
  "instance": "/landuse"
}
```

Mapeamento HTTP ↔ exceção de domínio:

| Exceção                              | HTTP | Tipo                                  |
|--------------------------------------|------|---------------------------------------|
| `InvalidEmailException`              | 400  | `invalid-email`                       |
| `WeakPasswordException`              | 400  | `weak-password`                       |
| `InvalidCoordinateException`         | 400  | `invalid-coordinate`                  |
| `InvalidNdviScoreException`          | 400  | `invalid-ndvi`                        |
| `InvalidLandUseDistributionException`| 400  | `invalid-land-use`                    |
| Token ausente/expirado/inválido      | 401  | `unauthorized`/`invalid-token`        |
| `InvalidCredentialsException`        | 401  | `invalid-credentials`                 |
| Falta role para revogar API key      | 403  | `forbidden`                           |
| `AccountNotFoundException`           | 404  | `account-not-found`                   |
| `EmailAlreadyInUseException`         | 409  | `email-in-use`                        |
| `ApiKeyAlreadyRevokedException`      | 409  | `api-key-already-revoked`             |
| `SatelliteDataUnavailableException`  | 503  | `satellite-data-unavailable`          |
| Exceção inesperada                   | 500  | `internal`                            |

## 7. Cobertura do backlog
Histórias atendidas no escopo de backend SOA + DDD:

| US     | Atendida por                                            |
|--------|---------------------------------------------------------|
| US-01  | `POST /auth/register` (identity)                        |
| US-02  | `POST /auth/login` (identity)                           |
| US-03  | `POST /api-keys/{id}/revoke` (identity, role ADMIN)     |
| US-05  | `GET /landuse?lat&lng` (satellite)                      |
| US-06  | `GET /vegetation?lat&lng` (satellite)                   |
| US-10  | `GlobalExceptionHandler` + `ProblemDetail` (ambos)      |
| US-21  | `RedisSatelliteQueryCache` com TTL 6h                   |
| US-22  | Senha como hash BCrypt, sem PII em logs                 |

Histórias fora do escopo desta GS (front-end, billing, pipelines de ingestão, ML, MFA, white-label) não foram implementadas — a arquitetura permite acrescentá-las como novos bounded contexts sem alterar os atuais.
