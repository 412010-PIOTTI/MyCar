package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for POST /api/vehicles. */
@Schema(description = "Payload to register a new vehicle")
@Getter
@Setter
@NoArgsConstructor
public class CreateVehicleRequest {

    @Schema(description = "Vehicle license plate (unique in the system)", example = "AB123CD",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La patente es obligatoria")
    @Size(max = 10, message = "La patente no puede superar los 10 caracteres")
    private String plate;

    @Schema(description = "Vehicle brand", example = "Toyota", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca no puede superar los 50 caracteres")
    private String brand;

    @Schema(description = "Vehicle model", example = "Corolla", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo no puede superar los 50 caracteres")
    private String model;

    @Schema(description = "Manufacturing year", example = "2020", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El año es obligatorio")
    @Min(value = 1900, message = "El año debe ser 1900 o posterior")
    @Max(value = 2100, message = "El año debe ser 2100 o anterior")
    private Integer year;

    @Schema(description = "Vehicle color (optional)", example = "Blanco")
    @Size(max = 30, message = "El color no puede superar los 30 caracteres")
    private String color;

    @Schema(description = "Initial odometer reading in kilometres", example = "35000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El kilometraje inicial es obligatorio")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer initialKm;
}
