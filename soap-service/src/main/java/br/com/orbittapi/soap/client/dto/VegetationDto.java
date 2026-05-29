package br.com.orbittapi.soap.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VegetationDto(
        double latitude,
        double longitude,
        double ndvi,
        String health,
        LocalDate imageDate,
        String source,
        boolean cacheHit
) {}
