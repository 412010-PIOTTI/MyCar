package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * Returned by POST /api/auth/login.
 * When {@code requires2FA} is true only {@code email} (masked) is present.
 * When false, the full JWT and user details are populated and {@code requires2FA} is omitted.
 */
@Schema(description = "Login result — either a JWT or a 2FA challenge")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    @Schema(description = "Signed JWT (absent when 2FA is required)", example = "eyJhbGci...")
    private final String token;

    @Schema(description = "User ID (absent when 2FA is required)", example = "1")
    private final Long id;

    @Schema(description = "Display name (absent when 2FA is required)", example = "Ana Pérez")
    private final String name;

    @Schema(description = "Email — full when authenticated, masked (a***@domain.com) when 2FA is required", example = "a***@test.com")
    private final String email;

    @Schema(description = "User role (absent when 2FA is required)", example = "USER")
    private final Role role;

    @Schema(description = "True when a 2FA code was sent and the JWT is not yet issued", example = "true")
    private final Boolean requires2FA;
}
