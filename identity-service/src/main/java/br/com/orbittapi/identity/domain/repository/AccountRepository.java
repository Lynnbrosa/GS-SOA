package br.com.orbittapi.identity.domain.repository;

import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.Email;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID id);

    Optional<Account> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void deleteById(UUID id);
}
