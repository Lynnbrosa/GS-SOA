package br.com.orbittapi.satellite.domain;

import br.com.orbittapi.satellite.domain.exception.InvalidNdviScoreException;
import br.com.orbittapi.satellite.domain.model.NdviScore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NdviScoreTest {

    @Test
    void rejectsValuesOutOfRange() {
        assertThatThrownBy(() -> new NdviScore(1.5)).isInstanceOf(InvalidNdviScoreException.class);
        assertThatThrownBy(() -> new NdviScore(-1.5)).isInstanceOf(InvalidNdviScoreException.class);
    }

    @Test
    void classifiesVegetationHealth() {
        assertThat(new NdviScore(0.0).classify()).isEqualTo(NdviScore.VegetationHealth.NONE);
        assertThat(new NdviScore(0.2).classify()).isEqualTo(NdviScore.VegetationHealth.SPARSE);
        assertThat(new NdviScore(0.4).classify()).isEqualTo(NdviScore.VegetationHealth.MODERATE);
        assertThat(new NdviScore(0.8).classify()).isEqualTo(NdviScore.VegetationHealth.DENSE);
    }
}
