package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for PUT /api/users/me/2fa. */
@Schema(description = "Toggle 2FA on or off. Password confirmation is required to prevent accidental changes.")
@Getter
@Setter
@NoArgsConstructor
public class Toggle2FARequest {

    @Schema(description = "Desired 2FA state", example = "true")
    private boolean enabled;

    @Schema(description = "Current account password for confirmation", example = "secret123")
    @NotBlank(message = "Password is required")
    private String password;
}
