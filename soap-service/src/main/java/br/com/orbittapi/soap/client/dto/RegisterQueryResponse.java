package br.com.orbittapi.soap.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterQueryResponse(
        String queryId,
        String type,
        double latitude,
        double longitude,
        String status,
        Instant executedAt
) {}
