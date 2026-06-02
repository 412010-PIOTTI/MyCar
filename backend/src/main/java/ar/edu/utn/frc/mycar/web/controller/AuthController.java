package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.AuthService;
import ar.edu.utn.frc.mycar.web.dto.request.LoginRequest;
import ar.edu.utn.frc.mycar.web.dto.request.RegisterRequest;
import ar.edu.utn.frc.mycar.web.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoints for user registration and authentication. */
@Tag(name = "Authentication", description = "Register and authenticate users")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register a new user account",
            description = """
                    Creates a new user account with role USER. \
                    Passwords are stored as BCrypt hashes. \
                    Returns a signed JWT valid for 24 hours that must be sent \
                    as `Authorization: Bearer <token>` on all protected requests."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created. Returns the JWT and user details.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed — one or more fields are invalid or missing.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The email address is already registered.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @SecurityRequirements   // no token required for this endpoint
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(
            summary = "Authenticate with email and password",
            description = """
                    Validates the provided credentials and returns a signed JWT valid for 24 hours. \
                    The token must be sent as `Authorization: Bearer <token>` on all protected requests. \
                    The error response is intentionally identical for unknown email and wrong password \
                    to prevent user enumeration."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful. Returns the JWT and user details.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed — email or password field is missing or malformed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials — email not found or password does not match.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account is disabled.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @SecurityRequirements   // no token required for this endpoint
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @Operation(
            summary = "Logout — invalidate the current JWT",
            description = """
                    Adds the token's jti to the server-side blacklist. \
                    The token is immediately unusable even if it hasn't expired yet. \
                    The client must also delete the token from its local storage."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token revoked successfully."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or already-revoked JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
}
