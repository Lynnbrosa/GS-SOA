package br.com.orbittapi.identity.application.usecase;

import br.com.orbittapi.identity.application.dto.AccountProfileResponse;
import br.com.orbittapi.identity.application.dto.UpdateEmailCommand;
import br.com.orbittapi.identity.application.port.DomainEventPublisher;
import br.com.orbittapi.identity.domain.exception.AccountNotFoundException;
import br.com.orbittapi.identity.domain.exception.EmailAlreadyInUseException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.Email;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateAccountEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateAccountEmailUseCase.class);

    private final AccountRepository accountRepository;
    private final DomainEventPublisher eventPublisher;

    public UpdateAccountEmailUseCase(AccountRepository accountRepository,
                                    DomainEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AccountProfileResponse execute(UUID accountId, UpdateEmailCommand command) {
        Email newEmail = new Email(command.email());

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        if (!account.email().equals(newEmail) && accountRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyInUseException(newEmail.value());
        }

        account.changeEmail(newEmail);
        Account saved = accountRepository.save(account);
        eventPublisher.publishAll(saved.pullDomainEvents());

        log.info("Email updated accountId={} newEmail={}", accountId, newEmail);

        return new AccountProfileResponse(
                saved.id(),
                saved.email().value(),
                saved.role().name(),
                saved.apiKey().value(),
                saved.apiKey().isRevoked(),
                saved.createdAt()
        );
    }
}
