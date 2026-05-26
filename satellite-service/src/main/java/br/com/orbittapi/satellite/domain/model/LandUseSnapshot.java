package br.com.orbittapi.satellite.domain.model;

import java.time.LocalDate;

public record LandUseSnapshot(
        Coordinate coordinate,
        LandUseDistribution distribution,
        LocalDate imageDate,
        SatelliteSource source
) {}
