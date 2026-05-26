package br.com.orbittapi.identity.domain;

import br.com.orbittapi.identity.domain.exception.InvalidEmailException;
import br.com.orbittapi.identity.domain.model.Email;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void rejectsBlankEmail() {
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void rejectsMalformedEmail() {
        assertThatThrownBy(() -> new Email("not-an-email"))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void normalizesToLowerCaseAndTrims() {
        Email email = new Email("  Test@Example.COM ");
        assertThat(email.value()).isEqualTo("test@example.com");
    }

    @Test
    void equalityByValue() {
        assertThat(new Email("a@b.com")).isEqualTo(new Email("A@B.com"));
    }
}
