package br.com.orbittapi.identity.interfaces.rest;

import br.com.orbittapi.identity.domain.exception.AccountNotFoundException;
import br.com.orbittapi.identity.domain.exception.ApiKeyAlreadyRevokedException;
import br.com.orbittapi.identity.domain.exception.EmailAlreadyInUseException;
import br.com.orbittapi.identity.domain.exception.InvalidCredentialsException;
import br.com.orbittapi.identity.domain.exception.InvalidEmailException;
import br.com.orbittapi.identity.domain.exception.WeakPasswordException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_TYPE = "https://orbittapi.dev/errors/";

    @ExceptionHandler(InvalidEmailException.class)
    public ProblemDetail handleInvalidEmail(InvalidEmailException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "invalid-email", "Invalid email", ex.getMessage(), req);
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ProblemDetail handleWeakPassword(WeakPasswordException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "weak-password", "Weak password", ex.getMessage(), req);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ProblemDetail handleEmailInUse(EmailAlreadyInUseException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "email-in-use", "Email already in use", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "invalid-credentials", "Invalid credentials", ex.getMessage(), req);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "account-not-found", "Account not found", ex.getMessage(), req);
    }

    @ExceptionHandler(ApiKeyAlreadyRevokedException.class)
    public ProblemDetail handleAlreadyRevoked(ApiKeyAlreadyRevokedException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "api-key-already-revoked", "API key already revoked", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, "validation-failed", "Validation failed", details, req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuth(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "unauthorized", "Unauthorized", ex.getMessage(), req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "forbidden", "Forbidden", ex.getMessage(), req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal", "Internal error",
                "Unexpected error processing the request", req);
    }

    private ProblemDetail build(HttpStatus status, String slug, String title, String detail, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(BASE_TYPE + slug));
        pd.setTitle(title);
        pd.setInstance(URI.create(req.getRequestURI()));
        return pd;
    }
}
