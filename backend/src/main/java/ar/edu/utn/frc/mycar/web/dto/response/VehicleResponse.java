package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Read-only view of a registered vehicle. Never exposes internal relations. */
@Schema(description = "Registered vehicle data")
public record VehicleResponse(

        @Schema(description = "Unique vehicle ID", example = "1")
        Long id,

        @Schema(description = "License plate", example = "AB123CD")
        String plate,

        @Schema(description = "Brand", example = "Toyota")
        String brand,

        @Schema(description = "Model", example = "Corolla")
        String model,

        @Schema(description = "Manufacturing year", example = "2020")
        Integer year,

        @Schema(description = "Color", example = "Blanco")
        String color,

        @Schema(description = "Current odometer reading in kilometres", example = "35000")
        Integer currentKm,

        @Schema(description = "Registration timestamp")
        LocalDateTime createdAt
) {}
