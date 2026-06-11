package ar.edu.utn.frc.mycar.web.dto.request;

import ar.edu.utn.frc.mycar.web.validation.MaxCurrentYear;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for PUT /api/vehicles/{id}. All fields are optional — null fields are left unchanged. */
@Schema(description = "Payload to update vehicle data. All fields are optional; null fields are left unchanged.")
@Getter
@Setter
@NoArgsConstructor
public class UpdateVehicleRequest {

    @Schema(description = "New license plate (globally unique)", example = "AB123CD")
    @Size(min = 1, max = 10, message = "La patente debe tener entre 1 y 10 caracteres")
    private String plate;

    @Schema(description = "Vehicle brand", example = "Toyota")
    @Size(min = 1, max = 50, message = "La marca debe tener entre 1 y 50 caracteres")
    private String brand;

    @Schema(description = "Vehicle model", example = "Corolla")
    @Size(min = 1, max = 50, message = "El modelo debe tener entre 1 y 50 caracteres")
    private String model;

    @Schema(description = "Manufacturing year (1900 – current year)", example = "2020")
    @Min(value = 1900, message = "El año debe ser 1900 o posterior")
    @MaxCurrentYear
    private Integer year;

    @Schema(description = "Vehicle color (null = unchanged, empty string = clear color)", example = "Blanco")
    @Size(max = 30, message = "El color no puede superar los 30 caracteres")
    private String color;
}
