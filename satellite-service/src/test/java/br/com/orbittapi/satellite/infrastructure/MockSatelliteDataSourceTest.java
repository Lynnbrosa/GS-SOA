package br.com.orbittapi.satellite.infrastructure;

import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.LandUseSnapshot;
import br.com.orbittapi.satellite.domain.model.VegetationSnapshot;
import br.com.orbittapi.satellite.infrastructure.adapter.MockSatelliteDataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockSatelliteDataSourceTest {

    private final MockSatelliteDataSource source = new MockSatelliteDataSource();

    @Test
    void landUseIsDeterministicForSameCoordinate() {
        Coordinate c = new Coordinate(-23.5, -46.6);
        LandUseSnapshot s1 = source.fetchLandUse(c);
        LandUseSnapshot s2 = source.fetchLandUse(c);
        assertThat(s1.distribution()).isEqualTo(s2.distribution());
    }

    @Test
    void vegetationIsDeterministicForSameCoordinate() {
        Coordinate c = new Coordinate(-23.5, -46.6);
        VegetationSnapshot v1 = source.fetchVegetation(c);
        VegetationSnapshot v2 = source.fetchVegetation(c);
        assertThat(v1.ndvi()).isEqualTo(v2.ndvi());
    }

    @Test
    void differentCoordinatesProduceDifferentResults() {
        VegetationSnapshot v1 = source.fetchVegetation(new Coordinate(-23.5, -46.6));
        VegetationSnapshot v2 = source.fetchVegetation(new Coordinate(40.0, -74.0));
        assertThat(v1.ndvi()).isNotEqualTo(v2.ndvi());
    }
}
