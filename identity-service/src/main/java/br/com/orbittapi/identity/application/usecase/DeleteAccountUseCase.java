package br.com.orbittapi.identity.application.usecase;

import br.com.orbittapi.identity.application.port.DomainEventPublisher;
import br.com.orbittapi.identity.domain.exception.AccountNotFoundException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeleteAccountUseCase.class);

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;

    public DeleteAccountUseCase(AccountRepository accountRepository,
                               DomainEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        account.markDeleted();
        accountRepository.deleteById(accountId);
        eventPublisher.publishAll(account.pullDomainEvents());

        log.info("Account deleted id={} email={}", accountId, account.email());
    }
}
