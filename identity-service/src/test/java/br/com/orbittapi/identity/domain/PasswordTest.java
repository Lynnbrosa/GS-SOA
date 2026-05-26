package br.com.orbittapi.identity.domain;

import br.com.orbittapi.identity.domain.exception.WeakPasswordException;
import br.com.orbittapi.identity.domain.model.Password;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordTest {

    @Test
    void rejectsShortPassword() {
        assertThatThrownBy(() -> Password.fromRaw("Abc1"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void rejectsPasswordWithoutDigit() {
        assertThatThrownBy(() -> Password.fromRaw("Abcdefgh"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void rejectsPasswordWithoutUppercase() {
        assertThatThrownBy(() -> Password.fromRaw("abcdefg1"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void hashesRawPassword() {
        Password p = Password.fromRaw("Abcdefg1");
        assertThat(p.hash()).isNotEqualTo("Abcdefg1");
        assertThat(p.matches("Abcdefg1")).isTrue();
        assertThat(p.matches("wrong")).isFalse();
    }
}
