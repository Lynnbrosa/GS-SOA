package br.com.orbittapi.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AccountProfileResponse(
        UUID id,
        String email,
        String role,
        String apiKey,
        boolean apiKeyRevoked,
        Instant createdAt
) {}
