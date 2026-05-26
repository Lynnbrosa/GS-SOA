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
public class RevokeApiKeyUseCase {

    private static final Logger log = LoggerFactory.getLogger(RevokeApiKeyUseCase.class);

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeApiKeyUseCase(AccountRepository accountRepository, DomainEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(UUID targetAccountId) {
        Account account = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + targetAccountId));

        account.revokeApiKey();
        Account saved = accountRepository.save(account);
        eventPublisher.publishAll(saved.pullDomainEvents());
        log.info("API key revoked accountId={}", targetAccountId);
    }
}
