package br.com.orbittapi.satellite.domain.repository;

import br.com.orbittapi.satellite.domain.model.SatelliteQuery;

public interface SatelliteQueryRepository {
    SatelliteQuery save(SatelliteQuery query);
}
