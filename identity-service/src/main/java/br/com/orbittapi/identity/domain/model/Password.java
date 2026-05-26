package br.com.orbittapi.identity.domain.model;

import br.com.orbittapi.identity.domain.exception.WeakPasswordException;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Objects;

public final class Password {

    private static final int MIN_LENGTH = 8;

    private final String hash;

    private Password(String hash) {
        this.hash = hash;
    }

    public static Password fromRaw(String raw) {
        validateStrength(raw);
        String hash = BCrypt.hashpw(raw, BCrypt.gensalt(12));
        return new Password(hash);
    }

    public static Password fromHash(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }
        return new Password(hash);
    }

    public boolean matches(String raw) {
        if (raw == null) return false;
        return BCrypt.checkpw(raw, hash);
    }

    public String hash() {
        return hash;
    }

    private static void validateStrength(String raw) {
        if (raw == null || raw.length() < MIN_LENGTH) {
            throw new WeakPasswordException("Password must have at least " + MIN_LENGTH + " characters");
        }
        if (!raw.matches(".*\\d.*")) {
            throw new WeakPasswordException("Password must contain at least one digit");
        }
        if (!raw.matches(".*[A-Z].*")) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Password password)) return false;
        return Objects.equals(hash, password.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
