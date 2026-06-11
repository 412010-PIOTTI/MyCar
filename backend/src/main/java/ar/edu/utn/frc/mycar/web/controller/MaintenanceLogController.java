package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.MaintenanceLogService;
import ar.edu.utn.frc.mycar.web.dto.request.CreateMaintenanceLogRequest;
import ar.edu.utn.frc.mycar.web.dto.response.MaintenanceLogResponse;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Endpoints for vehicle maintenance history. */
@Tag(name = "Maintenance", description = "Register and list vehicle maintenance records")
@RestController
@RequestMapping("/api/vehicles/{vehicleId}/maintenance")
@RequiredArgsConstructor
public class MaintenanceLogController {

    private final MaintenanceLogService maintenanceLogService;

    @Operation(
            summary = "Register a maintenance record",
            description = "Registers a new maintenance record for the given vehicle. If kmAtMaintenance is greater than the vehicle's current odometer, the vehicle's currentKm is updated automatically."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Maintenance record registered successfully.",
                    content = @Content(schema = @Schema(implementation = MaintenanceLogResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found or does not belong to the user.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceLogResponse create(Authentication authentication,
                                         @PathVariable Long vehicleId,
                                         @RequestBody @Valid CreateMaintenanceLogRequest request) {
        return maintenanceLogService.create(authentication.getName(), vehicleId, request);
    }

    @Operation(summary = "List maintenance records", description = "Returns all maintenance records for the given vehicle, ordered by date descending.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Maintenance list returned successfully.",
                    content = @Content(schema = @Schema(implementation = MaintenanceLogResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found or does not belong to the user.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<MaintenanceLogResponse> getAll(Authentication authentication, @PathVariable Long vehicleId) {
        return maintenanceLogService.getAll(authentication.getName(), vehicleId);
    }

    @Operation(summary = "Get a maintenance record by id", description = "Returns a single maintenance record by id, only if it belongs to the given vehicle owned by the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Record found and returned.",
                    content = @Content(schema = @Schema(implementation = MaintenanceLogResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Record or vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{logId}")
    public MaintenanceLogResponse getById(Authentication authentication,
                                          @PathVariable Long vehicleId,
                                          @PathVariable Long logId) {
        return maintenanceLogService.getById(authentication.getName(), vehicleId, logId);
    }
}
