package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.AlertService;
import ar.edu.utn.frc.mycar.web.dto.request.CreateAlertRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateAlertRequest;
import ar.edu.utn.frc.mycar.web.dto.response.AlertResponse;
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

@Tag(name = "Alerts", description = "Create and manage vehicle alerts (date-based and km-based)")
@RestController
@RequestMapping("/api/vehicles/{vehicleId}/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "Create an alert", description = "Creates a date-based or km-based alert for the vehicle.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alert created.",
                    content = @Content(schema = @Schema(implementation = AlertResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse create(Authentication authentication,
                                @PathVariable Long vehicleId,
                                @RequestBody @Valid CreateAlertRequest request) {
        return alertService.create(authentication.getName(), vehicleId, request);
    }

    @Operation(summary = "List active alerts",
               description = "Returns all active alerts for the vehicle ordered by urgency (URGENTE first). Urgency is recalculated live on every read.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned.",
                    content = @Content(schema = @Schema(implementation = AlertResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<AlertResponse> getAll(Authentication authentication,
                                      @PathVariable Long vehicleId) {
        return alertService.getAll(authentication.getName(), vehicleId);
    }

    @Operation(summary = "Get an alert by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert found.",
                    content = @Content(schema = @Schema(implementation = AlertResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Alert or vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{alertId}")
    public AlertResponse getById(Authentication authentication,
                                 @PathVariable Long vehicleId,
                                 @PathVariable Long alertId) {
        return alertService.getById(authentication.getName(), vehicleId, alertId);
    }

    @Operation(summary = "Update an alert", description = "Updates alert fields. All fields are optional — only non-null values are applied. Set active=false to dismiss the alert.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert updated.",
                    content = @Content(schema = @Schema(implementation = AlertResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Alert or vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PatchMapping("/{alertId}")
    public AlertResponse update(Authentication authentication,
                                @PathVariable Long vehicleId,
                                @PathVariable Long alertId,
                                @RequestBody @Valid UpdateAlertRequest request) {
        return alertService.update(authentication.getName(), vehicleId, alertId, request);
    }

    @Operation(summary = "Delete an alert")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Alert deleted."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Alert or vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{alertId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication,
                       @PathVariable Long vehicleId,
                       @PathVariable Long alertId) {
        alertService.delete(authentication.getName(), vehicleId, alertId);
    }
}
