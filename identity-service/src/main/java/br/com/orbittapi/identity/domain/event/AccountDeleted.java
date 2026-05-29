package br.com.orbittapi.identity.domain.event;

import br.com.orbittapi.identity.domain.model.Email;

import java.time.Instant;
import java.util.UUID;

public record AccountDeleted(
        UUID accountId,
        Email email,
        Instant occurredOn
) implements DomainEvent {

    public static AccountDeleted now(UUID accountId, Email email) {
        return new AccountDeleted(accountId, email, Instant.now());
    }
}
