package br.com.orbittapi.identity.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateEmailCommand(
        @NotBlank String email
) {}
