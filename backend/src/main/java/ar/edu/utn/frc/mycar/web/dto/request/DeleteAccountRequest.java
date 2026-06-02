package ar.edu.utn.frc.mycar.web.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Request body for {@code DELETE /api/users/me}. Requires the current password for confirmation. */
public record DeleteAccountRequest(
        @NotBlank(message = "Password is required")
        String password
) {}
