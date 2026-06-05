package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.VehicleService;
import ar.edu.utn.frc.mycar.web.dto.request.CreateVehicleRequest;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints for vehicle registration and management. */
@Tag(name = "Vehicles", description = "Register and manage vehicles")
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Registers a new vehicle for the authenticated user.
     *
     * <p>The plate is case-insensitive; it is stored in upper-case.
     * A user may own multiple vehicles, but each plate must be unique system-wide.
     */
    @Operation(
            summary = "Register a vehicle",
            description = """
                    Registers a new vehicle under the authenticated user's account. \
                    The plate is normalised to upper-case before persistence. \
                    A user may own multiple vehicles; each plate must be globally unique."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Vehicle registered successfully.",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed (missing or invalid fields).",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The plate is already registered in the system.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse register(Authentication authentication,
                                    @RequestBody @Valid CreateVehicleRequest request) {
        return vehicleService.register(authentication.getName(), request);
    }

    /**
     * Returns all vehicles that belong to the authenticated user, ordered from newest to oldest.
     * Returns an empty array if the user has no registered vehicles.
     */
    @Operation(
            summary = "List my vehicles",
            description = "Returns all vehicles registered under the authenticated user's account, ordered by registration date (newest first). Returns an empty array when no vehicles exist."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vehicle list returned successfully (may be empty).",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping
    public List<VehicleResponse> getAll(Authentication authentication) {
        return vehicleService.getAll(authentication.getName());
    }
}
