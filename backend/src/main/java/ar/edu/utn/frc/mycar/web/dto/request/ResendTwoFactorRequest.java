package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for POST /api/auth/verify-2fa/resend. */
@Schema(description = "Email of the user who needs a new 2FA code")
@Getter
@Setter
@NoArgsConstructor
public class ResendTwoFactorRequest {

    @Schema(description = "Email address", example = "ana@test.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;
}
