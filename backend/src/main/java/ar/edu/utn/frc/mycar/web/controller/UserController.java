package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.UserService;
import ar.edu.utn.frc.mycar.web.dto.request.ChangePasswordRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateProfileRequest;
import ar.edu.utn.frc.mycar.web.dto.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
