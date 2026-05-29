package br.com.orbittapi.soap.endpoint;

import br.com.orbittapi.soap.client.SatelliteRestClient;
import br.com.orbittapi.soap.client.dto.RegisterQueryResponse;
import br.com.orbittapi.soap.client.dto.VegetationDto;
import br.com.orbittapi.soap.exception.InvalidCoordinateSoapException;
import br.com.orbittapi.soap.generated.ConsultarVegetacaoRequest;
import br.com.orbittapi.soap.generated.ConsultarVegetacaoResponse;
import br.com.orbittapi.soap.generated.QueryType;
import br.com.orbittapi.soap.generated.RegistrarConsultaRequest;
import br.com.orbittapi.soap.generated.RegistrarConsultaResponse;
import jakarta.xml.bind.JAXBElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SatelliteEndpointTest {

    private static final String NS = "http://orbittapi.dev/soap/satellite";

    private SatelliteRestClient restClient;
    private SatelliteEndpoint endpoint;

    @BeforeEach
    void setUp() {
        restClient = mock(SatelliteRestClient.class);
        endpoint = new SatelliteEndpoint(restClient);
    }

    @Test
    void consultarVegetacaoTraduzRespostaRestParaSoap() {
        VegetationDto dto = new VegetationDto(
                -23.5, -46.6, 0.42, "MODERATE", LocalDate.of(2026, 5, 10), "MOCK", false);
        when(restClient.getVegetation(anyString(), eq(-23.5), eq(-46.6))).thenReturn(dto);

        ConsultarVegetacaoRequest req = new ConsultarVegetacaoRequest();
        req.setLatitude(-23.5);
        req.setLongitude(-46.6);
        JAXBElement<ConsultarVegetacaoRequest> in = new JAXBElement<>(
                new QName(NS, "ConsultarVegetacaoRequest"), ConsultarVegetacaoRequest.class, req);

        JAXBElement<ConsultarVegetacaoResponse> out = endpoint.consultarVegetacao(in);

        ConsultarVegetacaoResponse resp = out.getValue();
        assertThat(resp.getNdvi()).isEqualTo(0.42);
        assertThat(resp.getHealth().value()).isEqualTo("MODERATE");
        assertThat(resp.getSource().value()).isEqualTo("MOCK");
        assertThat(resp.getLatitude()).isEqualTo(-23.5);
    }

    @Test
    void consultarVegetacaoPropagaInvalidCoordinate() {
        when(restClient.getVegetation(anyString(), anyDouble(), anyDouble()))
                .thenThrow(new InvalidCoordinateSoapException("lat out of range"));

        ConsultarVegetacaoRequest req = new ConsultarVegetacaoRequest();
        req.setLatitude(-23.5);
        req.setLongitude(-46.6);
        JAXBElement<ConsultarVegetacaoRequest> in = new JAXBElement<>(
                new QName(NS, "ConsultarVegetacaoRequest"), ConsultarVegetacaoRequest.class, req);

        assertThatThrownBy(() -> endpoint.consultarVegetacao(in))
                .isInstanceOf(InvalidCoordinateSoapException.class);
    }

    @Test
    void registrarConsultaPersisteESalvaQueryId() {
        Instant now = Instant.parse("2026-05-26T10:00:00Z");
        RegisterQueryResponse dto = new RegisterQueryResponse(
                "11111111-2222-3333-4444-555555555555",
                "VEGETATION",
                -23.5, -46.6,
                "EXECUTED", now);
        when(restClient.registerQuery(eq("acct-1"), eq("VEGETATION"), anyDouble(), anyDouble()))
                .thenReturn(dto);

        RegistrarConsultaRequest req = new RegistrarConsultaRequest();
        req.setAccountId("acct-1");
        req.setTipo(QueryType.VEGETATION);
        req.setLatitude(-23.5);
        req.setLongitude(-46.6);
        JAXBElement<RegistrarConsultaRequest> in = new JAXBElement<>(
                new QName(NS, "RegistrarConsultaRequest"), RegistrarConsultaRequest.class, req);

        JAXBElement<RegistrarConsultaResponse> out = endpoint.registrarConsulta(in);

        RegistrarConsultaResponse resp = out.getValue();
        assertThat(resp.getQueryId()).isEqualTo("11111111-2222-3333-4444-555555555555");
        assertThat(resp.getStatus()).isEqualTo("EXECUTED");
        assertThat(resp.getExecutedAt()).isNotNull();
    }
}
