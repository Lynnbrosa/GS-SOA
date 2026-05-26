package br.com.orbittapi.identity.domain.exception;

public class ApiKeyAlreadyRevokedException extends DomainException {
    public ApiKeyAlreadyRevokedException(String message) {
        super(message);
    }
}
