package br.com.orbittapi.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        UUID accountId,
        String email,
        String apiKey,
        String token,
        Instant expiresAt
) {}
