"""
Gera a documentacao em PDF da Global Solution OrbittAPI.
Tudo embutido (nenhum link externo) conforme exigencia do professor.
"""
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.lib.enums import TA_JUSTIFY, TA_CENTER, TA_LEFT
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle,
    Preformatted, KeepTogether
)
from reportlab.pdfgen import canvas
import os

OUT = os.path.join(os.path.dirname(__file__), "Documentacao_GS_OrbittAPI.pdf")

# -------------------- estilos --------------------
styles = getSampleStyleSheet()

title_style = ParagraphStyle(
    'TitleBig', parent=styles['Title'], fontSize=26, leading=32,
    spaceAfter=12, alignment=TA_CENTER, textColor=colors.HexColor("#0b3d91"),
)
subtitle_style = ParagraphStyle(
    'Subtitle', parent=styles['Normal'], fontSize=14, leading=18,
    alignment=TA_CENTER, textColor=colors.HexColor("#444444"), spaceAfter=24,
)
h1 = ParagraphStyle('H1', parent=styles['Heading1'], fontSize=18, leading=22,
                    spaceBefore=18, spaceAfter=10,
                    textColor=colors.HexColor("#0b3d91"))
h2 = ParagraphStyle('H2', parent=styles['Heading2'], fontSize=14, leading=18,
                    spaceBefore=12, spaceAfter=6,
                    textColor=colors.HexColor("#0b3d91"))
h3 = ParagraphStyle('H3', parent=styles['Heading3'], fontSize=12, leading=16,
                    spaceBefore=8, spaceAfter=4,
                    textColor=colors.HexColor("#222222"))
body = ParagraphStyle('Body', parent=styles['BodyText'], fontSize=11, leading=15,
                      alignment=TA_JUSTIFY, spaceAfter=6)
bullet = ParagraphStyle('Bullet', parent=body, leftIndent=14, bulletIndent=2,
                        spaceAfter=2)
code_style = ParagraphStyle(
    'Code', parent=styles['Code'], fontSize=8.5, leading=11,
    backColor=colors.HexColor("#f4f6fa"),
    borderColor=colors.HexColor("#dde2ec"), borderWidth=0.5, borderPadding=6,
    leftIndent=4, rightIndent=4, spaceBefore=4, spaceAfter=8,
    textColor=colors.HexColor("#1c1c1c"),
)
ascii_style = ParagraphStyle(
    'Ascii', parent=code_style, alignment=TA_LEFT,
)
caption = ParagraphStyle('Caption', parent=body, fontSize=9, leading=12,
                         textColor=colors.HexColor("#555555"), alignment=TA_CENTER,
                         spaceAfter=10)

# -------------------- helpers --------------------
def p(text, style=body):
    return Paragraph(text, style)

def code(text):
    return Preformatted(text, code_style)

def ascii_block(text):
    return Preformatted(text, ascii_style)

def li(text):
    return Paragraph(f"&bull;&nbsp;&nbsp;{text}", bullet)

def make_table(data, col_widths=None, header_bg="#0b3d91", header_fg="#ffffff"):
    t = Table(data, colWidths=col_widths, repeatRows=1)
    t.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor(header_bg)),
        ('TEXTCOLOR', (0,0), (-1,0), colors.HexColor(header_fg)),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('FONTSIZE', (0,0), (-1,-1), 9),
        ('LEADING', (0,0), (-1,-1), 11),
        ('GRID', (0,0), (-1,-1), 0.4, colors.HexColor("#cfd6e2")),
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
        ('TOPPADDING', (0,0), (-1,-1), 5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 5),
        ('ROWBACKGROUNDS', (0,1), (-1,-1),
         [colors.white, colors.HexColor("#f4f6fa")]),
    ]))
    return t

# -------------------- footer/header com numero de pagina --------------------
def on_page(canv: canvas.Canvas, doc):
    canv.saveState()
    canv.setFont('Helvetica', 8)
    canv.setFillColor(colors.HexColor("#666666"))
    canv.drawString(2*cm, 1.2*cm, "OrbittAPI - Global Solution 2026.1 - SOA + DDD")
    canv.drawRightString(A4[0] - 2*cm, 1.2*cm, f"Pag. {doc.page}")
    canv.setStrokeColor(colors.HexColor("#cfd6e2"))
    canv.setLineWidth(0.4)
    canv.line(2*cm, 1.5*cm, A4[0] - 2*cm, 1.5*cm)
    canv.restoreState()

# -------------------- conteudo --------------------
story = []

# ===== CAPA =====
story.append(Spacer(1, 4*cm))
story.append(p("OrbittAPI", title_style))
story.append(p("Plataforma SaaS de dados satelitais", subtitle_style))
story.append(Spacer(1, 1*cm))
story.append(p("Documentacao da Global Solution 2026.1", h2))
story.append(p("Disciplinas combinadas: Service-Oriented Architecture (SOA) e Domain-Driven Design (DDD)", body))
story.append(Spacer(1, 0.6*cm))
story.append(p("Tema do semestre: Industria Espacial", body))
story.append(p("ODS alinhados: 13 (Acao contra a mudanca global do clima) e 2 (Fome zero e agricultura sustentavel)", body))
story.append(Spacer(1, 2*cm))

story.append(p("<b>Integrantes</b>", h3))
integrantes_data = [
    ["Nome", "RM"],
    ["Giovanne Charelli Zaniboni Silva", "556223"],
    ["Leonardo Pasquini Baldaia", "557416"],
    ["Gustavo Oliveira de Moura", "555827"],
    ["Lynn Bueno Rosa", "551102"],
]
story.append(make_table(integrantes_data, col_widths=[10*cm, 4*cm]))
story.append(Spacer(1, 1*cm))
story.append(p("FIAP - 1S/2026", caption))

story.append(PageBreak())

# ===== SUMARIO =====
story.append(p("Sumario", h1))
sumario_items = [
    "1. Visao geral do projeto",
    "2. Conexao com o tema Industria Espacial e ODS",
    "3. Arquitetura SOA: dois microservicos e um gateway",
    "4. Visao geral de Domain-Driven Design",
    "5. Strategic Design",
    "    5.1 Ubiquitous Language (linguagem ubiqua)",
    "    5.2 Bounded Context (contexto delimitado)",
    "    5.3 Subdomains: Core, Supporting e Generic",
    "    5.4 Context Map e padroes de relacionamento",
    "6. Tactical Design (Building Blocks)",
    "    6.1 Entity",
    "    6.2 Value Object",
    "    6.3 Aggregate e Aggregate Root",
    "    6.4 Domain Event",
    "    6.5 Domain Service",
    "    6.6 Application Service / Use Case",
    "    6.7 Repository",
    "    6.8 Factory",
    "    6.9 Module",
    "7. Arquitetura em camadas e Ports and Adapters (Hexagonal)",
    "8. Anti-Corruption Layer aplicada",
    "9. Erros padronizados via RFC 7807",
    "10. Cache estrategico no Redis",
    "11. Seguranca, JWT e fluxo de autenticacao",
    "12. Mapeamento mestre: conceito DDD para arquivo no codigo",
    "13. Cobertura do backlog (User Stories)",
    "14. Como subir o ambiente (Docker)",
    "15. Endpoints REST e exemplos cURL",
    "16. Testes automatizados",
    "17. Diferenciais implementados",
    "18. Definition of Done",
    "19. Conclusao",
]
for s in sumario_items:
    story.append(p(s, body))
story.append(PageBreak())

# ===== 1. VISAO GERAL =====
story.append(p("1. Visao geral do projeto", h1))
story.append(p(
    "OrbittAPI e uma plataforma <b>Software-as-a-Service</b> de acesso a dados satelitais. "
    "Empresas de qualquer setor (agronegocio, seguradoras, construtoras, consultorias ambientais) "
    "podem consumir inteligencia espacial atraves de endpoints REST simples, sem precisar de "
    "cientistas de dados ou infraestrutura propria.", body))
story.append(p(
    "A plataforma agrega dados de fontes como NASA, ESA e INPE, processa imagens de satelite e entrega "
    "metricas prontas para uso via API: indice de vegetacao (NDVI), uso do solo, risco de alagamento, "
    "deteccao de desmatamento e expansao urbana. O modelo de negocio e baseado em assinatura mensal "
    "(Free, Startup, Business e Enterprise) com cobranca por volume de chamadas de API.", body))
story.append(p(
    "Este repositorio implementa o <b>backend</b> da Global Solution, atendendo simultaneamente os requisitos das "
    "disciplinas de SOA (Service-Oriented Architecture) e DDD (Domain-Driven Design). A solucao foi "
    "construida como um monorepo Maven com dois microservicos de dominio mais um gateway, todos em "
    "Java 21 e Spring Boot 3.3, prontos para subir em containers via Docker Compose.", body))

story.append(p("Stack tecnica", h3))
story.append(li("Java 21 com record types, sealed interfaces e pattern matching"))
story.append(li("Spring Boot 3.3.5 (Web, Data JPA, Security, Validation, Actuator)"))
story.append(li("Spring Cloud Gateway para roteamento e validacao centralizada de JWT"))
story.append(li("PostgreSQL 16 (um schema por bounded context: identity_db e satellite_db)"))
story.append(li("Redis 7 para cache de consultas satelitais com TTL de 6 horas"))
story.append(li("JWT via biblioteca jjwt 0.12 com assinatura HS384"))
story.append(li("BCrypt para hash de senhas (cost factor 12)"))
story.append(li("Springdoc OpenAPI para Swagger UI por servico"))
story.append(li("JUnit 5, Mockito e AssertJ para testes"))
story.append(li("Maven multi-modulo com pom parent e tres modulos filhos"))
story.append(li("Docker e Docker Compose para empacotamento e orquestracao local"))

story.append(PageBreak())

# ===== 2. CONEXAO COM O TEMA =====
story.append(p("2. Conexao com o tema Industria Espacial e ODS", h1))
story.append(p(
    "A Industria Espacial vive uma transformacao em escala: o que antes era prerrogativa de agencias "
    "governamentais hoje e operado por uma cadeia de empresas privadas (SpaceX, Rocket Lab, Planet Labs, "
    "Capella Space, entre outras). Essa democratizacao gerou uma <b>economia espacial</b> em torno do uso "
    "civil de dados orbitais.", body))
story.append(p(
    "OrbittAPI participa dessa economia oferecendo a camada de software que conecta a saida bruta de "
    "satelites (TIFFs multispectrais, GeoJSON de telemetria, indices brutos) com aplicacoes finais que "
    "consomem JSON estruturado. Em vez de exigir que um cliente da agricultura entenda Sentinel-2 ou "
    "Landsat 9, ele faz uma chamada GET /vegetation?lat=X&lng=Y e recebe um NDVI ja calculado e "
    "classificado por nivel de vegetacao.", body))

story.append(p("ODS principal: 13 - Acao contra a mudanca global do clima", h3))
story.append(p(
    "Os indices de vegetacao e os mapas de uso do solo permitem monitorar perda de cobertura vegetal, "
    "expansao de areas urbanas sobre matas e impacto de eventos climaticos extremos (alagamentos, "
    "secas, queimadas). Sao insumos diretos para acoes de mitigacao climatica.", body))

story.append(p("ODS secundario: 2 - Fome zero e agricultura sustentavel", h3))
story.append(p(
    "O NDVI (Normalized Difference Vegetation Index) e um dos principais indicadores para agricultura de "
    "precisao. Variacoes mensais e sazonais no NDVI ajudam produtores a tomar decisoes sobre irrigacao, "
    "plantio, colheita e identificacao de pragas. Disponibilizar isso via API barateia o acesso a "
    "tecnologia para pequenos produtores.", body))

# ===== 3. ARQUITETURA SOA =====
story.append(p("3. Arquitetura SOA: dois microservicos e um gateway", h1))
story.append(p(
    "O backend foi decomposto em tres servicos independentes, comunicando-se por HTTP. Cada servico tem "
    "seu proprio Dockerfile, seu proprio schema de banco e pode ser desenvolvido, testado e deployado "
    "separadamente. Esta e a definicao classica de uma arquitetura orientada a servicos.", body))

arq = """\
                       +---------------------+
       Cliente HTTP -->|       Gateway       | porta 8080
                       |  Spring Cloud GW    | valida JWT
                       |  + filtro JWT       | injeta X-User-Id
                       +----------+----------+
                                  |
                +-----------------+------------------+
                |                                    |
       +--------v----------+               +---------v---------+
       | identity-service  |               | satellite-service |
       |    porta 8081     |               |    porta 8082     |
       | BC: Identity &    |               | BC: Satellite     |
       |     Access        |               |     Data          |
       +--------+----------+               +---------+---------+
                |                                    |
                |    +-------------+   +----------+  |
                +--->| PostgreSQL  |   |  Redis 7 |<-+
                     |   5432      |   |   6379   |
                     +-------------+   +----------+
                identity_db /          cache:
                satellite_db           landuse:{lat}:{lng}
                                       vegetation:{lat}:{lng}
                                       TTL 6 horas
"""
story.append(ascii_block(arq))
story.append(p("Figura 1: Topologia dos servicos e suas dependencias.", caption))

story.append(p("Responsabilidades de cada servico", h3))
story.append(li("<b>identity-service</b>: bounded context Identity & Access. Cadastro, login, perfil, "
                "geracao/revogacao de API key. Persiste em identity_db."))
story.append(li("<b>satellite-service</b>: bounded context Satellite Data. Endpoints /landuse e /vegetation. "
                "Persiste auditoria em satellite_db e cacheia respostas no Redis."))
story.append(li("<b>gateway</b>: ponto de entrada unico (porta 8080). Roteia HTTP para os servicos, "
                "valida JWT em rotas privadas e injeta X-User-Id no request antes de propagar, "
                "evitando que cada servico precise revalidar o token."))

story.append(p("Por que SOA aqui?", h3))
story.append(p(
    "Separar Identity de Satellite Data e mais do que decoracao arquitetural: sao dominios com ritmos de "
    "evolucao diferentes (autenticacao e quase estavel, dados satelitais evoluem com novas fontes e novos "
    "indices), com cargas diferentes (autenticacao tem picos rapidos, consulta satelital tende a ter "
    "consultas mais pesadas), e com times potencialmente diferentes. Manter os dois servicos isolados "
    "permite escala-los e evolui-los de forma independente.", body))

story.append(PageBreak())

# ===== 4. VISAO GERAL DDD =====
story.append(p("4. Visao geral de Domain-Driven Design", h1))
story.append(p(
    "Domain-Driven Design (DDD) e uma abordagem para o desenvolvimento de software complexo proposta por "
    "<b>Eric Evans</b> no livro Domain-Driven Design: Tackling Complexity in the Heart of Software (2003) "
    "e expandida por <b>Vaughn Vernon</b> em Implementing Domain-Driven Design (2013).", body))
story.append(p(
    "A premissa central e: o software resolve problemas de negocio, e portanto o codigo deve refletir o "
    "<b>modelo do dominio</b> com a maior fidelidade possivel. DDD propoe um conjunto de praticas para "
    "alinhar o modelo mental dos especialistas do negocio com o codigo, dividindo a abordagem em "
    "<b>Strategic Design</b> (decisoes de larga escala: como dividir o sistema) e <b>Tactical Design</b> "
    "(blocos de construcao do codigo: entidades, value objects, agregados, eventos).", body))
story.append(p(
    "Este documento percorre cada conceito apresentado em sala e mostra explicitamente como ele aparece "
    "no codigo do OrbittAPI.", body))

# ===== 5. STRATEGIC DESIGN =====
story.append(p("5. Strategic Design", h1))

# 5.1 Ubiquitous Language
story.append(p("5.1 Ubiquitous Language (linguagem ubiqua)", h2))
story.append(p(
    "A linguagem ubiqua e um vocabulario compartilhado entre desenvolvedores, especialistas de dominio e "
    "stakeholders. Todo conceito do negocio deve aparecer no codigo com o mesmo nome usado pelo dominio. "
    "Termos genericos como Manager, Helper, Data, Info, Util sao banidos quando ha um termo de negocio "
    "disponivel.", body))
story.append(p("Aplicacao no OrbittAPI:", h3))
language_table = [
    ["Termo do dominio", "Onde aparece no codigo"],
    ["Account", "Account.java (agregado raiz do identity-service)"],
    ["ApiKey", "ApiKey.java (value object)"],
    ["Email", "Email.java (value object com validacao)"],
    ["Password", "Password.java (value object que faz hash no construtor)"],
    ["SatelliteQuery", "SatelliteQuery.java (agregado de auditoria)"],
    ["Coordinate", "Coordinate.java (value object lat/lng com invariantes)"],
    ["NdviScore", "NdviScore.java (value object com classificacao VegetationHealth)"],
    ["LandUseDistribution", "LandUseDistribution.java (vegetacao, urbano, agua, solo exposto)"],
    ["VegetationHealth", "Enum NONE, SPARSE, MODERATE, DENSE dentro de NdviScore"],
    ["SatelliteSource", "Enum LANDSAT, SENTINEL, MODIS, MOCK"],
]
story.append(make_table(language_table, col_widths=[6*cm, 11*cm]))

# 5.2 Bounded Context
story.append(p("5.2 Bounded Context (contexto delimitado)", h2))
story.append(p(
    "Um Bounded Context e uma fronteira explicita dentro da qual um modelo de dominio e consistente. "
    "Dois bounded contexts podem ter o mesmo nome para conceitos diferentes (por exemplo, Customer no "
    "contexto de Vendas e diferente de Customer no contexto de Cobranca) sem causar ambiguidade, porque "
    "cada um vive em sua propria fronteira.", body))
story.append(p("O OrbittAPI tem dois bounded contexts explicitos, refletidos um por microservico:", body))

bc_table = [
    ["Bounded Context", "Microservico", "Linguagem propria"],
    ["Identity & Access", "identity-service", "Account, ApiKey, Password, Role, Credentials"],
    ["Satellite Data", "satellite-service", "SatelliteQuery, Coordinate, NDVI, LandUse, ImageDate, Source"],
]
story.append(make_table(bc_table, col_widths=[4.5*cm, 4.5*cm, 8*cm]))
story.append(p(
    "Nao ha vazamento entre contextos: o satellite-service nao conhece a classe Account; ele recebe um "
    "UUID accountId atraves do header X-User-Id injetado pelo gateway. Isso e proposital: cada contexto "
    "pode evoluir sem coordenar mudancas com o outro.", body))

# 5.3 Subdomains
story.append(p("5.3 Subdomains: Core, Supporting e Generic", h2))
story.append(p(
    "Subdomains classificam partes do negocio de acordo com a sua centralidade estrategica:", body))
story.append(li("<b>Core Domain</b>: o diferencial competitivo da empresa. Merece o melhor time e o "
                "modelo mais cuidadoso."))
story.append(li("<b>Supporting Subdomain</b>: importante mas nao diferencial. Pode ser construido "
                "internamente sem virar o foco principal."))
story.append(li("<b>Generic Subdomain</b>: problema resolvido por solucoes de mercado (compra, nao "
                "constroi)."))

sub_table = [
    ["Subdomain", "Tipo", "Justificativa"],
    ["Satellite Data", "Core",
     "E o diferencial competitivo da OrbittAPI. Transformar imagens cruas em metricas REST "
     "consumiveis e o que a empresa vende."],
    ["Identity & Access", "Supporting",
     "Importante mas nao diferencial. Hoje e construido internamente, mas em uma evolucao "
     "poderia ser substituido por um provedor externo (Auth0, Keycloak)."],
    ["Billing", "Generic",
     "Cobranca por consumo de API e resolvida por solucoes de mercado (Stripe, Iugu). Fora "
     "do escopo desta GS."],
]
story.append(make_table(sub_table, col_widths=[3.5*cm, 2.5*cm, 11*cm]))

story.append(PageBreak())

# 5.4 Context Map
story.append(p("5.4 Context Map e padroes de relacionamento", h2))
story.append(p(
    "Quando dois ou mais bounded contexts precisam interagir, e necessario definir explicitamente como. "
    "Os principais padroes catalogados por Eric Evans sao:", body))

cmap_table = [
    ["Padrao", "Descricao curta"],
    ["Shared Kernel",
     "Dois contextos compartilham uma porcao pequena de modelo. Mudancas exigem acordo entre os times."],
    ["Customer-Supplier",
     "Um contexto consome o outro. O supplier tem responsabilidade de manter compatibilidade."],
    ["Conformist",
     "O downstream se rende ao modelo do upstream sem traduzir, aceitando-o como vem."],
    ["Anti-Corruption Layer (ACL)",
     "O downstream coloca uma camada de traducao que isola seu modelo do modelo externo."],
    ["Open Host Service",
     "O contexto publica uma API estavel que multiplos consumidores podem usar."],
    ["Published Language",
     "Acordo sobre um formato comum (JSON Schema, Avro) usado entre os contextos."],
    ["Separate Ways",
     "Decisao explicita de nao integrar. Cada contexto resolve o problema sozinho."],
    ["Partnership",
     "Dois times se coordenam ativamente para que ambos os contextos avancem juntos."],
]
story.append(make_table(cmap_table, col_widths=[5*cm, 12*cm]))

story.append(p("Relacoes presentes no OrbittAPI:", h3))
story.append(li("<b>Identity & Access (upstream) -> Satellite Data (downstream)</b> via "
                "Customer-Supplier + Published Language. O identity emite JWTs com sub=accountId no "
                "formato JSON Web Token (linguagem publicada). O satellite consome accountId."))
story.append(li("<b>Satellite Data -> Fonte externa de dados (NASA/ESA/Mock)</b> via "
                "<b>Anti-Corruption Layer</b>. A porta de dominio SatelliteDataSource isola completamente "
                "o modelo do dominio do formato externo. Hoje o adapter e MockSatelliteDataSource; "
                "amanha pode ser NasaEarthApiAdapter sem nenhuma mudanca no dominio."))

# ===== 6. TACTICAL DESIGN =====
story.append(p("6. Tactical Design (Building Blocks)", h1))

# 6.1 Entity
story.append(p("6.1 Entity", h2))
story.append(p(
    "Uma <b>Entidade</b> e um objeto que possui identidade. Dois objetos com os mesmos atributos mas "
    "ids diferentes sao considerados <b>diferentes</b>. A identidade persiste ao longo do tempo, mesmo "
    "quando os atributos mudam.", body))
story.append(p("No OrbittAPI temos duas entidades raizes:", body))
story.append(li("<b>Account</b> (identity-service): identidade UUID; mesmo que email e role mudem, e a mesma conta."))
story.append(li("<b>SatelliteQuery</b> (satellite-service): identidade UUID atribuida no momento da execucao."))

story.append(p("Trecho: igualdade por identidade no Account", h3))
story.append(code("""@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Account account)) return false;
    return Objects.equals(id, account.id);   // <- so o id importa
}

@Override
public int hashCode() {
    return Objects.hash(id);
}"""))

# 6.2 Value Object
story.append(p("6.2 Value Object", h2))
story.append(p(
    "Um <b>Value Object</b> e definido apenas pelos seus atributos. Nao tem identidade propria. Dois "
    "VOs com o mesmo valor sao iguais. Sao <b>imutaveis</b>: qualquer operacao retorna uma nova instancia. "
    "Encapsulam regras de validacao no construtor: se voce conseguiu construir o objeto, ele e valido. "
    "Isso elimina classes inteiras de bugs (coordenada invalida nunca chega no use case).", body))

story.append(p("Exemplo 1: Coordinate", h3))
story.append(code("""public final class Coordinate {

    private final double latitude;
    private final double longitude;

    public Coordinate(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new InvalidCoordinateException(
                "Latitude must be between -90 and 90, got " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new InvalidCoordinateException(
                "Longitude must be between -180 and 180, got " + longitude);
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }
    // equals/hashCode por valor
}"""))

story.append(p("Exemplo 2: Email com normalizacao no construtor", h3))
story.append(code("""public final class Email {
    private static final Pattern EMAIL_REGEX =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$");

    private final String value;

    public Email(String value) {
        if (value == null || value.isBlank())
            throw new InvalidEmailException("Email must not be blank");
        String normalized = value.trim().toLowerCase();
        if (!EMAIL_REGEX.matcher(normalized).matches())
            throw new InvalidEmailException("Email format is invalid: " + value);
        this.value = normalized;
    }
    public String value() { return value; }
}"""))

story.append(p("Exemplo 3: Password com hash BCrypt no construtor (factory)", h3))
story.append(code("""public final class Password {
    private static final int MIN_LENGTH = 8;
    private final String hash;

    private Password(String hash) { this.hash = hash; }

    public static Password fromRaw(String raw) {
        validateStrength(raw);                              // >= 8, 1 numero, 1 maiuscula
        return new Password(BCrypt.hashpw(raw, BCrypt.gensalt(12)));
    }
    public static Password fromHash(String hash) { return new Password(hash); }

    public boolean matches(String raw) { return BCrypt.checkpw(raw, hash); }
}"""))
story.append(p(
    "Observacao: a senha em texto plano nunca persiste em lugar nenhum. O construtor publico aceita "
    "apenas o hash. A unica forma de criar uma senha a partir de raw e via factory method que ja faz "
    "validacao de forca e hash BCrypt.", body))

story.append(p("Exemplo 4: LandUseDistribution com invariante de soma", h3))
story.append(code("""public LandUseDistribution(double vegetationPercent, double urbanPercent,
                           double waterPercent, double bareSoilPercent) {
    validateRange("vegetation", vegetationPercent);
    validateRange("urban", urbanPercent);
    validateRange("water", waterPercent);
    validateRange("bareSoil", bareSoilPercent);

    double total = vegetationPercent + urbanPercent + waterPercent + bareSoilPercent;
    if (Math.abs(total - 100.0) > TOLERANCE) {
        throw new InvalidLandUseDistributionException(
            "Land use percentages must sum to 100 (+/- " + TOLERANCE + "), got " + total);
    }
    // ...
}"""))

story.append(PageBreak())

# 6.3 Aggregate
story.append(p("6.3 Aggregate e Aggregate Root", h2))
story.append(p(
    "Um <b>Agregado</b> e um cluster de objetos (entidades e value objects) que sao tratados como uma "
    "unica unidade transacional. Toda mudanca passa pela <b>raiz do agregado</b>, que e o ponto de entrada "
    "publico e o responsavel por garantir as <b>invariantes</b> do agregado.", body))
story.append(p("Regras de um agregado bem desenhado:", body))
story.append(li("Apenas a raiz e referenciada externamente. Objetos internos sao acessados via a raiz."))
story.append(li("A raiz garante todas as invariantes. Estado invalido nunca chega a ser construido."))
story.append(li("Uma transacao modifica um unico agregado. Multiplos agregados se coordenam via eventos."))

story.append(p("Agregado Account", h3))
story.append(p(
    "Account e a raiz. Email, Password e ApiKey sao value objects pertencentes ao agregado. As "
    "invariantes protegidas pela raiz incluem:", body))
story.append(li("Senha sempre persistida como hash BCrypt (nunca em texto plano)."))
story.append(li("Email unico (validado pelo use case consultando o repositorio antes de salvar)."))
story.append(li("API key revogada nunca pode ser revogada novamente."))
story.append(li("Eventos de dominio sao emitidos como parte da mudanca de estado."))

story.append(code("""public class Account {

    private final UUID id;
    private final Email email;
    private Password password;
    private ApiKey apiKey;
    private final AccountRole role;
    private final Instant createdAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static Account register(Email email, String rawPassword, AccountRole role) {
        Account account = new Account(
            UUID.randomUUID(),
            email,
            Password.fromRaw(rawPassword),    // <- hash garantido aqui
            ApiKey.generate(),                // <- API key sempre gerada na criacao
            role,
            Instant.now()
        );
        account.domainEvents.add(AccountRegistered.now(account.id, account.email));
        return account;
    }

    public void revokeApiKey() {
        if (apiKey.isRevoked()) {
            throw new ApiKeyAlreadyRevokedException(
                "API key for account " + id + " is already revoked");
        }
        this.apiKey = apiKey.revoke();
        domainEvents.add(ApiKeyRevoked.now(id, apiKey.value()));
    }

    public boolean authenticatesWith(String rawPassword) {
        return password.matches(rawPassword);
    }
    // ...
}"""))

story.append(p("Agregado SatelliteQuery", h3))
story.append(p(
    "Mais simples: agregado de auditoria que registra cada chamada feita aos endpoints de satellite. "
    "Quando criado via factory execute(), emite o evento QueryExecuted contendo accountId, type, "
    "coordinate e cacheHit (usado para auditoria e para billing futuro).", body))
story.append(code("""public static SatelliteQuery execute(UUID accountId, QueryType type,
                                     Coordinate coordinate, boolean cacheHit) {
    SatelliteQuery query = new SatelliteQuery(
        UUID.randomUUID(), accountId, type, coordinate, cacheHit, Instant.now()
    );
    query.domainEvents.add(
        QueryExecuted.now(query.id, accountId, type, coordinate, cacheHit));
    return query;
}"""))

# 6.4 Domain Event
story.append(p("6.4 Domain Event", h2))
story.append(p(
    "Um <b>Evento de Dominio</b> representa algo significativo que aconteceu no negocio. E imutavel, "
    "tem data/hora e nome no <b>passado</b> (Registered, Revoked, Executed). Outros componentes podem "
    "reagir a esses eventos de forma desacoplada.", body))
story.append(p(
    "No OrbittAPI, os agregados acumulam eventos durante a execucao de um caso de uso, e o use case "
    "publica todos eles apos a persistencia bem-sucedida. A publicacao usa ApplicationEventPublisher do "
    "Spring por padrao, mas a interface DomainEventPublisher esta em application/port permitindo trocar "
    "facilmente para Kafka, RabbitMQ ou outro mecanismo em uma evolucao futura.", body))

story.append(p("Definicoes dos eventos do projeto", h3))
story.append(code("""public record AccountRegistered(
        UUID accountId, Email email, Instant occurredOn) implements DomainEvent { }

public record ApiKeyRevoked(
        UUID accountId, String apiKeyValue, Instant occurredOn) implements DomainEvent { }

public record QueryExecuted(
        UUID queryId, UUID accountId, QueryType type,
        Coordinate coordinate, boolean cacheHit, Instant occurredOn) implements DomainEvent { }"""))

story.append(p("Publicacao dentro do use case (apos persistir)", h3))
story.append(code("""@Transactional
public AuthResponse execute(RegisterAccountCommand command) {
    Email email = new Email(command.email());

    if (accountRepository.existsByEmail(email))
        throw new EmailAlreadyInUseException(email.value());

    Account account = Account.register(email, command.password(), AccountRole.DEVELOPER);
    Account saved = accountRepository.save(account);

    eventPublisher.publishAll(saved.pullDomainEvents());   // <- aqui

    TokenProvider.IssuedToken token = tokenProvider.issue(saved);
    return new AuthResponse(...);
}"""))

story.append(PageBreak())

# 6.5 Domain Service
story.append(p("6.5 Domain Service", h2))
story.append(p(
    "Um <b>Domain Service</b> e usado quando uma operacao do dominio nao pertence naturalmente a "
    "nenhuma entidade ou value object. Tipicamente envolve mais de um agregado, ou e uma regra de "
    "calculo que nao se encaixa como metodo de um objeto.", body))
story.append(p(
    "No OrbittAPI nao identificamos a necessidade de Domain Services neste escopo: as operacoes do "
    "dominio (registrar conta, autenticar, revogar key, consultar landuse, consultar vegetation) "
    "couberam todas dentro dos agregados ou dentro de Application Services. Em uma evolucao com "
    "calculo de quotas, billing por consumo, deteccao de anomalias ou regras de plano (Free/Startup/"
    "Business/Enterprise), surgiriam candidatos a Domain Services.", body))

# 6.6 Application Service
story.append(p("6.6 Application Service / Use Case", h2))
story.append(p(
    "Um <b>Application Service</b> e a fachada que orquestra o caso de uso: recebe input, carrega "
    "agregados via repositorios, chama metodos do dominio para executar a logica de negocio, persiste "
    "as mudancas e publica eventos. <b>Nao contem regra de negocio</b>: regra fica no agregado. O "
    "application service so coordena.", body))

us_table = [
    ["Use Case", "Servico", "Responsabilidade"],
    ["RegisterAccountUseCase", "identity",
     "Cria Account, persiste, publica eventos, gera JWT"],
    ["LoginUseCase", "identity",
     "Carrega Account, valida senha, gera JWT"],
    ["GetMyProfileUseCase", "identity",
     "Carrega Account pelo id do JWT, retorna DTO de perfil"],
    ["RevokeApiKeyUseCase", "identity",
     "Carrega Account, executa revokeApiKey(), publica evento"],
    ["GetLandUseUseCase", "satellite",
     "Consulta cache; se miss, chama SatelliteDataSource; persiste auditoria"],
    ["GetVegetationUseCase", "satellite",
     "Mesmo fluxo do landuse mas para NDVI"],
]
story.append(make_table(us_table, col_widths=[5*cm, 2.2*cm, 9.8*cm]))

story.append(p("Exemplo completo: GetLandUseUseCase", h3))
story.append(code("""@Service
public class GetLandUseUseCase {

    private final SatelliteDataSource dataSource;
    private final SatelliteQueryCache cache;
    private final SatelliteQueryRepository queryRepository;
    private final DomainEventPublisher eventPublisher;

    public GetLandUseUseCase(SatelliteDataSource dataSource,
                             SatelliteQueryCache cache,
                             SatelliteQueryRepository queryRepository,
                             DomainEventPublisher eventPublisher) {
        this.dataSource = dataSource;
        this.cache = cache;
        this.queryRepository = queryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public LandUseResponse execute(UUID accountId, double lat, double lng) {
        Coordinate coordinate = new Coordinate(lat, lng);            // VO valida aqui

        Optional<LandUseSnapshot> cached = cache.getLandUse(coordinate);
        boolean cacheHit = cached.isPresent();
        LandUseSnapshot snapshot;
        if (cacheHit) {
            snapshot = cached.get();
        } else {
            snapshot = dataSource.fetchLandUse(coordinate);          // ACL: chama porta
            cache.putLandUse(coordinate, snapshot);
        }

        SatelliteQuery query = SatelliteQuery.execute(
            accountId, QueryType.LAND_USE, coordinate, cacheHit);    // agregado emite evento
        SatelliteQuery saved = queryRepository.save(query);
        eventPublisher.publishAll(saved.pullDomainEvents());

        return new LandUseResponse(/* ... */);
    }
}"""))

# 6.7 Repository
story.append(p("6.7 Repository", h2))
story.append(p(
    "Um <b>Repository</b> abstrai a persistencia de agregados, dando a ilusao de uma colecao em memoria. "
    "Em DDD, a <b>interface do repositorio mora no dominio</b> (porque o dominio precisa dele para "
    "expressar regras), e a <b>implementacao mora na infraestrutura</b> (JPA, MongoDB, in-memory). "
    "Isso e uma aplicacao direta do Princpio de Inversao de Dependencia.", body))

story.append(p("Interface no dominio (identity-service)", h3))
story.append(code("""// br.com.orbittapi.identity.domain.repository
public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    Optional<Account> findByEmail(Email email);
    boolean existsByEmail(Email email);
}"""))

story.append(p("Implementacao na infraestrutura via JPA", h3))
story.append(code("""// br.com.orbittapi.identity.infrastructure.persistence
@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository jpa;

    public AccountRepositoryImpl(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity saved = jpa.save(AccountPersistenceMapper.toEntity(account));
        return AccountPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(AccountPersistenceMapper::toDomain);
    }
    // ...
}"""))
story.append(p(
    "Observe que o dominio nao conhece JPA, nao conhece a tabela accounts, nao conhece o Hibernate. "
    "Ele conhece apenas a interface AccountRepository. A traducao entre Account (dominio) e "
    "AccountJpaEntity (persistencia) e feita por um <b>mapper explicito</b> em "
    "AccountPersistenceMapper.", body))

# 6.8 Factory
story.append(p("6.8 Factory", h2))
story.append(p(
    "Uma <b>Factory</b> encapsula a logica de criacao de objetos complexos. No DDD classico aparecem "
    "como classes separadas ou como metodos de fabrica estaticos dentro do proprio agregado.", body))
story.append(p("No OrbittAPI usamos <b>metodos de fabrica</b> (factory methods) dentro dos agregados:", body))
story.append(li("<b>Account.register(...)</b>: cria uma conta completa com Email, Password hasheada, "
                "ApiKey gerada, timestamp e evento AccountRegistered."))
story.append(li("<b>SatelliteQuery.execute(...)</b>: cria um registro de auditoria com UUID novo e ja "
                "emite o evento QueryExecuted."))
story.append(li("<b>ApiKey.generate()</b>: gera uma nova chave com 24 bytes de entropia via SecureRandom."))
story.append(li("<b>Password.fromRaw(raw)</b> e <b>Password.fromHash(hash)</b>: factories explicitas "
                "para os dois unicos caminhos legitimos de criar uma senha."))

# 6.9 Module
story.append(p("6.9 Module (pacote)", h2))
story.append(p(
    "Modulos em DDD agrupam conceitos relacionados, dando nome ao seu proposito. No OrbittAPI cada "
    "bounded context e um <b>modulo Maven</b> independente, e dentro de cada modulo organizamos por "
    "<b>camada</b> e por <b>subdominio</b>.", body))

mod_tree = """\
br.com.orbittapi.identity/        (bounded context modulo Maven)
|
|-- domain/         <- modelo puro: agregados, value objects, eventos, exceptions, ports
|   |-- model/      <- Account, Email, Password, ApiKey, AccountRole
|   |-- event/      <- AccountRegistered, ApiKeyRevoked, DomainEvent
|   |-- repository/ <- AccountRepository (interface)
|   |-- exception/  <- DomainException + filhas especificas
|
|-- application/    <- casos de uso, DTOs, portas de saida
|   |-- usecase/    <- RegisterAccount, Login, GetMyProfile, RevokeApiKey
|   |-- dto/        <- RegisterAccountCommand, LoginCommand, AuthResponse, ...
|   |-- port/       <- TokenProvider, DomainEventPublisher
|
|-- infrastructure/ <- adapters concretos
|   |-- persistence/<- AccountJpaEntity, AccountJpaRepository, AccountRepositoryImpl
|   |-- security/   <- JwtTokenProvider, JwtAuthenticationFilter, SpringDomainEventPublisher
|   |-- config/     <- SecurityConfig, OpenApiConfig
|
+-- interfaces/rest/ <- entrada HTTP
    |-- AuthController, MeController, ApiKeyController
    +-- GlobalExceptionHandler (RFC 7807)
"""
story.append(ascii_block(mod_tree))
story.append(p("Figura 2: organizacao em camadas dentro de cada bounded context.", caption))

story.append(PageBreak())

# ===== 7. ARQUITETURA EM CAMADAS / HEXAGONAL =====
story.append(p("7. Arquitetura em camadas e Ports and Adapters (Hexagonal)", h1))
story.append(p(
    "DDD propos originalmente uma <b>arquitetura em camadas</b> (Layered Architecture) com Domain, "
    "Application, Infrastructure e User Interface. <b>Alistair Cockburn</b> propos a <b>Hexagonal "
    "Architecture</b> (Ports and Adapters), onde o dominio fica no centro, e tudo que e externo "
    "(banco, HTTP, mensageria, cache) conversa com ele atraves de <b>portas</b> (interfaces) e "
    "<b>adaptadores</b> (implementacoes).", body))
story.append(p("Estrutura do OrbittAPI:", h3))
story.append(li("<b>domain</b>: o nucleo. Codigo Java puro, sem dependencia de Spring nem JPA."))
story.append(li("<b>application</b>: orquestra o dominio. Conhece o dominio, mas define <b>portas</b> "
                "para tudo que e externo (TokenProvider, DomainEventPublisher, SatelliteDataSource, "
                "SatelliteQueryCache, repositorios)."))
story.append(li("<b>infrastructure</b>: implementa as portas (JwtTokenProvider, "
                "RedisSatelliteQueryCache, MockSatelliteDataSource, AccountRepositoryImpl). E onde "
                "vivem as dependencias de framework."))
story.append(li("<b>interfaces/rest</b>: a borda HTTP. Controllers traduzem requests/responses "
                "para os use cases, e o GlobalExceptionHandler traduz excecoes de dominio em "
                "ProblemDetail (RFC 7807)."))

story.append(p("Beneficios praticos disso:", h3))
story.append(li("O dominio e testavel sem Spring (testes JUnit puros e instantaneos)."))
story.append(li("Trocar o adapter Mock por um adapter real da NASA nao requer mudar nada no dominio."))
story.append(li("Trocar Postgres por outro banco e uma mudanca isolada na infraestrutura."))
story.append(li("As regras de negocio nao se misturam com cabecalho HTTP nem com SQL."))

# ===== 8. ACL =====
story.append(p("8. Anti-Corruption Layer aplicada", h1))
story.append(p(
    "O dominio <b>satellite-service</b> nao deveria conhecer detalhes do formato de resposta da NASA, "
    "da ESA ou de qualquer fonte externa. Ele expressa apenas o conceito de SatelliteDataSource: "
    "dada uma Coordinate, me devolve um LandUseSnapshot ou um VegetationSnapshot. Essa e a porta.", body))
story.append(p("Porta no dominio:", h3))
story.append(code("""// br.com.orbittapi.satellite.domain.port.SatelliteDataSource
public interface SatelliteDataSource {
    LandUseSnapshot fetchLandUse(Coordinate coordinate);
    VegetationSnapshot fetchVegetation(Coordinate coordinate);
}"""))

story.append(p("Adapter atual (Mock deterministico):", h3))
story.append(code("""// br.com.orbittapi.satellite.infrastructure.adapter.MockSatelliteDataSource
@Component
public class MockSatelliteDataSource implements SatelliteDataSource {

    @Override
    public LandUseSnapshot fetchLandUse(Coordinate coordinate) {
        Random rng = seededRng(coordinate, "landuse");          // determinista
        double vegetation = roundTo(20 + rng.nextDouble() * 60, 2);
        // ... gera urban, water, bareSoil mantendo soma == 100
        LandUseDistribution distribution = new LandUseDistribution(
            vegetation, urban, water, bareSoil);
        return new LandUseSnapshot(coordinate, distribution,
            LocalDate.now().minusDays(rng.nextInt(30)), SatelliteSource.MOCK);
    }
}"""))
story.append(p(
    "Mesma coordenada produz sempre o mesmo resultado (semente baseada em lat/lng). Isso facilita teste "
    "e demonstracoes reproduziveis. Para usar o adapter NASA no futuro, basta criar "
    "NasaEarthApiAdapter implements SatelliteDataSource e marcar como Primary. <b>Nenhuma linha</b> do "
    "dominio precisa mudar.", body))

story.append(PageBreak())

# ===== 9. RFC 7807 =====
story.append(p("9. Erros padronizados via RFC 7807 (US-10)", h1))
story.append(p(
    "Todas as respostas de erro seguem o padrao <b>Problem Details for HTTP APIs</b> (RFC 7807). E uma "
    "representacao machine-readable que evita que cada API invente seu proprio formato de erro, e foi "
    "exigida pela US-10 do backlog. Implementamos via <b>ProblemDetail</b> do Spring Framework 6 "
    "(introduzido justamente para esse padrao) e um <b>@RestControllerAdvice</b> em cada servico.", body))

story.append(p("Formato padronizado", h3))
story.append(code("""{
  "type": "https://orbittapi.dev/errors/invalid-coordinate",
  "title": "Invalid coordinate",
  "status": 400,
  "detail": "Latitude must be between -90 and 90, got 91.0",
  "instance": "/landuse"
}"""))

story.append(p("Mapeamento entre excecoes de dominio e codigos HTTP", h3))
rfc_table = [
    ["Excecao", "HTTP", "Tipo do problema"],
    ["InvalidEmailException", "400", "invalid-email"],
    ["WeakPasswordException", "400", "weak-password"],
    ["InvalidCoordinateException", "400", "invalid-coordinate"],
    ["InvalidNdviScoreException", "400", "invalid-ndvi"],
    ["InvalidLandUseDistributionException", "400", "invalid-land-use"],
    ["JWT ausente ou invalido (no gateway)", "401", "missing-token / invalid-token"],
    ["InvalidCredentialsException", "401", "invalid-credentials"],
    ["AccessDeniedException (role insuficiente)", "403", "forbidden"],
    ["AccountNotFoundException", "404", "account-not-found"],
    ["EmailAlreadyInUseException", "409", "email-in-use"],
    ["ApiKeyAlreadyRevokedException", "409", "api-key-already-revoked"],
    ["SatelliteDataUnavailableException", "503", "satellite-data-unavailable"],
    ["Qualquer outra Exception", "500", "internal"],
]
story.append(make_table(rfc_table, col_widths=[7.5*cm, 1.5*cm, 7*cm]))

# ===== 10. CACHE =====
story.append(p("10. Cache estrategico no Redis (US-21)", h1))
story.append(p(
    "Cada consulta a um endpoint /landuse ou /vegetation, em producao real, dispararia um processamento "
    "pesado de visao computacional sobre uma imagem multispectral. Em uma plataforma com modelo de "
    "negocio por chamada, cachear essas respostas e essencial para conter o custo unitario.", body))
story.append(p(
    "Aplicamos cache no Redis com TTL de 6 horas (US-21 do backlog), atras de uma <b>porta</b> "
    "SatelliteQueryCache. O cache faz parte do fluxo do GetLandUseUseCase: cada chamada primeiro "
    "verifica o cache; em caso de miss, chama o SatelliteDataSource e armazena o resultado.", body))

story.append(p("Chaves utilizadas", h3))
story.append(li("<code>landuse:{lat}:{lng}</code>"))
story.append(li("<code>vegetation:{lat}:{lng}</code>"))

story.append(p("Decisao de design: DTOs internos no cache", h3))
story.append(p(
    "Para evitar que Jackson dependesse do formato interno dos value objects do dominio (e portanto "
    "evitar poluir o dominio com anotacoes @JsonProperty), criamos records dedicados em "
    "infrastructure/cache/CacheableSnapshots.java que sao convertidos de/para os objetos do dominio "
    "dentro do RedisSatelliteQueryCache. O dominio nao sabe que existe Jackson nem que existe Redis.", body))

story.append(code("""@Override
public Optional<LandUseSnapshot> getLandUse(Coordinate c) {
    return get(landUseKey(c), CacheableSnapshots.LandUseDto.class)
            .map(CacheableSnapshots.LandUseDto::toDomain);
}

@Override
public void putLandUse(Coordinate c, LandUseSnapshot snapshot) {
    put(landUseKey(c), CacheableSnapshots.LandUseDto.fromDomain(snapshot));
}"""))

# ===== 11. SEGURANCA =====
story.append(p("11. Seguranca, JWT e fluxo de autenticacao", h1))
story.append(p(
    "O fluxo de autenticacao foi desenhado para que <b>cada servico tenha responsabilidade unica</b> "
    "sobre seguranca:", body))
story.append(li("O <b>identity-service</b> e o unico que <b>emite</b> JWTs (assinatura HS384 via jjwt)."))
story.append(li("O <b>gateway</b> e o unico que <b>valida</b> JWTs vindos do cliente; apos validar, "
                "ele injeta um header <code>X-User-Id</code> com o UUID do dono do token. Servicos a "
                "jusante nao precisam revalidar."))
story.append(li("O <b>satellite-service</b> apenas le <code>X-User-Id</code>. Se o header faltar, ele "
                "responde 401 RFC 7807."))

flow = """\
+--------+   Authorization: Bearer <JWT>     +-------------+
| Client | --------------------------------> |   Gateway   |
+--------+                                   |  (valida)   |
                                              +------+------+
                                                     |  X-User-Id: <uuid>
                                              +------v---------+
                                              | satellite-svc  |
                                              +----------------+
"""
story.append(ascii_block(flow))
story.append(p("Figura 3: fluxo de propagacao da identidade entre o cliente, o gateway e os servicos.", caption))

story.append(p("Filtro JWT no gateway (trecho)", h3))
story.append(code("""try {
    Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();

    UUID accountId = UUID.fromString(claims.getSubject());
    String role = claims.get("role", String.class);

    ServerHttpRequest mutated = request.mutate()
            .header(USER_HEADER, accountId.toString())
            .header(ROLE_HEADER, role)
            .build();

    return chain.filter(exchange.mutate().request(mutated).build());
} catch (Exception ex) {
    return unauthorized(exchange, "invalid-token", "Invalid or expired JWT");
}"""))

story.append(PageBreak())

# ===== 12. MAPEAMENTO MESTRE =====
story.append(p("12. Mapeamento mestre: conceito DDD para arquivo no codigo", h1))
story.append(p(
    "Para satisfazer a exigencia de <i>trazer todos os topicos abordados em sala</i>, esta tabela "
    "mapeia explicitamente cada conceito DDD a um ponto concreto do codigo.", body))

mapping = [
    ["Conceito DDD", "Arquivo / classe / pacote no projeto"],
    ["Bounded Context", "Cada microservico (identity-service, satellite-service) e um BC"],
    ["Ubiquitous Language", "Termos: Account, ApiKey, SatelliteQuery, NdviScore, VegetationHealth"],
    ["Subdomain Core", "Satellite Data (satellite-service)"],
    ["Subdomain Supporting", "Identity & Access (identity-service)"],
    ["Subdomain Generic", "Billing (nao implementado, mencionado conceitualmente)"],
    ["Context Map", "Identity -> Satellite via Customer-Supplier + Published Language (JWT)"],
    ["Anti-Corruption Layer", "SatelliteDataSource (porta) + MockSatelliteDataSource (adapter)"],
    ["Entity", "Account, SatelliteQuery (identidade por UUID)"],
    ["Value Object", "Email, Password, ApiKey, Coordinate, NdviScore, LandUseDistribution"],
    ["Aggregate Root", "Account; SatelliteQuery"],
    ["Domain Event", "AccountRegistered, ApiKeyRevoked, QueryExecuted"],
    ["Domain Service", "Nao identificado neste escopo (justificativa na secao 6.5)"],
    ["Application Service", "RegisterAccountUseCase, LoginUseCase, GetMyProfileUseCase,\n"
                            "RevokeApiKeyUseCase, GetLandUseUseCase, GetVegetationUseCase"],
    ["Repository (porta)", "AccountRepository, SatelliteQueryRepository (em domain/repository)"],
    ["Repository (adapter)", "AccountRepositoryImpl, SatelliteQueryRepositoryImpl (JPA)"],
    ["Factory", "Account.register(), SatelliteQuery.execute(), ApiKey.generate(),\n"
                "Password.fromRaw() / Password.fromHash()"],
    ["Module", "Cada pacote raiz (br.com.orbittapi.identity, br.com.orbittapi.satellite)"],
    ["Layered Architecture", "domain -> application -> infrastructure -> interfaces/rest"],
    ["Hexagonal (Ports & Adapters)", "Ports em application/port e domain/port; adapters em infrastructure/"],
    ["DDD + RFC 7807", "GlobalExceptionHandler traduz DomainException em ProblemDetail"],
]
story.append(make_table(mapping, col_widths=[5*cm, 12*cm]))

# ===== 13. COBERTURA BACKLOG =====
story.append(p("13. Cobertura do backlog (User Stories)", h1))
backlog = [
    ["User Story", "Implementacao"],
    ["US-01 Cadastro com API Key gerada",
     "POST /auth/register -> Account.register() -> retorna apiKey + JWT"],
    ["US-02 Login com JWT 24h",
     "POST /auth/login -> JwtTokenProvider.issue() com expiracao 86400 segundos"],
    ["US-03 Revogar API Key (admin)",
     "POST /api-keys/{id}/revoke restrito a role ADMIN; emite ApiKeyRevoked"],
    ["US-05 /landuse com lat/lng",
     "GET /landuse -> Coordinate VO -> SatelliteDataSource -> LandUseDistribution"],
    ["US-06 /vegetation com NDVI",
     "GET /vegetation -> NdviScore + classify() em VegetationHealth"],
    ["US-10 Erros padronizados (RFC 7807)",
     "ProblemDetail + GlobalExceptionHandler em cada servico"],
    ["US-21 Cache Redis com TTL 6h",
     "RedisSatelliteQueryCache com Duration.ofHours(6)"],
    ["US-22 LGPD parcial",
     "Senha hasheada com BCrypt, sem dados pessoais em logs"],
]
story.append(make_table(backlog, col_widths=[5.5*cm, 11.5*cm]))
story.append(p(
    "User stories fora do escopo desta GS (front-end dashboard, billing, MFA, ingestao real de NASA/ESA, "
    "modelo de ML, white-label) sao acrescentaveis como <b>novos bounded contexts</b> sem alterar os "
    "atuais. A arquitetura ja esta preparada para essa expansao.", body))

story.append(PageBreak())

# ===== 14. DOCKER =====
story.append(p("14. Como subir o ambiente (Docker)", h1))
story.append(p("Pre-requisitos: Docker Desktop ou Docker Engine + Compose v2.", body))
story.append(p("Comando unico:", body))
story.append(code("docker compose up --build"))
story.append(p("Containers que sobem:", h3))
story.append(li("<b>orbittapi-postgres</b> (porta 5432) - cria identity_db e satellite_db via init.sql"))
story.append(li("<b>orbittapi-redis</b> (porta 6379) - cache"))
story.append(li("<b>orbittapi-identity</b> (porta 8081) - depende de postgres healthy"))
story.append(li("<b>orbittapi-satellite</b> (porta 8082) - depende de postgres e redis healthy"))
story.append(li("<b>orbittapi-gateway</b> (porta 8080) - depende dos dois servicos"))

story.append(p("Healthchecks de cada container", h3))
story.append(code("""# Postgres
test: ["CMD-SHELL", "pg_isready -U orbittapi"]
interval: 5s, timeout: 3s, retries: 10

# Redis
test: ["CMD", "redis-cli", "ping"]

# Servicos Java
HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5
  CMD wget -qO- http://localhost:<porta>/actuator/health | grep -q '"status":"UP"' """))

# ===== 15. ENDPOINTS =====
story.append(p("15. Endpoints REST e exemplos cURL", h1))

story.append(p("15.1 POST /auth/register", h2))
story.append(code("""curl -X POST http://localhost:8080/auth/register \\
  -H "Content-Type: application/json" \\
  -d '{"email":"dev@orbittapi.dev","password":"Abcdefg1"}'"""))
story.append(p("Resposta 201:", body))
story.append(code("""{
  "accountId": "f1be493a-99a0-4bbe-9c19-ab4c8d869037",
  "email": "dev@orbittapi.dev",
  "apiKey": "obt_RtbMOukYQ4LCi7Kg23lRYBhJEA-DmiQI",
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "expiresAt": "2026-05-27T00:00:00Z"
}"""))

story.append(p("15.2 POST /auth/login", h2))
story.append(code("""curl -X POST http://localhost:8080/auth/login \\
  -H "Content-Type: application/json" \\
  -d '{"email":"dev@orbittapi.dev","password":"Abcdefg1"}'"""))

story.append(p("15.3 GET /me", h2))
story.append(code("""curl http://localhost:8080/me \\
  -H "Authorization: Bearer <TOKEN>" """))
story.append(p("Resposta 200:", body))
story.append(code("""{
  "id": "f1be493a-99a0-4bbe-9c19-ab4c8d869037",
  "email": "dev@orbittapi.dev",
  "role": "DEVELOPER",
  "apiKey": "obt_...",
  "apiKeyRevoked": false,
  "createdAt": "2026-05-26T04:30:02.342Z"
}"""))

story.append(p("15.4 GET /landuse", h2))
story.append(code("""curl "http://localhost:8080/landuse?lat=-23.5&lng=-46.6" \\
  -H "Authorization: Bearer <TOKEN>" """))
story.append(p("Resposta 200 (primeira chamada, cache miss):", body))
story.append(code("""{
  "latitude": -23.5,
  "longitude": -46.6,
  "vegetationPercent": 34.08,
  "urbanPercent": 6.8,
  "waterPercent": 5.52,
  "bareSoilPercent": 53.6,
  "imageDate": "2026-05-04",
  "source": "MOCK",
  "cacheHit": false
}"""))
story.append(p("Segunda chamada com a mesma coordenada retorna <code>cacheHit: true</code>.", body))

story.append(p("15.5 GET /vegetation", h2))
story.append(code("""curl "http://localhost:8080/vegetation?lat=-23.5&lng=-46.6" \\
  -H "Authorization: Bearer <TOKEN>" """))
story.append(p("Resposta 200:", body))
story.append(code("""{
  "latitude": -23.5,
  "longitude": -46.6,
  "ndvi": 0.054,
  "health": "NONE",
  "imageDate": "2026-05-19",
  "source": "MOCK",
  "cacheHit": false
}"""))

story.append(p("15.6 POST /api-keys/{accountId}/revoke (apenas ADMIN)", h2))
story.append(code("""curl -X POST http://localhost:8080/api-keys/<accountId>/revoke \\
  -H "Authorization: Bearer <ADMIN_TOKEN>" """))

story.append(p("15.7 Exemplo de erro RFC 7807 (401 sem token)", h2))
story.append(code("""curl -i "http://localhost:8080/landuse?lat=-23.5&lng=-46.6"

HTTP/1.1 401 Unauthorized
Content-Type: application/problem+json

{
  "type": "https://orbittapi.dev/errors/missing-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Missing or invalid Authorization header",
  "instance": "/landuse"
}"""))

story.append(PageBreak())

# ===== 16. TESTES =====
story.append(p("16. Testes automatizados", h1))
story.append(p("Comando para rodar todos os testes:", body))
story.append(code("mvn test"))

story.append(p("Classes de teste presentes", h3))
tests_table = [
    ["Modulo", "Classe", "O que valida"],
    ["identity", "EmailTest",
     "Email vazio, malformado, normalizacao e igualdade por valor"],
    ["identity", "PasswordTest",
     "Senha curta, sem digito, sem maiuscula; hash; matches"],
    ["identity", "AccountTest",
     "register() emite AccountRegistered; revokeApiKey() emite ApiKeyRevoked; nao revoga 2x; identidade por id"],
    ["identity", "RegisterAccountUseCaseTest",
     "Cria conta nova; rejeita email duplicado; publica eventos"],
    ["satellite", "CoordinateTest",
     "Range de latitude e longitude; igualdade por valor"],
    ["satellite", "NdviScoreTest",
     "Range [-1, 1]; classify() em VegetationHealth"],
    ["satellite", "LandUseDistributionTest",
     "Soma == 100 (com tolerancia); rejeita negativos"],
    ["satellite", "SatelliteQueryTest",
     "execute() emite QueryExecuted com type/cacheHit corretos"],
    ["satellite", "MockSatelliteDataSourceTest",
     "Determinismo: mesma coordenada = mesma resposta"],
    ["satellite", "GetLandUseUseCaseTest",
     "Cache miss chama dataSource; cache hit nao chama; salva auditoria; publica eventos"],
]
story.append(make_table(tests_table, col_widths=[2*cm, 4.5*cm, 10.5*cm]))

story.append(p("Filosofia dos testes", h3))
story.append(li("<b>Testes de dominio sao puros</b>: zero Spring, zero JPA, instantaneos. Validam "
                "invariantes e emissao de eventos."))
story.append(li("<b>Testes de use case usam mocks</b>: as portas (Repository, TokenProvider, Cache, "
                "DataSource, EventPublisher) sao mockadas com Mockito."))
story.append(li("<b>Determinismo do adapter Mock</b> e testado para garantir reproducibilidade em "
                "demos e em testes de integracao futuros."))

# ===== 17. DIFERENCIAIS =====
story.append(p("17. Diferenciais implementados", h1))
story.append(li("<b>Anti-Corruption Layer explicita</b> (porta SatelliteDataSource), permitindo trocar "
                "o adapter Mock por um adapter NASA Earth API sem mudar o dominio."))
story.append(li("<b>Cache Redis com chave determinista</b> e TTL de 6 horas (US-21)."))
story.append(li("<b>Validacao centralizada no value object</b>: coordenada invalida nunca chega ao use case."))
story.append(li("<b>Gateway com filtro JWT</b> centralizado: validacao em um unico ponto, propagacao "
                "via header para os servicos a jusante."))
story.append(li("<b>Erros padronizados RFC 7807</b> em todos os servicos."))
story.append(li("<b>Swagger UI por servico</b> (cada bounded context publica sua propria documentacao)."))
story.append(li("<b>Healthchecks via Actuator</b> em todos os 3 servicos Spring Boot."))
story.append(li("<b>Dockerfile multi-stage</b> por servico com build incremental do Maven (cache de dependencias)."))
story.append(li("<b>Construtor injection em 100%</b> do codigo (sem @Autowired em campo)."))
story.append(li("<b>Logging via SLF4J</b> em todas as camadas; zero printStackTrace ou System.out."))
story.append(li("<b>Sem Lombok nas entidades JPA</b>: getters e setters explicitos, evitando armadilhas "
                "com proxies do Hibernate."))

# ===== 18. DEFINITION OF DONE =====
story.append(p("18. Definition of Done - resultado dos testes", h1))
dod = [
    ["Item", "Resultado"],
    ["docker compose up sobe os 5 containers sem erro",
     "OK (5/5 healthy)"],
    ["POST /auth/register cria conta e retorna JWT",
     "OK (201, retorna accountId, apiKey, token, expiresAt)"],
    ["GET /landuse com Bearer retorna 200 + JSON estruturado",
     "OK"],
    ["GET /landuse sem token retorna 401 RFC 7807",
     "OK (type missing-token)"],
    ["Segunda chamada identica e cache hit",
     "OK (cacheHit: true confirmado em log e na resposta)"],
    ["Swagger UI acessivel em cada servico",
     "OK (porta 8081/swagger-ui.html e 8082/swagger-ui.html)"],
    ["mvn test passa em todos os modulos",
     "OK (10 classes de teste, todas passam)"],
    ["docs/architecture.md mapeia DDD para codigo",
     "OK (este documento e seu equivalente em Markdown)"],
    ["README completo",
     "OK (inclui integrantes, cURL, estrutura, tabela DDD)"],
]
story.append(make_table(dod, col_widths=[10.5*cm, 6.5*cm]))

# ===== 19. CONCLUSAO =====
story.append(p("19. Conclusao", h1))
story.append(p(
    "O OrbittAPI demonstra o uso conjunto de SOA e DDD em um projeto coeso. A decomposicao em dois "
    "microservicos refletindo dois bounded contexts da exemplo concreto da relacao entre as duas "
    "disciplinas: SOA define as fronteiras tecnicas, DDD define as fronteiras de negocio, e elas "
    "coincidem por design.", body))
story.append(p(
    "Todos os blocos taticos de DDD (entidades, value objects, agregados, eventos, repositorios, "
    "application services, factories, modulos) aparecem no codigo de forma explicita e nomeavel. Os "
    "padroes estrategicos (linguagem ubiqua, bounded context, anti-corruption layer, context map) sao "
    "decisoes arquiteturais visiveis na estrutura de pastas e na escolha de microservicos.", body))
story.append(p(
    "A entrega esta funcional, testada e empacotada para subir com um unico comando, e a arquitetura "
    "esta preparada para evoluir com novos bounded contexts (billing, dashboard, ingestao real, ML) "
    "sem grandes refatoracoes nos contextos atuais.", body))

story.append(Spacer(1, 1*cm))
story.append(p("Fim do documento.", caption))

# -------------------- build --------------------
doc = SimpleDocTemplate(
    OUT, pagesize=A4,
    leftMargin=2*cm, rightMargin=2*cm,
    topMargin=2*cm, bottomMargin=2*cm,
    title="Documentacao GS OrbittAPI",
    author="Giovanne, Leonardo, Gustavo, Lynn",
)
doc.build(story, onFirstPage=on_page, onLaterPages=on_page)

size_kb = os.path.getsize(OUT) / 1024
print(f"PDF gerado: {OUT}")
print(f"Tamanho: {size_kb:.1f} KB")
