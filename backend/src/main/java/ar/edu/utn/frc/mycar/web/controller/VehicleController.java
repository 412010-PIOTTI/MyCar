package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.VehicleService;
import ar.edu.utn.frc.mycar.web.dto.request.CreateVehicleRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateVehicleRequest;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
     * Updates the mutable fields of a vehicle owned by the authenticated user.
     *
     * <p>All request fields are optional. Omitted or {@code null} fields are left unchanged.
     * Returns 404 when the vehicle does not exist, belongs to another user, or is deleted.
     */
    @Operation(
            summary = "Update a vehicle",
            description = """
                    Updates one or more fields of the vehicle with the given id, provided it \
                    belongs to the authenticated user and is not deleted. All fields are optional \
                    — omitted or null fields are left unchanged. The plate is normalised to \
                    upper-case and must remain globally unique."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vehicle updated successfully.",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed (invalid field values).",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found or does not belong to the authenticated user.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "The new plate is already registered by another vehicle.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @PutMapping("/{id}")
    public VehicleResponse update(Authentication authentication,
                                  @PathVariable Long id,
                                  @RequestBody @Valid UpdateVehicleRequest request) {
        return vehicleService.update(authentication.getName(), id, request);
    }

    /**
     * Soft-deletes a vehicle owned by the authenticated user.
     *
     * <p>Sets the vehicle's {@code active} flag to {@code false}. The vehicle is immediately
     * removed from all listings and becomes inaccessible via other endpoints.
     * Returns 404 when the vehicle does not exist, belongs to another user, or is already deleted.
     */
    @Operation(
            summary = "Delete a vehicle",
            description = """
                    Soft-deletes the vehicle with the given id by setting its active flag to false. \
                    The vehicle is immediately removed from all listings. Returns 404 when the vehicle \
                    does not exist, belongs to another user, or is already deleted."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully."),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found or does not belong to the authenticated user.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        vehicleService.delete(authentication.getName(), id);
    }

    /**
     * Returns all active vehicles that belong to the authenticated user, ordered from newest to oldest.
     * Returns an empty array if the user has no registered vehicles.
     */
    @Operation(
            summary = "List my vehicles",
            description = "Returns all active vehicles registered under the authenticated user's account, ordered by registration date (newest first). Returns an empty array when no vehicles exist."
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

    /**
     * Returns a single active vehicle by id, only if it belongs to the authenticated user.
     *
     * <p>Returns 404 for "not found", "belongs to another user", and "soft-deleted"
     * to avoid leaking the existence of other users' vehicles.
     */
    @Operation(
            summary = "Get a vehicle by id",
            description = """
                    Returns the vehicle with the given id, provided it belongs to the \
                    authenticated user and is not deleted. Returns 404 when the vehicle \
                    does not exist, is owned by a different user, or has been deleted."""
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Vehicle found and returned.",
                    content = @Content(schema = @Schema(implementation = VehicleResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found or does not belong to the authenticated user.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))
            )
    })
    @GetMapping("/{id}")
    public VehicleResponse getById(Authentication authentication, @PathVariable Long id) {
        return vehicleService.getById(authentication.getName(), id);
    }
}
