package br.com.orbittapi.identity.application;

import br.com.orbittapi.identity.application.port.DomainEventPublisher;
import br.com.orbittapi.identity.application.usecase.DeleteAccountUseCase;
import br.com.orbittapi.identity.domain.event.AccountDeleted;
import br.com.orbittapi.identity.domain.event.DomainEvent;
import br.com.orbittapi.identity.domain.exception.AccountNotFoundException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.AccountRole;
import br.com.orbittapi.identity.domain.model.Email;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteAccountUseCaseTest {

    private AccountRepository repo;
    private DomainEventPublisher publisher;
    private DeleteAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        repo = mock(AccountRepository.class);
        publisher = mock(DomainEventPublisher.class);
        useCase = new DeleteAccountUseCase(repo, publisher);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void deletesAccountAndEmitsEvent() {
        Account account = Account.register(new Email("doomed@orbittapi.dev"), "Abcdefg1", AccountRole.DEVELOPER);
        account.pullDomainEvents();
        when(repo.findById(account.id())).thenReturn(Optional.of(account));

        useCase.execute(account.id());

        verify(repo).deleteById(account.id());

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(publisher).publishAll(captor.capture());
        List<DomainEvent> captured = (List<DomainEvent>) captor.getValue();
        assertThat(captured).hasSize(1);
        assertThat(captured.get(0)).isInstanceOf(AccountDeleted.class);
    }

    @Test
    void rejectsWhenAccountNotFound() {
        UUID missing = UUID.randomUUID();
        when(repo.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(missing))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
