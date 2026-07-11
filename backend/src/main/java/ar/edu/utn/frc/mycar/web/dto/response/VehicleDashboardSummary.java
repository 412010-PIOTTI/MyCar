package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Per-vehicle status summary for the dashboard")
public record VehicleDashboardSummary(

        @Schema(description = "Vehicle ID", example = "1")
        Long vehicleId,

        @Schema(description = "License plate", example = "AB123CD")
        String plate,

        @Schema(description = "Brand", example = "Volkswagen")
        String brand,

        @Schema(description = "Model", example = "Gol")
        String model,

        @Schema(description = "Current odometer reading in kilometres", example = "46812")
        Integer currentKm,

        @Schema(description = "Active alerts, sorted by urgency (most urgent first)")
        List<AlertResponse> activeAlerts,

        @Schema(description = "Key documents with their current status, sorted by soonest expiry first")
        List<DocumentResponse> documents,

        @Schema(description = "Most recent maintenance log, or null if none registered yet", nullable = true)
        MaintenanceLogResponse lastService
) {}
