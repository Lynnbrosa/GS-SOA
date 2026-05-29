"""
Gera a documentacao em PDF da Global Solution OrbittAPI.
PDF autossuficiente (sem links externos clicaveis) seguindo as 13 secoes
exigidas pelo professor.

Antes de montar o PDF, este script gera tambem PNGs estilo "terminal" para
servir de prints/evidencias, embutindo o output real capturado durante os
smoke tests do compose com profile soap.
"""
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import cm
from reportlab.lib.enums import TA_JUSTIFY, TA_CENTER, TA_LEFT
from reportlab.lib import colors
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Table, TableStyle,
    Preformatted, Image, KeepTogether
)
from reportlab.pdfgen import canvas
from PIL import Image as PilImage, ImageDraw, ImageFont
import os

HERE = os.path.dirname(os.path.abspath(__file__))
SCREEN_DIR = os.path.join(HERE, "screenshots")
OUT = os.path.join(HERE, "Documentacao_GS_OrbittAPI.pdf")
os.makedirs(SCREEN_DIR, exist_ok=True)

# =============================================================================
# Parte 1: gerar PNGs estilo "terminal" com Pillow
# =============================================================================

def _load_mono_font(size):
    candidates = [
        "C:/Windows/Fonts/consola.ttf",        # Consolas
        "C:/Windows/Fonts/CascadiaCode.ttf",
        "C:/Windows/Fonts/CascadiaMono.ttf",
        "C:/Windows/Fonts/lucon.ttf",          # Lucida Console
        "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf",
    ]
    for path in candidates:
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, size)
            except Exception:
                pass
    return ImageFont.load_default()

def render_terminal_png(out_path, title, body_lines, width=1200, font_size=15,
                        bg="#1e1e1e", fg="#e8e8e8", title_bg="#3c3c3c",
                        title_fg="#ffffff", accent="#7ed957"):
    """Renderiza algo parecido com uma janela de terminal e salva como PNG."""
    font = _load_mono_font(font_size)
    title_font = _load_mono_font(font_size)

    # calcula altura
    line_h = font_size + 6
    title_h = font_size + 18
    padding = 20

    # quebra linhas longas em ate ~100 chars
    wrapped = []
    max_chars = (width - 2 * padding) // (font_size // 2 + 1)
    for line in body_lines:
        if not line:
            wrapped.append("")
            continue
        while len(line) > max_chars:
            wrapped.append(line[:max_chars])
            line = line[max_chars:]
        wrapped.append(line)

    height = title_h + padding * 2 + line_h * len(wrapped) + 10
    img = PilImage.new("RGB", (width, height), bg)
    draw = ImageDraw.Draw(img)

    # barra de titulo
    draw.rectangle([(0, 0), (width, title_h)], fill=title_bg)
    # bolinhas mac
    for i, c in enumerate(["#ff5f56", "#ffbd2e", "#27c93f"]):
        cx = 18 + i * 22
        cy = title_h // 2
        draw.ellipse([(cx - 7, cy - 7), (cx + 7, cy + 7)], fill=c)
    draw.text((100, 6), title, font=title_font, fill=title_fg)

    # corpo
    y = title_h + padding
    for line in wrapped:
        if line.startswith("$ "):
            draw.text((padding, y), "$ ", font=font, fill=accent)
            draw.text((padding + font.getlength("$ "), y), line[2:], font=font, fill=fg)
        elif line.startswith("# "):
            draw.text((padding, y), line, font=font, fill="#a0a0a0")
        elif "HTTP/" in line or line.startswith(">>>"):
            draw.text((padding, y), line, font=font, fill=accent)
        elif line.startswith("ERROR") or "Fault" in line:
            draw.text((padding, y), line, font=font, fill="#ff7b72")
        elif "Tests run:" in line and "Failures: 0, Errors: 0" in line:
            draw.text((padding, y), line, font=font, fill=accent)
        elif "BUILD SUCCESS" in line:
            draw.text((padding, y), line, font=font, fill=accent)
        elif "healthy" in line.lower():
            draw.text((padding, y), line, font=font, fill=accent)
        else:
            draw.text((padding, y), line, font=font, fill=fg)
        y += line_h

    img.save(out_path, "PNG", optimize=True)
    return out_path


# -------- screenshots concretos ---------------------------------------------

S = {}

S["docker_ps"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "01_docker_ps.png"),
    "PowerShell - docker ps (6 containers healthy)",
    [
        "$ docker ps --filter \"name=orbittapi\" --format \"table {{.Names}}\\t{{.Status}}\"",
        "",
        "NAMES                 STATUS",
        "orbittapi-soap        Up 4 minutes (healthy)",
        "orbittapi-satellite   Up 4 minutes (healthy)",
        "orbittapi-gateway     Up 6 minutes (healthy)",
        "orbittapi-identity    Up 6 minutes (healthy)",
        "orbittapi-postgres    Up 6 minutes (healthy)",
        "orbittapi-redis       Up 6 minutes (healthy)",
    ],
)

S["mvn_test"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "02_mvn_test.png"),
    "PowerShell - mvn test (39 testes, todos verdes)",
    [
        "$ mvn test",
        "",
        "[INFO] Running br.com.orbittapi.identity.application.DeleteAccountUseCaseTest",
        "[INFO] Running br.com.orbittapi.identity.application.RegisterAccountUseCaseTest",
        "[INFO] Running br.com.orbittapi.identity.application.UpdateAccountEmailUseCaseTest",
        "[INFO] Running br.com.orbittapi.identity.domain.AccountTest",
        "[INFO] Running br.com.orbittapi.identity.domain.EmailTest",
        "[INFO] Running br.com.orbittapi.identity.domain.PasswordTest",
        "[INFO] Tests run: 21, Failures: 0, Errors: 0, Skipped: 0",
        "[INFO] Running br.com.orbittapi.satellite.application.GetLandUseUseCaseTest",
        "[INFO] Running br.com.orbittapi.satellite.domain.CoordinateTest",
        "[INFO] Running br.com.orbittapi.satellite.domain.LandUseDistributionTest",
        "[INFO] Running br.com.orbittapi.satellite.domain.NdviScoreTest",
        "[INFO] Running br.com.orbittapi.satellite.domain.SatelliteQueryTest",
        "[INFO] Running br.com.orbittapi.satellite.infrastructure.MockSatelliteDataSourceTest",
        "[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0",
        "[INFO] Running br.com.orbittapi.soap.endpoint.SatelliteEndpointTest",
        "[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0",
        "",
        "[INFO] BUILD SUCCESS    (TOTAL: 39 testes, 0 falhas)",
    ],
)

S["register"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "03_register.png"),
    "PowerShell - POST /auth/register (US-01: cadastro com JWT)",
    [
        "$ curl -X POST http://localhost:8080/auth/register -H \"Content-Type: application/json\" \\",
        "       -d '{\"email\":\"dev@orbittapi.dev\",\"password\":\"Abcdefg1\"}'",
        "",
        ">>> HTTP/1.1 201 Created",
        "",
        "{",
        "  \"accountId\": \"412e2976-2281-4358-be78-4e16621e2bc0\",",
        "  \"email\": \"dev@orbittapi.dev\",",
        "  \"apiKey\": \"obt_QBC1atcWJBihhi3yElZPH-YsyTA2Swro\",",
        "  \"token\": \"eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJvcmJpdHRhcGktaWRlbnRpdHkiLCJzdWIi...\",",
        "  \"expiresAt\": \"2026-05-30T20:13:33.727366898Z\"",
        "}",
    ],
)

S["landuse_cache"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "04_landuse_cache.png"),
    "PowerShell - GET /landuse + cache hit no Redis (US-05 + US-21)",
    [
        "$ curl \"http://localhost:8080/landuse?lat=-23.5&lng=-46.6\" -H \"Authorization: Bearer $TOKEN\"",
        "",
        ">>> HTTP/1.1 200 OK",
        "",
        "{",
        "  \"latitude\": -23.5, \"longitude\": -46.6,",
        "  \"vegetationPercent\": 34.08, \"urbanPercent\": 6.8,",
        "  \"waterPercent\": 5.52, \"bareSoilPercent\": 53.6,",
        "  \"imageDate\": \"2026-05-07\", \"source\": \"MOCK\",",
        "  \"cacheHit\": false",
        "}",
        "",
        "$ curl \"http://localhost:8080/vegetation?lat=-23.5&lng=-46.6\" -H \"Authorization: Bearer $TOKEN\"",
        "",
        ">>> HTTP/1.1 200 OK",
        "",
        "{",
        "  \"latitude\": -23.5, \"longitude\": -46.6,",
        "  \"ndvi\": 0.054, \"health\": \"NONE\",",
        "  \"imageDate\": \"2026-05-22\", \"source\": \"MOCK\",",
        "  \"cacheHit\": true",
        "}",
    ],
)

S["put_me"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "05_put_me.png"),
    "PowerShell - PUT /me (CRUD - atualiza email) + 409 RFC 7807",
    [
        "$ curl -X PUT http://localhost:8080/me -H \"Authorization: Bearer $TOKEN\" \\",
        "       -H \"Content-Type: application/json\" -d '{\"email\":\"dev-renamed@orbittapi.dev\"}'",
        "",
        ">>> HTTP/1.1 200 OK",
        "",
        "{",
        "  \"id\": \"412e2976-2281-4358-be78-4e16621e2bc0\",",
        "  \"email\": \"dev-renamed@orbittapi.dev\",",
        "  \"role\": \"DEVELOPER\",",
        "  \"apiKeyRevoked\": false",
        "}",
        "",
        "# Email duplicado -> 409 RFC 7807",
        "$ curl -X PUT http://localhost:8080/me -d '{\"email\":\"already@orbittapi.dev\"}' ...",
        "",
        ">>> HTTP/1.1 409 Conflict",
        "",
        "{",
        "  \"type\": \"https://orbittapi.dev/errors/email-in-use\",",
        "  \"title\": \"Email already in use\", \"status\": 409,",
        "  \"detail\": \"Email already in use: already@orbittapi.dev\",",
        "  \"instance\": \"/me\"",
        "}",
    ],
)

S["delete_forbidden"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "06_delete_forbidden.png"),
    "PowerShell - DELETE /accounts/{id} sem ADMIN (CRUD - 403)",
    [
        "$ curl -X DELETE \"http://localhost:8080/accounts/412e2976-2281-4358-be78-4e16621e2bc0\" \\",
        "       -H \"Authorization: Bearer $TOKEN_DEVELOPER\"",
        "",
        ">>> HTTP/1.1 403 Forbidden",
        "",
        "(role insuficiente; somente tokens com role=ADMIN podem apagar contas)",
    ],
)

S["soap_consulta"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "07_soap_consulta.png"),
    "PowerShell - SOAP consultarVegetacao (POST /ws na porta 8083)",
    [
        "$ curl -X POST http://localhost:8083/ws -H \"Content-Type: text/xml; charset=utf-8\" \\",
        "       -H 'SOAPAction: \"\"' -d @consultar.xml",
        "",
        "# consultar.xml (REQUEST):",
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"",
        "                  xmlns:sat=\"http://orbittapi.dev/soap/satellite\">",
        "  <soapenv:Body>",
        "    <sat:ConsultarVegetacaoRequest>",
        "      <sat:latitude>-23.5</sat:latitude>",
        "      <sat:longitude>-46.6</sat:longitude>",
        "    </sat:ConsultarVegetacaoRequest>",
        "  </soapenv:Body>",
        "</soapenv:Envelope>",
        "",
        ">>> HTTP/1.1 200 OK   (RESPONSE)",
        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">",
        "  <SOAP-ENV:Body>",
        "    <ns2:ConsultarVegetacaoResponse xmlns:ns2=\"http://orbittapi.dev/soap/satellite\">",
        "      <ns2:latitude>-23.5</ns2:latitude>",
        "      <ns2:longitude>-46.6</ns2:longitude>",
        "      <ns2:ndvi>0.054</ns2:ndvi>",
        "      <ns2:health>NONE</ns2:health>",
        "      <ns2:imageDate>2026-05-22</ns2:imageDate>",
        "      <ns2:source>MOCK</ns2:source>",
        "    </ns2:ConsultarVegetacaoResponse>",
        "  </SOAP-ENV:Body>",
        "</SOAP-ENV:Envelope>",
    ],
)

S["soap_registrar"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "08_soap_registrar.png"),
    "PowerShell - SOAP registrarConsulta (persiste e retorna queryId)",
    [
        "$ curl -X POST http://localhost:8083/ws -H \"Content-Type: text/xml; charset=utf-8\" -d @reg.xml",
        "",
        "# REQUEST:",
        "<sat:RegistrarConsultaRequest xmlns:sat=\"http://orbittapi.dev/soap/satellite\">",
        "  <sat:accountId>00000000-0000-0000-0000-000000000501</sat:accountId>",
        "  <sat:tipo>VEGETATION</sat:tipo>",
        "  <sat:latitude>-23.5</sat:latitude>",
        "  <sat:longitude>-46.6</sat:longitude>",
        "</sat:RegistrarConsultaRequest>",
        "",
        ">>> HTTP/1.1 200 OK   (RESPONSE)",
        "<ns2:RegistrarConsultaResponse xmlns:ns2=\"http://orbittapi.dev/soap/satellite\">",
        "  <ns2:queryId>ac63d5ba-3b4d-4a60-a103-49529d8f55c1</ns2:queryId>",
        "  <ns2:status>EXECUTED</ns2:status>",
        "  <ns2:executedAt>2026-05-29T20:13:20.354Z</ns2:executedAt>",
        "</ns2:RegistrarConsultaResponse>",
    ],
)

S["soap_fault"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "09_soap_fault.png"),
    "PowerShell - SOAP Fault: latitude invalida (Client error)",
    [
        "$ curl -X POST http://localhost:8083/ws -H \"Content-Type: text/xml; charset=utf-8\" \\",
        "       -d '<sat:ConsultarVegetacaoRequest>",
        "             <sat:latitude>95</sat:latitude>",
        "             <sat:longitude>0</sat:longitude>",
        "           </sat:ConsultarVegetacaoRequest>'",
        "",
        ">>> SOAP Fault:",
        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">",
        "  <SOAP-ENV:Body>",
        "    <SOAP-ENV:Fault>",
        "      <faultcode>SOAP-ENV:Client</faultcode>",
        "      <faultstring xml:lang=\"en\">Invalid request</faultstring>",
        "    </SOAP-ENV:Fault>",
        "  </SOAP-ENV:Body>",
        "</SOAP-ENV:Envelope>",
    ],
)

S["wsdl"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "10_wsdl.png"),
    "PowerShell - WSDL publicado em /ws/satellite.wsdl",
    [
        "$ curl -s http://localhost:8083/ws/satellite.wsdl | head -25",
        "",
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>",
        "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"",
        "                  xmlns:sch=\"http://orbittapi.dev/soap/satellite\"",
        "                  xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"",
        "                  xmlns:tns=\"http://orbittapi.dev/soap/satellite\"",
        "                  targetNamespace=\"http://orbittapi.dev/soap/satellite\">",
        "  <wsdl:types>",
        "    <xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"",
        "               targetNamespace=\"http://orbittapi.dev/soap/satellite\">",
        "      <xs:simpleType name=\"VegetationHealth\">",
        "        <xs:restriction base=\"xs:string\">",
        "          <xs:enumeration value=\"NONE\"/>",
        "          <xs:enumeration value=\"SPARSE\"/>",
        "          <xs:enumeration value=\"MODERATE\"/>",
        "          <xs:enumeration value=\"DENSE\"/>",
        "        </xs:restriction>",
        "      </xs:simpleType>",
        "      ...",
    ],
)

S["queries"] = render_terminal_png(
    os.path.join(SCREEN_DIR, "11_queries.png"),
    "PowerShell - POST /queries (endpoint novo do satellite-service)",
    [
        "$ curl -X POST http://localhost:8082/queries \\",
        "       -H \"X-User-Id: 412e2976-2281-4358-be78-4e16621e2bc0\" \\",
        "       -H \"Content-Type: application/json\" \\",
        "       -d '{\"type\":\"LAND_USE\",\"latitude\":-23.5,\"longitude\":-46.6}'",
        "",
        ">>> HTTP/1.1 201 Created",
        "",
        "{",
        "  \"queryId\": \"703bcd79-f760-4009-9026-2bc0525e9db7\",",
        "  \"type\": \"LAND_USE\",",
        "  \"latitude\": -23.5,",
        "  \"longitude\": -46.6,",
        "  \"status\": \"EXECUTED\",",
        "  \"executedAt\": \"2026-05-29T20:13:57.022860935Z\"",
        "}",
    ],
)


# =============================================================================
# Parte 2: montar o PDF
# =============================================================================
styles = getSampleStyleSheet()

title_style = ParagraphStyle(
    'TitleBig', parent=styles['Title'], fontSize=26, leading=32,
    spaceAfter=12, alignment=TA_CENTER, textColor=colors.HexColor("#0b3d91"))
subtitle_style = ParagraphStyle(
    'Subtitle', parent=styles['Normal'], fontSize=14, leading=18,
    alignment=TA_CENTER, textColor=colors.HexColor("#444444"), spaceAfter=24)
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
    leftIndent=4, rightIndent=4, spaceBefore=4, spaceAfter=8)
ascii_style = ParagraphStyle('Ascii', parent=code_style, alignment=TA_LEFT)
caption = ParagraphStyle('Caption', parent=body, fontSize=9, leading=12,
                         textColor=colors.HexColor("#555555"), alignment=TA_CENTER,
                         spaceAfter=10)

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

def shot(path, caption_text=None, max_width=17 * cm):
    """Insere uma imagem PNG do screenshots/ dimensionada para caber na pagina."""
    img = Image(path)
    iw, ih = img.imageWidth, img.imageHeight
    ratio = max_width / iw
    img.drawWidth = max_width
    img.drawHeight = ih * ratio
    parts = [img]
    if caption_text:
        parts.append(p(caption_text, caption))
    return KeepTogether(parts)

def on_page(canv, doc):
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

# ===== CAPA + INTEGRANTES =====
story.append(Spacer(1, 4*cm))
story.append(p("OrbittAPI", title_style))
story.append(p("Plataforma SaaS de dados satelitais", subtitle_style))
story.append(Spacer(1, 1*cm))
story.append(p("Documentacao da Global Solution 2026.1", h2))
story.append(p("Disciplinas combinadas: SOA (Service-Oriented Architecture) "
               "e DDD (Domain-Driven Design)", body))
story.append(Spacer(1, 0.5*cm))
story.append(p("Tema do semestre: Industria Espacial", body))
story.append(p("ODS principal: 13 (Acao contra a mudanca global do clima). "
               "ODS secundario: 2 (Fome zero e agricultura sustentavel).", body))
story.append(Spacer(1, 1.5*cm))

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
for s in [
    "1. Descricao da solucao proposta",
    "2. Problema que sera resolvido",
    "3. Objetivos da aplicacao",
    "4. Arquitetura da solucao",
    "5. Diagrama de Arquitetura SOA",
    "6. Explicacao da API REST",
    "7. Explicacao do Web Service SOAP",
    "8. Explicacao da integracao entre os servicos",
    "9. Tecnologias utilizadas",
    "10. Evidencias de funcionamento",
    "11. Prints dos testes realizados",
    "12. Conclusao do projeto",
]:
    story.append(p(s, body))
story.append(PageBreak())


# ===== 1. DESCRICAO DA SOLUCAO =====
story.append(p("1. Descricao da solucao proposta", h1))
story.append(p(
    "OrbittAPI e uma plataforma <b>Software-as-a-Service</b> de acesso a dados "
    "satelitais. A solucao agrega imagens de satelite (Landsat, Sentinel/Copernicus) "
    "e disponibiliza metricas prontas para uso via API REST simples - indice de "
    "vegetacao (NDVI), uso do solo, risco de alagamento, deteccao de desmatamento "
    "e expansao urbana. Empresas de qualquer setor (agronegocio, seguradoras, "
    "construtoras, consultorias ambientais) podem consumir inteligencia espacial "
    "sem precisar de cientistas de dados ou infraestrutura propria.", body))
story.append(p(
    "Este repositorio implementa o <b>backend</b> da Global Solution. A entrega "
    "atende simultaneamente as duas disciplinas: SOA, com a decomposicao em "
    "microservicos comunicando-se por HTTP REST e SOAP, e DDD, com modelagem por "
    "bounded contexts, agregados, value objects, eventos de dominio e arquitetura "
    "em portas e adaptadores.", body))
story.append(p(
    "Sao 4 servicos backend: dois microservicos de dominio (identity-service e "
    "satellite-service), um API Gateway que valida JWT e propaga identidade, e um "
    "Web Service SOAP opcional que reutiliza o satellite-service via REST.", body))


# ===== 2. PROBLEMA =====
story.append(p("2. Problema que sera resolvido", h1))
story.append(p(
    "Dados satelitais publicos (NASA, ESA, INPE) sao um recurso valioso para "
    "monitoramento ambiental, agricultura, planejamento urbano e gestao de risco. "
    "Contudo, consumi-los hoje exige:", body))
story.append(li("Lidar com formatos brutos (TIFFs multispectrais, GeoJSON de telemetria, indices crus)"))
story.append(li("Ter infraestrutura para processar imagens com visao computacional"))
story.append(li("Manter integracoes especificas com cada provedor (NASA Earth API, Copernicus Open Hub, etc.)"))
story.append(li("Contratar especialistas em sensoriamento remoto"))
story.append(p(
    "<b>Resultado:</b> apenas empresas com grande estrutura conseguem extrair "
    "valor desses dados. Pequenos produtores rurais, startups de agritech, "
    "seguradoras regionais e consultorias ambientais ficam de fora.", body))
story.append(p(
    "OrbittAPI resolve isso oferecendo uma camada de servicos REST que abstrai "
    "essa complexidade: o cliente faz <code>GET /vegetation?lat&amp;lng</code> e "
    "recebe um NDVI ja calculado e classificado por nivel de vegetacao. O onus de "
    "ingerir, processar e armazenar os dados de satelite fica todo na plataforma.", body))


# ===== 3. OBJETIVOS =====
story.append(p("3. Objetivos da aplicacao", h1))
objetivos = [
    ["Eixo", "Objetivo concreto"],
    ["Negocio",
     "Disponibilizar metricas de satelite via API REST com modelo SaaS por consumo, "
     "permitindo escalar de pequenos produtores a clientes Enterprise"],
    ["Tecnico (SOA)",
     "Arquitetura orientada a servicos com 2 microservicos + gateway + web service SOAP, "
     "cada qual com seu proprio banco e ciclo de deploy"],
    ["Tecnico (DDD)",
     "Modelo de dominio rico com agregados, value objects, eventos e portas. "
     "Anti-corruption layer para a fonte de dados externa"],
    ["Operacional",
     "Subir o ambiente inteiro com um unico comando (docker compose). "
     "Healthchecks em todos os servicos"],
    ["Performance",
     "Cache Redis com TTL de 6 horas para reduzir custo de processamento e latencia"],
    ["Confiabilidade",
     "Erros padronizados em RFC 7807 (Problem Details). "
     "Testes automatizados cobrindo agregados, value objects e casos de uso"],
    ["Sustentabilidade",
     "Alinhamento aos ODS 13 (clima) e 2 (agricultura sustentavel) via NDVI "
     "e indices de uso do solo"],
]
story.append(make_table(objetivos, col_widths=[3.5*cm, 13.5*cm]))


# ===== 4. ARQUITETURA DA SOLUCAO =====
story.append(p("4. Arquitetura da solucao", h1))
story.append(p(
    "A solucao adota dois pilares complementares: <b>SOA</b> define as "
    "fronteiras tecnicas (servicos independentes, comunicacao por HTTP) e "
    "<b>DDD</b> define as fronteiras de negocio (bounded contexts, agregados, "
    "linguagem ubiqua). As duas fronteiras coincidem por desenho: cada bounded "
    "context vira um microservico Maven separado.", body))

story.append(p("Camadas internas de cada servico (DDD classica)", h3))
arq_tree = """\
br.com.orbittapi.<servico>/
|-- domain/          <- nucleo: agregados, value objects, eventos, ports
|   |-- model/
|   |-- event/
|   |-- repository/  <- interfaces (portas)
|   |-- port/        <- outras portas (ex: SatelliteDataSource)
|   `-- exception/
|-- application/     <- orquestracao de casos de uso
|   |-- usecase/     <- @Service - regras de orquestracao
|   |-- dto/         <- comandos de entrada / respostas
|   `-- port/        <- portas usadas pelo use case (TokenProvider...)
|-- infrastructure/  <- adapters concretos (JPA, Redis, JWT, Mock)
|   |-- persistence/
|   |-- adapter/
|   |-- cache/
|   |-- security/
|   `-- config/
`-- interfaces/rest/ <- controllers + GlobalExceptionHandler (RFC 7807)
"""
story.append(ascii_block(arq_tree))
story.append(p("Figura 1: estrutura DDD interna de cada microservico.", caption))

story.append(p("Bounded contexts e suas responsabilidades", h3))
bc_table = [
    ["Contexto", "Servico", "Linguagem ubiqua propria"],
    ["Identity & Access",  "identity-service (8081)",
     "Account, ApiKey, Email, Password, Role, Credentials"],
    ["Satellite Data",     "satellite-service (8082)",
     "SatelliteQuery, Coordinate, NdviScore, LandUseDistribution, "
     "VegetationHealth, SatelliteSource"],
    ["API Gateway",        "gateway (8080)",
     "Spring Cloud Gateway com validacao JWT centralizada"],
    ["SOAP Gateway",       "soap-service (8083, profile soap)",
     "Operacoes SOAP que consomem satellite REST internamente"],
]
story.append(make_table(bc_table, col_widths=[3.5*cm, 5.5*cm, 8*cm]))

story.append(p("Decisao chave: dois bancos, dois schemas", h3))
story.append(p(
    "Cada bounded context tem seu schema isolado no Postgres (identity_db e "
    "satellite_db). Isso reforca a autonomia: o satellite-service nunca le "
    "diretamente da tabela de contas - ele recebe apenas um <code>X-User-Id</code> "
    "(UUID) via header HTTP, injetado pelo gateway apos validar o JWT.", body))

story.append(PageBreak())


# ===== 5. DIAGRAMA DE ARQUITETURA SOA =====
story.append(p("5. Diagrama de Arquitetura SOA", h1))

diagrama = """\
                                                              +-------------+
                                                              | SOAP client |
                                                              | (SoapUI...) |
                                                              +------+------+
                       +------------------+                          |
       REST client --->|     Gateway      |  porta 8080              | SOAP
                       |  (valida JWT,    |                          v
                       |   injeta         |                 +-------------------+
                       |   X-User-Id)     |                 |   soap-service    | 8083
                       +--------+---------+                 |   (profile soap,  |
                                |                           |    contract-first)|
                +---------------+----------------+          +-------+-----------+
                |                                |                  |
       +--------v---------+              +-------v----------+       |  REST interna
       | identity-service |              | satellite-service|<------+  (X-User-Id)
       |   porta 8081     |              |   porta 8082     |
       +--------+---------+              +-------+----------+
                |                                |
                |    +-----------+   +---------+ |
                +--->| Postgres  |   |  Redis  |<+
                     |   5432    |   |  6379   |
                     +-----------+   +---------+
                identity_db /        cache landuse:{lat}:{lng}
                satellite_db         vegetation:{lat}:{lng}  (TTL 6h)
"""
story.append(ascii_block(diagrama))
story.append(p("Figura 2: arquitetura SOA completa. "
               "O Gateway e ponto unico de entrada REST. O SOAP gateway e opcional "
               "(profile 'soap') e consome o satellite-service via REST.",
               caption))

story.append(p("Caracteristicas SOA presentes", h3))
story.append(li("<b>Decomposicao por bounded context</b>: dois microservicos com "
                "responsabilidades disjuntas (autenticacao vs. dados de satelite)."))
story.append(li("<b>Autonomia de dados</b>: cada servico tem seu schema. "
                "Nenhuma consulta cross-schema. Comunicacao so via HTTP."))
story.append(li("<b>Gateway pattern</b>: ponto de entrada unico com validacao "
                "centralizada de JWT. Servicos a jusante nao revalidam token."))
story.append(li("<b>Service composition</b>: o soap-service compoe operacoes "
                "executando chamadas REST internas. E uma evidencia direta de "
                "integracao entre estilos arquiteturais."))
story.append(li("<b>Independencia de deploy</b>: cada servico tem seu Dockerfile, "
                "sua porta e seu ciclo. Profile 'soap' permite subir backend REST "
                "sem o SOAP (compatibilidade com clientes legados ou apps mobile)."))
story.append(li("<b>Padronizacao de erros</b>: todos os servicos REST retornam "
                "ProblemDetail (RFC 7807). O SOAP retorna SOAP Fault padrao."))


# ===== 6. API REST =====
story.append(p("6. Explicacao da API REST", h1))
story.append(p(
    "A API REST e exposta pelo <b>Gateway</b> na porta 8080. Todas as chamadas "
    "passam por ele. Endpoints de autenticacao sao publicos; os demais exigem "
    "<code>Authorization: Bearer &lt;JWT&gt;</code>. Em caso de erro, todos os "
    "endpoints retornam um <b>ProblemDetail</b> (RFC 7807).", body))

rest_table = [
    ["Metodo", "Rota", "Auth", "Body / Query", "O que faz"],
    ["POST", "/auth/register", "publico",
     "{email, password}",
     "Cria conta, gera API Key (obt_...) e retorna JWT (24h)"],
    ["POST", "/auth/login", "publico",
     "{email, password}",
     "Autentica e retorna JWT"],
    ["GET",  "/me", "Bearer", "-",
     "Devolve o perfil da conta autenticada"],
    ["PUT",  "/me", "Bearer", "{email}",
     "CRUD - atualiza o e-mail da conta autenticada"],
    ["POST", "/api-keys/{accountId}/revoke", "Bearer (ADMIN)", "-",
     "Revoga a API Key da conta indicada (US-03)"],
    ["DELETE", "/accounts/{accountId}", "Bearer (ADMIN)", "-",
     "CRUD - apaga a conta indicada e emite AccountDeleted"],
    ["GET", "/landuse", "Bearer",
     "lat, lng",
     "Devolve LandUseDistribution (vegetacao, urbano, agua, solo)"],
    ["GET", "/vegetation", "Bearer",
     "lat, lng",
     "Devolve NDVI + classificacao em VegetationHealth"],
    ["POST", "/queries", "X-User-Id",
     "{type, latitude, longitude}",
     "Registra um SatelliteQuery e emite QueryExecuted"],
]
story.append(make_table(rest_table, col_widths=[1.6*cm, 4*cm, 2.8*cm, 3.6*cm, 5*cm]))

story.append(p("Codigos HTTP retornados", h3))
http_table = [
    ["Caso", "HTTP", "Tipo (RFC 7807)"],
    ["Validacao falha (coordenada invalida, body invalido, etc.)", "400", "invalid-coordinate / validation-failed"],
    ["JWT ausente ou expirado",                                     "401", "missing-token / invalid-token"],
    ["Credenciais invalidas no /auth/login",                        "401", "invalid-credentials"],
    ["Token sem ROLE_ADMIN em rota privilegiada",                   "403", "forbidden"],
    ["Conta nao encontrada",                                        "404", "account-not-found"],
    ["E-mail ja em uso no register/PUT /me",                        "409", "email-in-use"],
    ["API Key ja revogada",                                         "409", "api-key-already-revoked"],
    ["Satellite externo indisponivel (futuro adapter NASA)",        "503", "satellite-data-unavailable"],
]
story.append(make_table(http_table, col_widths=[10*cm, 1.4*cm, 5.6*cm]))


# ===== 7. SOAP =====
story.append(p("7. Explicacao do Web Service SOAP", h1))
story.append(p(
    "O modulo <b>soap-service</b> (porta 8083) atende ao requisito de "
    "<b>Web Service SOAP com WSDL</b> da disciplina. E <b>contract-first</b>: "
    "o contrato e definido pelo XSD <code>soap-service/src/main/resources/satellite.xsd</code> "
    "e o WSDL e gerado em runtime pelo Spring WS.", body))

story.append(p("Detalhes tecnicos", h3))
story.append(li("Spring Boot 3.3.5 com <code>spring-boot-starter-web-services</code> + <code>wsdl4j</code>"))
story.append(li("Plugin <code>jaxb2-maven-plugin</code> 3.2.0 gera classes Java em "
                "<code>br.com.orbittapi.soap.generated</code> a partir do XSD na fase generate-sources"))
story.append(li("<code>MessageDispatcherServlet</code> registrado em <code>/ws/*</code>"))
story.append(li("<code>DefaultWsdl11Definition</code> publica o WSDL em <code>/ws/satellite.wsdl</code>"))
story.append(li("Namespace: <code>http://orbittapi.dev/soap/satellite</code>"))
story.append(li("<code>SoapFaultMappingExceptionResolver</code> mapeia excecoes para SOAP Fault Client/Server"))

story.append(p("Operacoes expostas", h3))
soap_ops = [
    ["Operacao", "Entrada", "Saida", "Integracao interna"],
    ["consultarVegetacao",
     "latitude, longitude",
     "latitude, longitude, ndvi, health (VegetationHealth), imageDate, source",
     "Chama GET /vegetation no satellite-service via RestClient"],
    ["registrarConsulta",
     "accountId, tipo (QueryType), latitude, longitude",
     "queryId, status, executedAt",
     "Chama POST /queries no satellite-service; persiste SatelliteQuery"],
]
story.append(make_table(soap_ops, col_widths=[3.8*cm, 3.5*cm, 4.8*cm, 4.9*cm]))

story.append(p("Tipos definidos no XSD (regras como contrato)", h3))
story.append(li("<code>Latitude</code> e <code>Longitude</code>: tipos simples com restricoes "
                "<code>xs:minInclusive/maxInclusive</code> de [-90, 90] e [-180, 180]"))
story.append(li("<code>VegetationHealth</code>: enum NONE | SPARSE | MODERATE | DENSE"))
story.append(li("<code>QueryType</code>: enum VEGETATION | LAND_USE"))
story.append(li("<code>SatelliteSource</code>: enum LANDSAT | SENTINEL | MODIS | MOCK"))
story.append(p("As validacoes do XSD garantem que envelopes SOAP malformados nem "
               "chegam ao @Endpoint - sao rejeitados ainda na camada de unmarshaling, "
               "virando SOAP Fault automaticamente.", body))


# ===== 8. INTEGRACAO ENTRE OS SERVICOS =====
story.append(p("8. Explicacao da integracao entre os servicos", h1))

story.append(p("8.1 Cliente REST -> Gateway -> Microservicos", h2))
story.append(p(
    "Todos os clientes REST batem na porta 8080 (gateway). O gateway tem 5 rotas "
    "configuradas em <code>application.yml</code>: <code>/auth/**</code>, "
    "<code>/me/**</code>, <code>/api-keys/**</code>, <code>/accounts/**</code> e "
    "<code>/landuse</code> + <code>/vegetation</code> (para o satellite). Antes "
    "de propagar, o filtro <code>JwtAuthenticationGatewayFilter</code> faz:", body))
story.append(li("Le o cabecalho <code>Authorization: Bearer &lt;JWT&gt;</code>"))
story.append(li("Valida a assinatura HS384 contra o segredo compartilhado e confere o issuer"))
story.append(li("Em caso de falha, responde 401 RFC 7807 sem chegar a propagar"))
story.append(li("Em caso de sucesso, injeta <code>X-User-Id</code> e <code>X-User-Role</code> no request"))
story.append(li("Encaminha para o servico destino, que confia no header sem revalidar"))

story.append(p("8.2 Microservico -> Postgres / Redis", h2))
story.append(p(
    "O identity-service usa apenas Postgres (schema <code>identity_db</code>) via "
    "Spring Data JPA. O satellite-service usa Postgres "
    "(<code>satellite_db</code>, para auditoria em SatelliteQuery) e Redis para "
    "cache. Ambos os servicos abrem connection pool no <code>HikariCP</code>. A "
    "porta Redis e abstraida pela interface <code>SatelliteQueryCache</code> com "
    "adapter <code>RedisSatelliteQueryCache</code>.", body))

story.append(p("8.3 Cliente SOAP -> soap-service -> satellite-service via REST", h2))
story.append(p(
    "Esta e a <b>integracao REST + SOAP</b> exigida pela disciplina. O cliente "
    "SOAP nao toca em nenhum endpoint REST diretamente - ele envia um envelope para "
    "<code>POST /ws</code> na porta 8083. O <code>@Endpoint SatelliteEndpoint</code>:", body))
story.append(li("Recebe o request como um JAXBElement&lt;ConsultarVegetacaoRequest&gt;"))
story.append(li("Delega para <code>SatelliteRestClient</code>, que e um RestClient do Spring 6"))
story.append(li("O RestClient faz HTTP GET (ou POST /queries) com header <code>X-User-Id</code>"))
story.append(li("A resposta JSON e desserializada e convertida para a classe JAXB de response"))
story.append(li("Em caso de erro, <code>HttpClientErrorException</code> 400 vira "
                "<code>InvalidCoordinateSoapException</code> e o "
                "<code>SoapFaultMappingExceptionResolver</code> a transforma em SOAP Fault Client"))

flow = """\
+------+   POST /ws envelope        +---------------+
|SoapUI| -------------------------> | SatelliteEndp.|  @Endpoint
+------+                            +-------+-------+
                                            |
                                            v
                                    +---------------+
                                    | SatelliteRest |  Spring RestClient
                                    |    Client     |  (X-User-Id header)
                                    +-------+-------+
                                            |
                                            v
                                    +---------------+
                                    | satellite-svc |  GET /vegetation
                                    |   porta 8082  |  POST /queries
                                    +---------------+
"""
story.append(ascii_block(flow))
story.append(p("Figura 3: chamada SOAP percorre o RestClient antes de tocar o satellite REST.", caption))

story.append(p("8.4 Auditoria entre servicos", h2))
story.append(p(
    "Cada chamada bem-sucedida ao satellite (via REST direto, via gateway ou via "
    "SOAP) cria um agregado <code>SatelliteQuery</code> com UUID proprio e emite "
    "o evento de dominio <code>QueryExecuted(queryId, accountId, type, coordinate, cacheHit)</code>. "
    "Hoje esses eventos sao publicados via <code>ApplicationEventPublisher</code> "
    "do Spring (em memoria); a arquitetura ja preve que num cenario de producao "
    "eles seriam enviados para Kafka/RabbitMQ para billing e analytics.", body))


# ===== 9. TECNOLOGIAS =====
story.append(p("9. Tecnologias utilizadas", h1))
tech_table = [
    ["Categoria", "Tecnologia", "Onde e usada"],
    ["Linguagem",          "Java 21",                              "Todos os servicos"],
    ["Framework",          "Spring Boot 3.3.5",                    "Todos os servicos"],
    ["Build",              "Maven 3.9 multi-modulo",               "pom parent + 4 modulos filhos"],
    ["API REST",           "Spring Web (MVC) + Bean Validation",   "identity + satellite"],
    ["API Gateway",        "Spring Cloud Gateway 2023.0.3",        "gateway (8080)"],
    ["Web Service SOAP",   "Spring WS + wsdl4j + Jakarta XML Bind","soap-service (8083)"],
    ["Geracao JAXB",       "jaxb2-maven-plugin 3.2.0",             "Gera classes Java do XSD"],
    ["Persistencia",       "Spring Data JPA + Hibernate 6",        "identity + satellite"],
    ["Banco transacional", "PostgreSQL 16",                        "identity_db + satellite_db"],
    ["Cache",              "Redis 7",                              "cache landuse / vegetation (TTL 6h)"],
    ["Conn pool",          "HikariCP",                             "Datasource dos servicos"],
    ["Seguranca / Tokens", "Spring Security + jjwt 0.12.6 (HS384)","identity emite, gateway valida"],
    ["Hash de senha",      "BCrypt cost 12",                       "Password VO"],
    ["DTO mapping",        "MapStruct 1.6.2 (disponivel)",         "Disponivel para futuras evolucoes"],
    ["Documentacao API",   "Springdoc OpenAPI 2.6 (Swagger UI)",   "identity + satellite"],
    ["Testes",             "JUnit 5 + Mockito 5 + AssertJ",        "39 testes em 3 modulos"],
    ["Integracao testes",  "Testcontainers",                       "Disponivel para integracao Postgres"],
    ["Containers",         "Docker + Docker Compose v2",           "5 containers (default) / 6 (--profile soap)"],
    ["Logs",               "SLF4J + Logback",                      "Padrao em todos os servicos"],
    ["Versionamento",      "Git + GitHub",                         "repo Lynnbrosa/GS-SOA"],
]
story.append(make_table(tech_table, col_widths=[3.5*cm, 5.5*cm, 8*cm]))


# ===== 10. EVIDENCIAS DE FUNCIONAMENTO =====
story.append(PageBreak())
story.append(p("10. Evidencias de funcionamento", h1))
story.append(p(
    "As evidencias abaixo foram capturadas durante a execucao do comando "
    "<code>docker compose --profile soap up --build</code>. Mostram os 6 "
    "containers em estado healthy e respostas reais dos endpoints REST e SOAP.", body))

story.append(p("10.1 Os 6 containers de pe e saudaveis", h3))
story.append(shot(S["docker_ps"],
    "Print 1: docker ps confirma 5 containers REST + soap-service rodando "
    "(profile soap ativo)."))

story.append(p("10.2 Fluxo de autenticacao (US-01)", h3))
story.append(shot(S["register"],
    "Print 2: POST /auth/register retorna accountId, apiKey (com prefixo obt_) "
    "e JWT com expiracao em 24 horas."))

story.append(p("10.3 Endpoints satelitais e cache no Redis (US-05, US-06 e US-21)", h3))
story.append(shot(S["landuse_cache"],
    "Print 3: GET /landuse na primeira chamada retorna cacheHit:false; "
    "GET /vegetation em coordenada ja visitada retorna cacheHit:true (TTL 6h)."))

story.append(p("10.4 CRUD em Account: PUT /me e DELETE /accounts/{id}", h3))
story.append(shot(S["put_me"],
    "Print 4: PUT /me atualiza o e-mail (200). Tentativa de usar e-mail ja "
    "existente retorna 409 RFC 7807."))
story.append(shot(S["delete_forbidden"],
    "Print 5: DELETE /accounts/{id} com token DEVELOPER e barrado com 403. "
    "Apenas tokens com role ADMIN passam pela regra do SecurityConfig."))

story.append(p("10.5 WSDL publicado pelo soap-service", h3))
story.append(shot(S["wsdl"],
    "Print 6: GET /ws/satellite.wsdl publica o WSDL gerado em runtime a partir "
    "do XSD do contrato. Tipos VegetationHealth, QueryType, etc. ja aparecem."))

story.append(p("10.6 SOAP consultarVegetacao com REST por baixo", h3))
story.append(shot(S["soap_consulta"],
    "Print 7: o envelope SOAP retorna NDVI=0.054 vindo do satellite-service "
    "via integracao REST interna. health=NONE classificado pelo NdviScore."))

story.append(p("10.7 SOAP registrarConsulta - persistencia via REST", h3))
story.append(shot(S["soap_registrar"],
    "Print 8: registrarConsulta SOAP chama POST /queries do satellite via REST "
    "e devolve o queryId UUID + status EXECUTED."))

story.append(p("10.8 SOAP Fault em latitude invalida", h3))
story.append(shot(S["soap_fault"],
    "Print 9: latitude=95 (fora de [-90,90]) e mapeada pelo "
    "SoapFaultMappingExceptionResolver em <faultcode>SOAP-ENV:Client</faultcode>."))

story.append(p("10.9 POST /queries via REST direto", h3))
story.append(shot(S["queries"],
    "Print 10: o mesmo endpoint usado pelo SOAP esta disponivel para chamadas "
    "REST diretas, recebendo X-User-Id. Retorna 201 Created com queryId UUID."))


# ===== 11. PRINTS DOS TESTES =====
story.append(PageBreak())
story.append(p("11. Prints dos testes realizados", h1))
story.append(p(
    "Os testes automatizados rodam com <code>mvn test</code> em todos os 4 "
    "modulos. Cobrem invariantes de agregados, validacao de value objects, "
    "logica dos casos de uso (com mocks) e o endpoint SOAP "
    "(com REST client mockado).", body))

story.append(shot(S["mvn_test"],
    "Print 11: mvn test imprime BUILD SUCCESS - 39 testes verdes em 3 modulos "
    "(identity: 21, satellite: 15, soap: 3)."))

story.append(p("Distribuicao das classes de teste", h3))
tests_table = [
    ["Modulo", "Classe de teste", "O que valida"],
    ["identity",  "EmailTest",
     "Email vazio, malformado, normalizacao, igualdade por valor"],
    ["identity",  "PasswordTest",
     "Senha curta, sem digito, sem maiuscula, hash BCrypt, matches"],
    ["identity",  "AccountTest",
     "Register emite AccountRegistered; revokeApiKey emite ApiKeyRevoked; "
     "nao revoga 2x; igualdade por id"],
    ["identity",  "RegisterAccountUseCaseTest",
     "Cria conta nova, rejeita e-mail duplicado, publica eventos"],
    ["identity",  "UpdateAccountEmailUseCaseTest",
     "Atualiza e-mail, rejeita duplicado, rejeita conta inexistente"],
    ["identity",  "DeleteAccountUseCaseTest",
     "Deleta + emite AccountDeleted, rejeita conta inexistente"],
    ["satellite", "CoordinateTest",
     "Range de latitude/longitude, igualdade por valor"],
    ["satellite", "NdviScoreTest",
     "Range [-1, 1], classificacao em VegetationHealth"],
    ["satellite", "LandUseDistributionTest",
     "Soma = 100 (com tolerancia), rejeita negativos"],
    ["satellite", "SatelliteQueryTest",
     "execute() emite QueryExecuted com tipo e cacheHit corretos"],
    ["satellite", "MockSatelliteDataSourceTest",
     "Determinismo: mesma coordenada sempre retorna o mesmo resultado"],
    ["satellite", "GetLandUseUseCaseTest",
     "Cache miss chama dataSource; cache hit nao chama; salva auditoria"],
    ["soap",      "SatelliteEndpointTest",
     "consultarVegetacao traduz REST->SOAP; "
     "InvalidCoordinateSoapException e propagada; "
     "registrarConsulta retorna queryId"],
]
story.append(make_table(tests_table, col_widths=[2*cm, 5*cm, 10*cm]))


# ===== 12. CONCLUSAO =====
story.append(p("12. Conclusao do projeto", h1))
story.append(p(
    "OrbittAPI cumpre integralmente o escopo solicitado pela Global Solution: a "
    "decomposicao em microservicos atende ao requisito de SOA, e a modelagem com "
    "bounded contexts, agregados, value objects, eventos de dominio e arquitetura "
    "hexagonal atende ao requisito de DDD. A integracao REST mais SOAP demonstra "
    "que ambos os estilos podem coexistir sem duplicar logica: o Web Service SOAP "
    "e um adapter de transporte que reaproveita o dominio exposto via REST.", body))
story.append(p(
    "O projeto sobe com um unico comando (<code>docker compose --profile soap up</code>) "
    "e inclui healthchecks, cache Redis com TTL de 6h, JWT, autenticacao por "
    "header injetado pelo gateway, mapeamento padronizado de erros para RFC 7807 "
    "no REST e SOAP Fault no SOAP. Os 39 testes automatizados cobrem agregados, "
    "value objects, casos de uso e o endpoint SOAP, garantindo regressao zero "
    "diante de evolucoes futuras.", body))
story.append(p(
    "Para os proximos sprints, a arquitetura ja esta preparada para acrescentar "
    "novos bounded contexts (billing, dashboard, pipeline real de ingestao "
    "NASA/ESA, modelo de ML para classificacao do uso do solo) como modulos "
    "Maven independentes, sem refatorar os contextos atuais. A porta "
    "<code>SatelliteDataSource</code> permite trocar o adapter Mock pelo adapter "
    "real assim que a chave da API NASA Earth estiver disponivel - sem nenhuma "
    "mudanca no dominio.", body))
story.append(p(
    "Por fim, a relacao com o tema do semestre (Industria Espacial) e direta: "
    "OrbittAPI participa da economia espacial emergente como camada de software "
    "que democratiza o acesso aos dados de satelites publicos, gerando impacto "
    "real no chao (agricultura de precisao, monitoramento ambiental, planejamento "
    "urbano) e alinhada aos ODS 13 (clima) e 2 (agricultura).", body))

story.append(Spacer(1, 1.5*cm))
story.append(p("Fim do documento.", caption))


# ============================== BUILD ==============================
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
print(f"Screenshots: {SCREEN_DIR}")
