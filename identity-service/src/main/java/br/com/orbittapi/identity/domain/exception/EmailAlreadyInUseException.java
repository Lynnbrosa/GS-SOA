package br.com.orbittapi.identity.domain.exception;

public class EmailAlreadyInUseException extends DomainException {
    public EmailAlreadyInUseException(String email) {
        super("Email already in use: " + email);
    }
}
