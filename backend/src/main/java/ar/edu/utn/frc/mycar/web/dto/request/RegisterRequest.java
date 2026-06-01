package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for POST /api/auth/register. */
@Schema(description = "Payload required to create a new user account")
@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    /** Full display name of the user. */
    @Schema(description = "Full display name", example = "Ana Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    /** Email address used to identify the account and as the JWT subject. */
    @Schema(description = "Unique email address", example = "ana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    private String email;

    /** Plain-text password. Stored as BCrypt hash; never returned in any response. */
    @Schema(description = "Plain-text password (min 8 chars). Stored as BCrypt hash.", example = "secret123", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
