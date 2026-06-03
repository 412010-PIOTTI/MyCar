package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.AuthService;
import ar.edu.utn.frc.mycar.application.service.TwoFactorService;
import ar.edu.utn.frc.mycar.web.dto.request.CancelTwoFactorRequest;
import ar.edu.utn.frc.mycar.web.dto.request.LoginRequest;
import ar.edu.utn.frc.mycar.web.dto.request.RegisterRequest;
import ar.edu.utn.frc.mycar.web.dto.request.ResendTwoFactorRequest;
import ar.edu.utn.frc.mycar.web.dto.request.VerifyTwoFactorRequest;
import ar.edu.utn.frc.mycar.web.dto.response.AuthResponse;
import ar.edu.utn.frc.mycar.web.dto.response.LoginResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoints for user registration, authentication, and 2FA. */
@Tag(name = "Authentication", description = "Register and authenticate users")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TwoFactorService twoFactorService;

    @Operation(
            summary = "Register a new user account",
            description = "Creates a new user account with role USER. Returns a JWT valid for 24 hours."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Email already registered.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(
            summary = "Authenticate with email and password",
            description = """
                    Validates credentials. If 2FA is disabled, returns the JWT immediately. \
                    If 2FA is enabled, sends a 6-digit code to the user's email and returns \
                    `{ "requires2FA": true, "email": "a***@domain.com" }`. \
                    The JWT is issued after a successful call to POST /api/auth/verify-2fa."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated or 2FA challenge sent.",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Account disabled.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @Operation(
            summary = "Complete 2FA login — submit the 6-digit code",
            description = """
                    Validates the code sent by email. \
                    On success, marks the token as used and returns the JWT. \
                    On failure, increments the attempt counter; after 5 failures the code is invalidated."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code correct — JWT issued.",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Wrong code, expired, or max attempts reached.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/verify-2fa")
    public AuthResponse verifyTwoFactor(@RequestBody @Valid VerifyTwoFactorRequest request) {
        return authService.verifyTwoFactor(request.getEmail(), request.getCode());
    }

    @Operation(
            summary = "Resend 2FA code",
            description = "Generates a new 6-digit code and sends it to the email. Invalidates any previous pending code."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New code sent."),
            @ApiResponse(responseCode = "401", description = "Email not found or 2FA not enabled.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @PostMapping("/verify-2fa/resend")
    public ResponseEntity<Void> resendTwoFactor(@RequestBody @Valid ResendTwoFactorRequest request) {
        twoFactorService.resend(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Cancel 2FA flow",
            description = "Invalidates the pending 2FA token for the email (user chose to cancel login)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Token invalidated (or no token existed)."),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @SecurityRequirements
    @DeleteMapping("/verify-2fa/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelTwoFactor(@RequestBody @Valid CancelTwoFactorRequest request) {
        twoFactorService.cancel(request.getEmail());
    }

    @Operation(
            summary = "Logout — invalidate the current JWT",
            description = "Adds the token's jti to the server-side blacklist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token revoked successfully."),
            @ApiResponse(responseCode = "401", description = "Missing or already-revoked JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Sesión cerrada correctamente"));
    }
}
