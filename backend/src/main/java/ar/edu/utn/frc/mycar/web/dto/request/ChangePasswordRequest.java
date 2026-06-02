package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for PUT /api/users/me/password. */
@Schema(description = "Payload to change the authenticated user's password")
@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordRequest {

    @Schema(description = "Current password for verification", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(description = "New password (min 8 chars)", minLength = 8, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters")
    private String newPassword;
}
