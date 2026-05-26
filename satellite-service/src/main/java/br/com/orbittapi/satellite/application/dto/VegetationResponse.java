package br.com.orbittapi.satellite.application.dto;

import java.time.LocalDate;

public record VegetationResponse(
        double latitude,
        double longitude,
        double ndvi,
        String health,
        LocalDate imageDate,
        String source,
        boolean cacheHit
) {}
