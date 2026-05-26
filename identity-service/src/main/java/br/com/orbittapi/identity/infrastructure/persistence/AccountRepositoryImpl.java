package br.com.orbittapi.identity.infrastructure.persistence;

import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.Email;
import br.com.orbittapi.identity.domain.repository.AccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository jpa;

    public AccountRepositoryImpl(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity saved = jpa.save(AccountPersistenceMapper.toEntity(account));
        return AccountPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpa.findById(id).map(AccountPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(AccountPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return jpa.existsByEmail(email.value());
    }
}
