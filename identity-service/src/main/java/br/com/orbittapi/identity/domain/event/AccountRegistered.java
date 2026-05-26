package br.com.orbittapi.identity.domain.event;

import br.com.orbittapi.identity.domain.model.Email;

import java.time.Instant;
import java.util.UUID;

public record AccountRegistered(
        UUID accountId,
        Email email,
        Instant occurredOn
) implements DomainEvent {

    public static AccountRegistered now(UUID accountId, Email email) {
        return new AccountRegistered(accountId, email, Instant.now());
    }
}
