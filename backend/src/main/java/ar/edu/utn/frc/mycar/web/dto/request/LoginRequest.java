package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for POST /api/auth/login. */
@Schema(description = "Credentials required to authenticate an existing account")
@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    /** Registered email address. */
    @Schema(description = "Registered email address", example = "ana@test.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    /** Account password (plain text — transmitted over HTTPS only). */
    @Schema(description = "Account password", example = "secret123")
    @NotBlank(message = "Password is required")
    private String password;
}
