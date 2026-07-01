package ar.edu.utn.frc.mycar.web.dto.request;

import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Schema(description = "Payload to create an alert for a vehicle")
@Getter
@Setter
@NoArgsConstructor
public class CreateAlertRequest {

    @Schema(description = "Alert title", example = "Renovar VTV", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    private String title;

    @Schema(description = "DATE = triggered by expiry date | KM = triggered by odometer",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El tipo de alerta es obligatorio")
    private AlertType alertType;

    @Schema(description = "Expiry date (required when alertType = DATE)", example = "2025-03-15")
    private LocalDate alertDate;

    @Schema(description = "Odometer km limit (required when alertType = KM)", example = "50000")
    @Min(value = 0, message = "El km de alerta no puede ser negativo")
    private Integer alertKm;

    @Schema(description = "Days in advance to start warning (default: 30)", example = "30")
    @Min(value = 1, message = "Los días de anticipación deben ser al menos 1")
    private Integer advanceDays;
}
