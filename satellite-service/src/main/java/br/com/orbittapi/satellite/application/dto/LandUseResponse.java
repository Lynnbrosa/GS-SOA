package br.com.orbittapi.satellite.application.dto;

import java.time.LocalDate;

public record LandUseResponse(
        double latitude,
        double longitude,
        double vegetationPercent,
        double urbanPercent,
        double waterPercent,
        double bareSoilPercent,
        LocalDate imageDate,
        String source,
        boolean cacheHit
) {}
