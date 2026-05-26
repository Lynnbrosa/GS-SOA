package br.com.orbittapi.satellite.domain;

import br.com.orbittapi.satellite.domain.exception.InvalidCoordinateException;
import br.com.orbittapi.satellite.domain.model.Coordinate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordinateTest {

    @Test
    void acceptsValidCoordinates() {
        Coordinate c = new Coordinate(-23.5, -46.6);
        assertThat(c.latitude()).isEqualTo(-23.5);
        assertThat(c.longitude()).isEqualTo(-46.6);
    }

    @Test
    void rejectsLatitudeOutOfRange() {
        assertThatThrownBy(() -> new Coordinate(91.0, 0.0))
                .isInstanceOf(InvalidCoordinateException.class);
        assertThatThrownBy(() -> new Coordinate(-91.0, 0.0))
                .isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void rejectsLongitudeOutOfRange() {
        assertThatThrownBy(() -> new Coordinate(0.0, 181.0))
                .isInstanceOf(InvalidCoordinateException.class);
        assertThatThrownBy(() -> new Coordinate(0.0, -181.0))
                .isInstanceOf(InvalidCoordinateException.class);
    }

    @Test
    void equalityByValue() {
        assertThat(new Coordinate(1.0, 2.0)).isEqualTo(new Coordinate(1.0, 2.0));
    }
}
