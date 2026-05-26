package br.com.orbittapi.satellite.domain.port;

import br.com.orbittapi.satellite.domain.model.Coordinate;
import br.com.orbittapi.satellite.domain.model.LandUseSnapshot;
import br.com.orbittapi.satellite.domain.model.VegetationSnapshot;

/**
 * Porta de saida do dominio (anti-corruption layer).
 * Implementacoes (Mock, NASA, ESA) ficam em infrastructure/adapter.
 */
public interface SatelliteDataSource {

    LandUseSnapshot fetchLandUse(Coordinate coordinate);

    VegetationSnapshot fetchVegetation(Coordinate coordinate);
}
