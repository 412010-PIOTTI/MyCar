package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Returned by register and login on success. Contains the JWT and basic user info. */
@Schema(description = "Authentication result containing the JWT and user details")
@Getter
@AllArgsConstructor
public class AuthResponse {

    /** Signed JWT to include in subsequent requests as {@code Authorization: Bearer <token>}. */
    @Schema(description = "Signed JWT. Include as: Authorization: Bearer <token>", example = "eyJhbGci...")
    private String token;

    /** Database-assigned user ID. */
    @Schema(description = "Database-assigned user ID", example = "1")
    private Long id;

    /** User's display name. */
    @Schema(description = "User's display name", example = "Ana Pérez")
    private String name;

    /** User's email address. */
    @Schema(description = "User's email address", example = "ana@example.com")
    private String email;

    /** Role assigned to the user (USER or CONCESIONARIO). */
    @Schema(description = "Role assigned to the user", example = "USER")
    private Role role;
}
