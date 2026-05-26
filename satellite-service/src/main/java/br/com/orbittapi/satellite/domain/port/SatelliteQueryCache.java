package br.com.orbittapi.satellite.domain.port;

import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.LandUseSnapshot;
import br.com.orbittapi.satellite.domain.model.VegetationSnapshot;

import java.util.Optional;

public interface SatelliteQueryCache {

    Optional<LandUseSnapshot> getLandUse(Coordinate coordinate);

    void putLandUse(Coordinate coordinate, LandUseSnapshot snapshot);

    Optional<VegetationSnapshot> getVegetation(Coordinate coordinate);

    void putVegetation(Coordinate coordinate, VegetationSnapshot snapshot);
}
