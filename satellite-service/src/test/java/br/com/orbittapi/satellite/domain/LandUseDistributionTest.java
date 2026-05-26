package br.com.orbittapi.satellite.domain;

import br.com.orbittapi.satellite.domain.exception.InvalidLandUseDistributionException;
import br.com.orbittapi.satellite.domain.model.LandUseDistribution;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LandUseDistributionTest {

    @Test
    void acceptsPercentagesSummingTo100() {
        LandUseDistribution dist = new LandUseDistribution(50, 20, 10, 20);
        assertThat(dist.vegetationPercent()).isEqualTo(50);
    }

    @Test
    void rejectsPercentagesNotSummingTo100() {
        assertThatThrownBy(() -> new LandUseDistribution(50, 20, 10, 10))
                .isInstanceOf(InvalidLandUseDistributionException.class);
    }

    @Test
    void rejectsNegativePercentages() {
        assertThatThrownBy(() -> new LandUseDistribution(-1, 50, 50, 1))
                .isInstanceOf(InvalidLandUseDistributionException.class);
    }
}
