package br.com.orbittapi.identity.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterAccountCommand(
        @NotBlank String email,
        @NotBlank String password
) {}
