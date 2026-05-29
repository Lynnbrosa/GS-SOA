package br.com.orbittapi.soap.client.dto;

public record RegisterQueryRequest(
        String type,
        double latitude,
        double longitude
) {}
