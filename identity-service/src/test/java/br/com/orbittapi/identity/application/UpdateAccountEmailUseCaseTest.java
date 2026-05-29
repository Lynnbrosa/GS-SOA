package br.com.orbittapi.identity.application;

import br.com.orbittapi.identity.application.dto.AccountProfileResponse;
import br.com.orbittapi.identity.application.dto.UpdateEmailCommand;
import br.com.orbittapi.identity.application.port.DomainEventPublisher;
import br.com.orbittapi.identity.application.usecase.UpdateAccountEmailUseCase;
import br.com.orbittapi.identity.domain.exception.AccountNotFoundException;
import br.com.orbittapi.identity.domain.exception.EmailAlreadyInUseException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.AccountRole;
import br.com.orbittapi.identity.domain.model.Email;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateAccountEmailUseCaseTest {

    private AccountRepository repo;
    private DomainEventPublisher publisher;
    private UpdateAccountEmailUseCase useCase;

    @BeforeEach
    void setUp() {
        repo = mock(AccountRepository.class);
        publisher = mock(DomainEventPublisher.class);
        useCase = new UpdateAccountEmailUseCase(repo, publisher);
    }

    @Test
    void updatesEmailWhenNewIsFree() {
        Account account = Account.register(new Email("old@orbittapi.dev"), "Abcdefg1", AccountRole.DEVELOPER);
        account.pullDomainEvents();
        when(repo.findById(account.id())).thenReturn(Optional.of(account));
        when(repo.existsByEmail(new Email("new@orbittapi.dev"))).thenReturn(false);
        when(repo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountProfileResponse response = useCase.execute(account.id(), new UpdateEmailCommand("new@orbittapi.dev"));

        assertThat(response.email()).isEqualTo("new@orbittapi.dev");
        verify(repo).save(any(Account.class));
        verify(publisher, times(1)).publishAll(any());
    }

    @Test
    void rejectsDuplicatedEmail() {
        Account account = Account.register(new Email("old@orbittapi.dev"), "Abcdefg1", AccountRole.DEVELOPER);
        when(repo.findById(account.id())).thenReturn(Optional.of(account));
        when(repo.existsByEmail(new Email("dup@orbittapi.dev"))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(account.id(), new UpdateEmailCommand("dup@orbittapi.dev")))
                .isInstanceOf(EmailAlreadyInUseException.class);
    }

    @Test
    void rejectsWhenAccountNotFound() {
        UUID missing = UUID.randomUUID();
        when(repo.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(missing, new UpdateEmailCommand("any@x.com")))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
