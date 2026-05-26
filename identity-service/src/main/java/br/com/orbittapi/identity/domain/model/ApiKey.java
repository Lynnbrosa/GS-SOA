package br.com.orbittapi.identity.domain.model;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

public final class ApiKey {

    private static final String PREFIX = "obt_";
    private static final int ENTROPY_BYTES = 24;
    private static final SecureRandom RNG = new SecureRandom();

    private final String value;
    private final boolean revoked;

    private ApiKey(String value, boolean revoked) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ApiKey value must not be blank");
        }
        this.value = value;
        this.revoked = revoked;
    }

    public static ApiKey generate() {
        byte[] buffer = new byte[ENTROPY_BYTES];
        RNG.nextBytes(buffer);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
        return new ApiKey(PREFIX + token, false);
    }

    public static ApiKey rehydrate(String value, boolean revoked) {
        return new ApiKey(value, revoked);
    }

    public ApiKey revoke() {
        if (revoked) {
            return this;
        }
        return new ApiKey(value, true);
    }

    public String value() {
        return value;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public boolean isActive() {
        return !revoked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApiKey apiKey)) return false;
        return revoked == apiKey.revoked && Objects.equals(value, apiKey.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, revoked);
    }
}
