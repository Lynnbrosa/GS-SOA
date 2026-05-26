package br.com.orbittapi.identity.application.usecase;

import br.com.orbittapi.identity.application.dto.AccountProfileResponse;
import br.com.orbittapi.identity.domain.exception.AccountNotFoundException;
import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetMyProfileUseCase {

    private final AccountRepository accountRepository;

    public GetMyProfileUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public AccountProfileResponse execute(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        return new AccountProfileResponse(
                account.id(),
                account.email().value(),
                account.role().name(),
                account.apiKey().value(),
                account.apiKey().isRevoked(),
                account.createdAt()
        );
    }
}
