package br.com.orbittapi.identity.domain;

import br.com.orbittapi.identity.domain.event.AccountRegistered;
import br.com.orbittapi.identity.domain.event.ApiKeyRevoked;
import br.com.orbittapi.identity.domain.event.DomainEvent;
import br.com.orbittapi.identity.domain.exception.ApiKeyAlreadyRevokedException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.AccountRole;
import br.com.orbittapi.identity.domain.model.Email;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    @Test
    void registerGeneratesIdApiKeyAndEmitsEvent() {
        Account account = Account.register(new Email("dev@orbittapi.dev"), "Abcdefg1", AccountRole.DEVELOPER);

        assertThat(account.id()).isNotNull();
        assertThat(account.apiKey().value()).startsWith("obt_");
        assertThat(account.apiKey().isRevoked()).isFalse();

        List<DomainEvent> events = account.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(AccountRegistered.class);
    }

    @Test
    void authenticatesWithCorrectPassword() {
        Account account = Account.register(new Email("dev@orbittapi.dev"), "Abcdefg1", AccountRole.DEVELOPER);
        assertThat(account.authenticatesWith("Abcdefg1")).isTrue();
        assertThat(account.authenticatesWith("wrong")).isFalse();
    }

    @Test
    void revokeApiKeyEmitsEventAndMarksRevoked() {
        Account account = Account.register(new Email("dev@orbittapi.dev"), "Abcdefg1", AccountRole.DEVELOPER);
        account.pullDomainEvents();

        account.revokeApiKey();

        assertThat(account.apiKey().isRevoked()).isTrue();
        List<DomainEvent> events = account.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ApiKeyRevoked.class);
    }

    @Test
    void revokingAlreadyRevokedKeyThrows() {
        Account account = Account.register(new Email("dev@orbittapi.dev"), "Abcdefg1", AccountRole.DEVELOPER);
        account.revokeApiKey();

        assertThatThrownBy(account::revokeApiKey)
                .isInstanceOf(ApiKeyAlreadyRevokedException.class);
    }

    @Test
    void equalityByIdentity() {
        Account a = Account.register(new Email("a@b.com"), "Abcdefg1", AccountRole.DEVELOPER);
        Account b = Account.rehydrate(a.id(), a.email(), a.password(), a.apiKey(), a.role(), a.createdAt());
        assertThat(a).isEqualTo(b);
    }
}
