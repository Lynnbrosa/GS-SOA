package br.com.orbittapi.identity.infrastructure.persistence;

import br.com.orbittapi.identity.domain.model.Account;
import br.com.orbittapi.identity.domain.model.ApiKey;
import br.com.orbittapi.identity.domain.model.Email;
import br.com.orbittapi.identity.domain.model.Password;

final class AccountPersistenceMapper {

    private AccountPersistenceMapper() {
    }

    static AccountJpaEntity toEntity(Account account) {
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setId(account.id());
        entity.setEmail(account.email().value());
        entity.setPasswordHash(account.password().hash());
        entity.setApiKey(account.apiKey().value());
        entity.setApiKeyRevoked(account.apiKey().isRevoked());
        entity.setRole(account.role());
        entity.setCreatedAt(account.createdAt());
        return entity;
    }

    static Account toDomain(AccountJpaEntity entity) {
        return Account.rehydrate(
                entity.getId(),
                new Email(entity.getEmail()),
                Password.fromHash(entity.getPasswordHash()),
                ApiKey.rehydrate(entity.getApiKey(), entity.isApiKeyRevoked()),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }
}
