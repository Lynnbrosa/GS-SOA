package br.com.orbittapi.identity.application;

import br.com.orbittapi.identity.application.dto.AuthResponse;
import br.com.orbittapi.identity.application.dto.RegisterAccountCommand;
import br.com.orbittapi.identity.application.port.DomainEventPublisher;
import br.com.orbittapi.identity.application.port.TokenProvider;
import br.com.orbittapi.identity.application.usecase.RegisterAccountUseCase;
import br.com.orbittapi.identity.domain.exception.EmailAlreadyInUseException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.Email;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterAccountUseCaseTest {

    private AccountRepository accountRepository;
    private TokenProvider tokenProvider;
    private DomainEventPublisher eventPublisher;
    private RegisterAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        tokenProvider = mock(TokenProvider.class);
        eventPublisher = mock(DomainEventPublisher.class);
        useCase = new RegisterAccountUseCase(accountRepository, tokenProvider, eventPublisher);
    }

    @Test
    void registersNewAccountAndIssuesToken() {
        when(accountRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenProvider.issue(any(Account.class)))
                .thenReturn(new TokenProvider.IssuedToken("token-123", Instant.now().plusSeconds(86400)));

        AuthResponse response = useCase.execute(new RegisterAccountCommand("dev@orbittapi.dev", "Abcdefg1"));

        assertThat(response.email()).isEqualTo("dev@orbittapi.dev");
        assertThat(response.token()).isEqualTo("token-123");
        assertThat(response.apiKey()).startsWith("obt_");

        verify(accountRepository).save(any(Account.class));
        verify(eventPublisher, times(1)).publishAll(any());
    }

    @Test
    void rejectsDuplicatedEmail() {
        when(accountRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new RegisterAccountCommand("dev@orbittapi.dev", "Abcdefg1")))
                .isInstanceOf(EmailAlreadyInUseException.class);
    }

    @Test
    void readsExistingByEmailReturnsEmptyWhenNotFound() {
        when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
        assertThat(accountRepository.findByEmail(new Email("none@x.com"))).isEmpty();
    }
}
