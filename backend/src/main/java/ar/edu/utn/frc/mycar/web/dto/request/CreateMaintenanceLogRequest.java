package ar.edu.utn.frc.mycar.web.dto.request;

import ar.edu.utn.frc.mycar.domain.enums.MaintenanceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Request body for POST /api/vehicles/{id}/maintenance. */
@Schema(description = "Payload to register a maintenance record")
@Getter
@Setter
@NoArgsConstructor
public class CreateMaintenanceLogRequest {

    @Schema(description = "Type of maintenance", example = "ACEITE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El tipo de mantenimiento es obligatorio")
    private MaintenanceType type;

    @Schema(description = "Date the maintenance was performed", example = "2024-03-15",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate date;

    @Schema(description = "Odometer reading at the time of maintenance", example = "45000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El kilometraje es obligatorio")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer kmAtMaintenance;

    @Schema(description = "Optional description of work performed", example = "Cambio de aceite 5W40 + filtro")
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    private String description;

    @Schema(description = "Optional cost of the maintenance", example = "12500.00")
    @DecimalMin(value = "0.00", inclusive = false, message = "El costo debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El costo no puede tener más de 8 enteros y 2 decimales")
    private BigDecimal cost;
}
