package br.com.orbittapi.identity.application.usecase;

import br.com.orbittapi.identity.application.dto.AuthResponse;
import br.com.orbittapi.identity.application.dto.RegisterAccountCommand;
import br.com.orbittapi.identity.application.port.DomainEventPublisher;
import br.com.orbittapi.identity.application.port.TokenProvider;
import br.com.orbittapi.identity.domain.exception.EmailAlreadyInUseException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.AccountRole;
import br.com.orbittapi.identity.domain.model.Email;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterAccountUseCase.class);

    private final AccountRepository accountRepository;
    private final TokenProvider tokenProvider;
    private final DomainEventPublisher eventPublisher;

    public RegisterAccountUseCase(AccountRepository accountRepository,
                                  TokenProvider tokenProvider,
                                  DomainEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.tokenProvider = tokenProvider;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResponse execute(RegisterAccountCommand command) {
        Email email = new Email(command.email());

        if (accountRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email.value());
        }

        Account account = Account.register(email, command.password(), AccountRole.DEVELOPER);
        Account saved = accountRepository.save(account);

        eventPublisher.publishAll(saved.pullDomainEvents());

        TokenProvider.IssuedToken token = tokenProvider.issue(saved);
        log.info("Account registered id={} email={}", saved.id(), saved.email());

        return new AuthResponse(
                saved.id(),
                saved.email().value(),
                saved.apiKey().value(),
                token.token(),
                token.expiresAt()
        );
    }
}
