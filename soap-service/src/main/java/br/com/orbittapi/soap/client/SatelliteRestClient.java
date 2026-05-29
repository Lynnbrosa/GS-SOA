package br.com.orbittapi.soap.client;

import br.com.orbittapi.soap.client.dto.RegisterQueryRequest;
import br.com.orbittapi.soap.client.dto.RegisterQueryResponse;
import br.com.orbittapi.soap.client.dto.VegetationDto;
import br.com.orbittapi.soap.exception.InvalidCoordinateSoapException;
import br.com.orbittapi.soap.exception.SatelliteUnavailableSoapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;

/**
 * Cliente REST que o Endpoint SOAP usa para falar com o satellite-service.
 * E aqui que vive a integracao REST <-> SOAP exigida pela disciplina:
 * o SOAP nao reimplementa o dominio, apenas o consome via HTTP.
 */
@Component
public class SatelliteRestClient {

    private static final Logger log = LoggerFactory.getLogger(SatelliteRestClient.class);
    private static final String USER_HEADER = "X-User-Id";

    private final RestClient client;

    public SatelliteRestClient(RestClient satelliteRestClient) {
        this.client = satelliteRestClient;
    }

    public VegetationDto getVegetation(String userId, double lat, double lng) {
        try {
            VegetationDto dto = client.get()
                    .uri(uri -> uri.path("/vegetation").queryParam("lat", lat).queryParam("lng", lng).build())
                    .header(USER_HEADER, userId)
                    .retrieve()
                    .body(VegetationDto.class);

            if (dto == null) {
                throw new SatelliteUnavailableSoapException("Satellite returned empty body");
            }
            log.info("REST GET /vegetation lat={} lng={} returned ndvi={}", lat, lng, dto.ndvi());
            return dto;
        } catch (HttpClientErrorException ex) {
            handleClientError(ex, "consultarVegetacao");
            throw ex; // unreachable, mas o compilador exige
        } catch (ResourceAccessException ex) {
            throw new SatelliteUnavailableSoapException(
                    "Satellite service unreachable: " + ex.getMessage(), ex);
        }
    }

    public RegisterQueryResponse registerQuery(String userId, String type, double lat, double lng) {
        try {
            RegisterQueryResponse dto = client.post()
                    .uri("/queries")
                    .header(USER_HEADER, userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new RegisterQueryRequest(type, lat, lng))
                    .retrieve()
                    .body(RegisterQueryResponse.class);

            if (dto == null) {
                throw new SatelliteUnavailableSoapException("Satellite returned empty body");
            }
            log.info("REST POST /queries userId={} type={} queryId={}", userId, type, dto.queryId());
            return dto;
        } catch (HttpClientErrorException ex) {
            handleClientError(ex, "registrarConsulta");
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new SatelliteUnavailableSoapException(
                    "Satellite service unreachable: " + ex.getMessage(), ex);
        }
    }

    private void handleClientError(HttpClientErrorException ex, String op) {
        HttpStatusCode status = ex.getStatusCode();
        String body = ex.getResponseBodyAsString();
        log.warn("REST {} returned {} body={}", op, status, body);

        if (status.value() == 400) {
            throw new InvalidCoordinateSoapException(
                    "Invalid input for " + op + ": " + extractDetail(body));
        }
        if (status.value() == 401 || status.value() == 403) {
            throw new InvalidCoordinateSoapException(
                    "Unauthorized call to satellite-service for " + op);
        }
        throw new SatelliteUnavailableSoapException(
                "Satellite returned " + status + " for " + op + ": " + extractDetail(body));
    }

    private String extractDetail(String body) {
        if (body == null) return "";
        int idx = body.indexOf("\"detail\":\"");
        if (idx < 0) return body;
        int start = idx + "\"detail\":\"".length();
        int end = body.indexOf("\"", start);
        return end > start ? body.substring(start, end) : body;
    }
}
