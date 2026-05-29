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
                                                      +-------------------+
                                                      |   SOAP client     |
                                                      | (SoapUI, etc.)    |
                                                      +---------+---------+
                       +------------------+                     |
       REST client --->|     Gateway      |   8080              | SOAP
                       +--------+---------+                     |
                                |                               v
                +---------------+----------------+    +-------------------+
                |                                |    |   soap-service    | 8083
       +--------v---------+              +-------v----+--+ (profile soap) |
       | identity-service |  8081        | satellite-svc |                |
       |                  |              |     8082      |<--- REST ------+
       | Bounded context: |              |               | (consome /vegetation
       | Identity&Access  |              | BC: Satellite |  e /queries)
       +--------+---------+              +-------+-------+
                |                                |
                |    +----------+   +---------+  |
                +--->| Postgres |   |  Redis  |<-+
                     | 5432     |   | 6379    |
                     +----------+   +---------+
```

O SOAP é opcional (`--profile soap`) e não interfere com clientes REST (ex.: app mobile).
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
├── gateway/                      # Spring Cloud Gateway
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/br/com/orbittapi/gateway/
│       ├── GatewayApplication.java
│       ├── config/               # JwtProperties
│       └── filter/               # JwtAuthenticationGatewayFilter (valida + injeta X-User-Id)
│
└── soap-service/                 # Web Service SOAP contract-first (opcional, profile "soap")
    ├── pom.xml
    ├── Dockerfile
    └── src/main/
        ├── resources/satellite.xsd       # contrato (XSD)
        └── java/br/com/orbittapi/soap/
            ├── SoapApplication.java
            ├── config/                   # WebServiceConfig, RestClientConfig, SoapFaultConfig
            ├── endpoint/                 # SatelliteEndpoint (@Endpoint)
            ├── client/                   # SatelliteRestClient (consome satellite-service REST)
            └── exception/                # excecoes mapeadas para SOAP Fault
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

### 8. Atualizar meu e-mail (CRUD — PUT /me)
```bash
curl -X PUT http://localhost:8080/me \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"email":"novo@orbittapi.dev"}'
```
Retorna 200 com o `AccountProfileResponse` atualizado. E-mail duplicado → 409 RFC 7807.

### 9. Apagar conta (CRUD — DELETE /accounts/{id}, ADMIN)
```bash
curl -X DELETE http://localhost:8080/accounts/<accountId> \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```
Retorna 204. Sem ADMIN → 403 RFC 7807.

### 10. Registrar consulta (satellite — `POST /queries`)
Endpoint novo criado para suportar a integração SOAP↔REST (descrita abaixo); aceita também chamada REST direta:
```bash
curl -X POST http://localhost:8080/queries \
  -H "X-User-Id: <accountId>" \
  -H "Content-Type: application/json" \
  -d '{"type":"VEGETATION","latitude":-23.5,"longitude":-46.6}'
```

---

## Web Service SOAP (opcional — `--profile soap`)

Para atender ao requisito de **SOAP + WSDL + integração REST↔SOAP** da disciplina de SOA, há um módulo adicional `soap-service` que sobe na porta **8083** quando ativado por profile. Ele é **contract-first** (definido pelo XSD `soap-service/src/main/resources/satellite.xsd`) e o WSDL é publicado em tempo de execução.

### Subir o SOAP
```bash
docker compose --profile soap up --build
```
Sem o `--profile soap`, só os 5 contêineres originais sobem (sem regressão para o app mobile).

### WSDL
- URL: <http://localhost:8083/ws/satellite.wsdl>
- Namespace: `http://orbittapi.dev/soap/satellite`

### Operações expostas

| Operação | Descrição | Integração interna |
|---|---|---|
| `consultarVegetacao` | Recebe `latitude`/`longitude` e devolve NDVI + classificação | chama `GET /vegetation` do satellite-service |
| `registrarConsulta` | Recebe `accountId`/`tipo`/`latitude`/`longitude` e persiste um `SatelliteQuery`, retornando `queryId`+`status`+`executedAt` | chama `POST /queries` do satellite-service |

Esta é a **integração REST↔SOAP** exigida: o Web Service SOAP **não duplica** o domínio; ele consome a API REST do satellite-service via `RestClient`, passando o header `X-User-Id`.

### Envelope SOAP — request de `consultarVegetacao`
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:sat="http://orbittapi.dev/soap/satellite">
   <soapenv:Header/>
   <soapenv:Body>
      <sat:ConsultarVegetacaoRequest>
         <sat:latitude>-23.5</sat:latitude>
         <sat:longitude>-46.6</sat:longitude>
      </sat:ConsultarVegetacaoRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

### Envelope SOAP — response
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:ConsultarVegetacaoResponse xmlns:ns2="http://orbittapi.dev/soap/satellite">
         <ns2:latitude>-23.5</ns2:latitude>
         <ns2:longitude>-46.6</ns2:longitude>
         <ns2:ndvi>0.42</ns2:ndvi>
         <ns2:health>MODERATE</ns2:health>
         <ns2:imageDate>2026-05-10</ns2:imageDate>
         <ns2:source>MOCK</ns2:source>
      </ns2:ConsultarVegetacaoResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

### Envelope SOAP — `registrarConsulta`
Request:
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:sat="http://orbittapi.dev/soap/satellite">
   <soapenv:Body>
      <sat:RegistrarConsultaRequest>
         <sat:accountId>00000000-0000-0000-0000-000000000501</sat:accountId>
         <sat:tipo>VEGETATION</sat:tipo>
         <sat:latitude>-23.5</sat:latitude>
         <sat:longitude>-46.6</sat:longitude>
      </sat:RegistrarConsultaRequest>
   </soapenv:Body>
</soapenv:Envelope>
```
Response (resumido):
```xml
<ns2:RegistrarConsultaResponse xmlns:ns2="http://orbittapi.dev/soap/satellite">
   <ns2:queryId>11111111-2222-3333-4444-555555555555</ns2:queryId>
   <ns2:status>EXECUTED</ns2:status>
   <ns2:executedAt>2026-05-26T10:00:00Z</ns2:executedAt>
</ns2:RegistrarConsultaResponse>
```

### SOAP Fault (coordenada inválida)
Latitude fora de `[-90, 90]` no XSD ou recusada pelo satellite-service vira:
```xml
<SOAP-ENV:Fault>
   <faultcode>SOAP-ENV:Client</faultcode>
   <faultstring>Invalid request</faultstring>
</SOAP-ENV:Fault>
```
Mapeado por `SoapFaultMappingExceptionResolver` (`SoapFaultConfig.java`).

### Como testar no SoapUI

1. Abrir o SoapUI → **File → New SOAP Project**.
2. **Project Name**: `OrbittAPI SOAP`. **Initial WSDL**: `http://localhost:8083/ws/satellite.wsdl`. Marcar **Create sample requests for all operations**.
3. Em **SatellitePortBinding** vão aparecer 2 requests: `ConsultarVegetacaoRequest` e `RegistrarConsultaRequest`. Substituir `?` pelos valores do exemplo acima.
4. Clicar em ▶. A resposta XML aparece à direita.
5. Para testar Fault, mandar `latitude=95` → o servidor responde `SOAP-ENV:Fault` com `faultcode=Client`.

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
- **Unit tests de use cases** (com mocks): `RegisterAccountUseCaseTest`, `UpdateAccountEmailUseCaseTest`, `DeleteAccountUseCaseTest`, `GetLandUseUseCaseTest`
- **Determinismo do adapter Mock**: `MockSatelliteDataSourceTest`
- **Endpoint SOAP** com REST client mockado: `SatelliteEndpointTest` (consulta + cadastro + Fault)

Total: **39 testes** passando.

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
| CRUD   | `PUT /me` (update email), `DELETE /accounts/{id}` (ADMIN)     |
| SOA    | `soap-service` (porta 8083) com WSDL contract-first, integração REST↔SOAP |

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
