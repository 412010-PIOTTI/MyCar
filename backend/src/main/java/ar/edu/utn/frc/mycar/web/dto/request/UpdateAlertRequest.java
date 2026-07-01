package ar.edu.utn.frc.mycar.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Schema(description = "Payload to update an existing alert. All fields are optional — null means no change.")
@Getter
@Setter
@NoArgsConstructor
public class UpdateAlertRequest {

    @Schema(description = "New alert title", example = "Renovar VTV urgente")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    private String title;

    @Schema(description = "New expiry date", example = "2025-06-01")
    private LocalDate alertDate;

    @Schema(description = "New km limit", example = "55000")
    @Min(value = 0, message = "El km de alerta no puede ser negativo")
    private Integer alertKm;

    @Schema(description = "Days in advance to start warning", example = "15")
    @Min(value = 1, message = "Los días de anticipación deben ser al menos 1")
    private Integer advanceDays;

    @Schema(description = "Set to false to dismiss/deactivate the alert", example = "false")
    private Boolean active;
}
