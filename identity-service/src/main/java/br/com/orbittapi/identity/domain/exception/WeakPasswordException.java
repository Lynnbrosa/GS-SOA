package br.com.orbittapi.identity.domain.exception;

public class WeakPasswordException extends DomainException {
    public WeakPasswordException(String message) {
        super(message);
    }
}
