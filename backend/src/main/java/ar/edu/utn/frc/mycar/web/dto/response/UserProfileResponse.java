package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Read-only view of the authenticated user's profile. Never exposes passwordHash. */
@Schema(description = "Authenticated user's profile data")
public record UserProfileResponse(

        @Schema(description = "Unique user ID", example = "1")
        Long id,

        @Schema(description = "Full display name", example = "Ana Pérez")
        String name,

        @Schema(description = "Email address (read-only, used as login identifier)", example = "ana@example.com")
        String email,

        @Schema(description = "User role", example = "USER")
        Role role,

        @Schema(description = "Account creation timestamp")
        LocalDateTime createdAt
) {}
