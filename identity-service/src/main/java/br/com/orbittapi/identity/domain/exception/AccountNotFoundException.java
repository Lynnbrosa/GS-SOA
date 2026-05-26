package br.com.orbittapi.identity.domain.exception;

public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
