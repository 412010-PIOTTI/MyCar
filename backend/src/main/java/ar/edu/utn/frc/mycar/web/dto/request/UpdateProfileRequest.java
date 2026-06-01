package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for PUT /api/users/me. Email cannot be changed. */
@Schema(description = "Payload to update the authenticated user's profile")
@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @Schema(description = "New full display name", example = "Ana García", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
}
