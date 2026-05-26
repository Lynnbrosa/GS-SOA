package br.com.orbittapi.identity.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyRevoked(
        UUID accountId,
        String apiKeyValue,
        Instant occurredOn
) implements DomainEvent {

    public static ApiKeyRevoked now(UUID accountId, String apiKeyValue) {
        return new ApiKeyRevoked(accountId, apiKeyValue, Instant.now());
    }
}
