package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.UserService;
import ar.edu.utn.frc.mycar.web.dto.request.ChangePasswordRequest;
import ar.edu.utn.frc.mycar.web.dto.request.DeleteAccountRequest;
import ar.edu.utn.frc.mycar.web.dto.request.Toggle2FARequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateProfileRequest;
import ar.edu.utn.frc.mycar.web.dto.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoints for reading and updating the authenticated user's profile. */
@Tag(name = "User profile", description = "Read and update the authenticated user's own data")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get my profile",
            description = "Returns the profile of the currently authenticated user. Email is read-only."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile returned successfully.",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/me")
    public UserProfileResponse getMe(Authentication authentication) {
        return userService.getMe(authentication.getName());
    }

    @Operation(
            summary = "Update my profile",
            description = "Updates the authenticated user's display name. Email cannot be changed."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully.",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PutMapping("/me")
    public UserProfileResponse updateMe(Authentication authentication,
                                        @RequestBody @Valid UpdateProfileRequest request) {
        return userService.updateProfile(authentication.getName(), request);
    }

    @Operation(
            summary = "Change my password",
            description = "Changes the password for the authenticated user. Requires the current password for verification."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed successfully."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or current password is incorrect.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Authentication authentication,
                               @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(authentication.getName(), request);
    }

    @Operation(
            summary = "Enable or disable two-factor authentication",
            description = "Requires current password confirmation. Returns the updated profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "2FA state updated.",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Password incorrect or validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/me/2fa")
    public UserProfileResponse toggle2FA(Authentication authentication,
                                         @RequestBody @Valid Toggle2FARequest request) {
        return userService.toggle2FA(authentication.getName(), request);
    }

    @Operation(
            summary = "Delete my account (soft delete)",
            description = """
                    Requires the current password for confirmation. \
                    Sets active=false on the user record and revokes the current JWT. \
                    All vehicle, expense and document data is preserved in the database."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account deactivated and token revoked."),
            @ApiResponse(
                    responseCode = "400",
                    description = "Password is incorrect.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> deleteAccount(
            Authentication authentication,
            @RequestBody @Valid DeleteAccountRequest request,
            HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        userService.deleteAccount(authentication.getName(), request.password(), authHeader.substring(7));
        return ResponseEntity.ok(Map.of("message", "Cuenta desactivada correctamente"));
    }
}
