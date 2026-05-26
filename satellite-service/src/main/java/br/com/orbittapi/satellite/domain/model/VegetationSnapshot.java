package br.com.orbittapi.satellite.domain.model;

import java.time.LocalDate;

public record VegetationSnapshot(
        Coordinate coordinate,
        NdviScore ndvi,
        LocalDate imageDate,
        SatelliteSource source
) {}
