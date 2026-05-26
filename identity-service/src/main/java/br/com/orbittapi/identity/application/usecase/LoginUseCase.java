package br.com.orbittapi.identity.application.usecase;

import br.com.orbittapi.identity.application.dto.AuthResponse;
import br.com.orbittapi.identity.application.dto.LoginCommand;
import br.com.orbittapi.identity.application.port.TokenProvider;
import br.com.orbittapi.identity.domain.exception.InvalidCredentialsException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.Email;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCase.class);

    private final AccountRepository accountRepository;
    private final TokenProvider tokenProvider;

    public LoginUseCase(AccountRepository accountRepository, TokenProvider tokenProvider) {
        this.accountRepository = accountRepository;
        this.tokenProvider = tokenProvider;
    }

    @Transactional(readOnly = true)
    public AuthResponse execute(LoginCommand command) {
        Email email = new Email(command.email());

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!account.authenticatesWith(command.password())) {
            log.warn("Failed login attempt for email={}", email);
            throw new InvalidCredentialsException();
        }

        TokenProvider.IssuedToken token = tokenProvider.issue(account);
        log.info("Login successful accountId={}", account.id());

        return new AuthResponse(
                account.id(),
                account.email().value(),
                account.apiKey().value(),
                token.token(),
                token.expiresAt()
        );
    }
}
