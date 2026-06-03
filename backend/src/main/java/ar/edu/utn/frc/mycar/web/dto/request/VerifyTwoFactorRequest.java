package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for POST /api/auth/verify-2fa. */
@Schema(description = "Email and 6-digit code to complete 2FA login")
@Getter
@Setter
@NoArgsConstructor
public class VerifyTwoFactorRequest {

    @Schema(description = "Email address of the user completing 2FA", example = "ana@test.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    @Schema(description = "6-digit code received by email", example = "483921")
    @NotBlank(message = "Code is required")
    @Size(min = 6, max = 6, message = "Code must be exactly 6 digits")
    private String code;
}
