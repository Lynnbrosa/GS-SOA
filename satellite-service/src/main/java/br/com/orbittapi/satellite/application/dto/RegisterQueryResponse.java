package br.com.orbittapi.satellite.application.dto;

import java.time.Instant;
import java.util.UUID;

public record RegisterQueryResponse(
        UUID queryId,
        String type,
        double latitude,
        double longitude,
        String status,
        Instant executedAt
) {}
