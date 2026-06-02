package ar.edu.utn.frc.mycar.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank(message = "Password is required")
        String password
) {}
