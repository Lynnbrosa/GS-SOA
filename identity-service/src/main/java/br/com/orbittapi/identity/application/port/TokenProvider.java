package br.com.orbittapi.identity.application.port;

import br.com.orbittapi.identity.domain.model.Account;

import java.time.Instant;

public interface TokenProvider {

    IssuedToken issue(Account account);

    record IssuedToken(String token, Instant expiresAt) {}
}
