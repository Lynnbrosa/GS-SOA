package br.com.orbittapi.identity.domain.model;

import br.com.orbittapi.identity.domain.event.AccountRegistered;
import br.com.orbittapi.identity.domain.event.ApiKeyRevoked;
import br.com.orbittapi.identity.domain.event.DomainEvent;
import br.com.orbittapi.identity.domain.exception.ApiKeyAlreadyRevokedException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final Email email;
    private Password password;
    private ApiKey apiKey;
    private final AccountRole role;
    private final Instant createdAt;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Account(UUID id, Email email, Password password, ApiKey apiKey,
                    AccountRole role, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.email = Objects.requireNonNull(email, "email");
        this.password = Objects.requireNonNull(password, "password");
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.role = Objects.requireNonNull(role, "role");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Account register(Email email, String rawPassword, AccountRole role) {
        Account account = new Account(
                UUID.randomUUID(),
                email,
                Password.fromRaw(rawPassword),
                ApiKey.generate(),
                role,
                Instant.now()
        );
        account.domainEvents.add(AccountRegistered.now(account.id, account.email));
        return account;
    }

    public static Account rehydrate(UUID id, Email email, Password password,
                                    ApiKey apiKey, AccountRole role, Instant createdAt) {
        return new Account(id, email, password, apiKey, role, createdAt);
    }

    public void revokeApiKey() {
        if (apiKey.isRevoked()) {
            throw new ApiKeyAlreadyRevokedException("API key for account " + id + " is already revoked");
        }
        this.apiKey = apiKey.revoke();
        domainEvents.add(ApiKeyRevoked.now(id, apiKey.value()));
    }

    public boolean authenticatesWith(String rawPassword) {
        return password.matches(rawPassword);
    }

    public UUID id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public Password password() {
        return password;
    }

    public ApiKey apiKey() {
        return apiKey;
    }

    public AccountRole role() {
        return role;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copy = List.copyOf(domainEvents);
        domainEvents.clear();
        return copy;
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
