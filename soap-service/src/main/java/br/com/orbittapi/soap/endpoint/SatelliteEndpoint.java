package br.com.orbittapi.soap.endpoint;

import br.com.orbittapi.soap.client.SatelliteRestClient;
import br.com.orbittapi.soap.client.dto.RegisterQueryResponse;
import br.com.orbittapi.soap.client.dto.VegetationDto;
import br.com.orbittapi.soap.generated.ConsultarVegetacaoRequest;
import br.com.orbittapi.soap.generated.ConsultarVegetacaoResponse;
import br.com.orbittapi.soap.generated.RegistrarConsultaRequest;
import br.com.orbittapi.soap.generated.RegistrarConsultaResponse;
import br.com.orbittapi.soap.generated.SatelliteSource;
import br.com.orbittapi.soap.generated.VegetationHealth;
import jakarta.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.time.Instant;
import java.time.LocalDate;
import java.util.GregorianCalendar;

@Endpoint
public class SatelliteEndpoint {

    private static final Logger log = LoggerFactory.getLogger(SatelliteEndpoint.class);
    public static final String NAMESPACE = "http://orbittapi.dev/soap/satellite";

    private final SatelliteRestClient restClient;

    public SatelliteEndpoint(SatelliteRestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Operacao 1 (consulta). Resolve um NDVI usando o satellite-service REST.
     * Usuario tecnico SOAP nao tem JWT; usamos um id de sistema fixo.
     */
    @PayloadRoot(namespace = NAMESPACE, localPart = "ConsultarVegetacaoRequest")
    @ResponsePayload
    public JAXBElement<ConsultarVegetacaoResponse> consultarVegetacao(
            @RequestPayload JAXBElement<ConsultarVegetacaoRequest> requestElement) {
        ConsultarVegetacaoRequest req = requestElement.getValue();
        log.info("SOAP consultarVegetacao lat={} lng={}", req.getLatitude(), req.getLongitude());

        VegetationDto dto = restClient.getVegetation(
                SoapSystemIdentity.systemUserId(), req.getLatitude(), req.getLongitude());

        ConsultarVegetacaoResponse resp = new ConsultarVegetacaoResponse();
        resp.setLatitude(dto.latitude());
        resp.setLongitude(dto.longitude());
        resp.setNdvi(dto.ndvi());
        resp.setHealth(VegetationHealth.fromValue(dto.health()));
        resp.setImageDate(toXmlDate(dto.imageDate()));
        resp.setSource(SatelliteSource.fromValue(dto.source()));

        return new jakarta.xml.bind.JAXBElement<>(
                new javax.xml.namespace.QName(NAMESPACE, "ConsultarVegetacaoResponse"),
                ConsultarVegetacaoResponse.class, resp);
    }

    /**
     * Operacao 2 (cadastro/processamento). Persiste um SatelliteQuery no satellite-service.
     */
    @PayloadRoot(namespace = NAMESPACE, localPart = "RegistrarConsultaRequest")
    @ResponsePayload
    public JAXBElement<RegistrarConsultaResponse> registrarConsulta(
            @RequestPayload JAXBElement<RegistrarConsultaRequest> requestElement) {
        RegistrarConsultaRequest req = requestElement.getValue();
        log.info("SOAP registrarConsulta accountId={} tipo={} lat={} lng={}",
                req.getAccountId(), req.getTipo(), req.getLatitude(), req.getLongitude());

        RegisterQueryResponse dto = restClient.registerQuery(
                req.getAccountId(),
                req.getTipo().value(),
                req.getLatitude(),
                req.getLongitude());

        RegistrarConsultaResponse resp = new RegistrarConsultaResponse();
        resp.setQueryId(dto.queryId());
        resp.setStatus(dto.status());
        resp.setExecutedAt(toXmlDateTime(dto.executedAt()));

        return new jakarta.xml.bind.JAXBElement<>(
                new javax.xml.namespace.QName(NAMESPACE, "RegistrarConsultaResponse"),
                RegistrarConsultaResponse.class, resp);
    }

    // -------- helpers de conversao para tipos XML --------
    private static XMLGregorianCalendar toXmlDate(LocalDate d) {
        try {
            GregorianCalendar gc = GregorianCalendar.from(
                    d.atStartOfDay(java.time.ZoneOffset.UTC));
            XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            cal.setHour(DatatypeConstants.FIELD_UNDEFINED);
            cal.setMinute(DatatypeConstants.FIELD_UNDEFINED);
            cal.setSecond(DatatypeConstants.FIELD_UNDEFINED);
            cal.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
            cal.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
            return cal;
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static XMLGregorianCalendar toXmlDateTime(Instant i) {
        try {
            GregorianCalendar gc = GregorianCalendar.from(
                    i.atZone(java.time.ZoneOffset.UTC));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
}
