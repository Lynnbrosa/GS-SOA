package br.com.orbittapi.identity.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
        @NotBlank String email,
        @NotBlank String password
) {}
