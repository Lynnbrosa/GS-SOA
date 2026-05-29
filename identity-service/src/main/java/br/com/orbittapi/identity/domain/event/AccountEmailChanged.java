package br.com.orbittapi.identity.domain.event;

import br.com.orbittapi.identity.domain.model.Email;

import java.time.Instant;
import java.util.UUID;

public record AccountEmailChanged(
        UUID accountId,
        Email previousEmail,
        Email newEmail,
        Instant occurredOn
) implements DomainEvent {

    public static AccountEmailChanged now(UUID accountId, Email previous, Email current) {
        return new AccountEmailChanged(accountId, previous, current, Instant.now());
    }
}
