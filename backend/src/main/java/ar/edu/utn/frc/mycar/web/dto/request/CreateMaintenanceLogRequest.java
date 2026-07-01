package ar.edu.utn.frc.mycar.web.dto.request;

import ar.edu.utn.frc.mycar.domain.enums.MaintenanceSystem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload to register a maintenance or repair record")
@Getter
@Setter
@NoArgsConstructor
public class CreateMaintenanceLogRequest {

    @Schema(description = "Vehicle system involved", example = "MOTOR", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El sistema es obligatorio")
    private MaintenanceSystem system;

    @Schema(description = "Date the service was performed", example = "2024-03-15",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate date;

    @Schema(description = "Odometer reading at the time of service", example = "45000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El kilometraje es obligatorio")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer kmAtMaintenance;

    @Schema(description = "Workshop or repair shop name", example = "Taller Central SA",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El taller es obligatorio")
    @Size(max = 100, message = "El taller no puede superar los 100 caracteres")
    private String workshop;

    @Schema(description = "Optional description of work performed", example = "Cambio de pastillas y discos delanteros")
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    private String description;

    @Schema(description = "Optional cost of the service", example = "25000.00")
    @DecimalMin(value = "0.00", inclusive = false, message = "El costo debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El costo no puede tener más de 8 enteros y 2 decimales")
    private BigDecimal cost;

    @Schema(description = "Odometer reading at which the next service is expected", example = "55000")
    @Min(value = 0, message = "El próximo service en km no puede ser negativo")
    private Integer nextServiceKm;

    @Schema(description = "Date on which the next service is expected", example = "2025-03-15")
    private LocalDate nextServiceDate;

    @Schema(description = "If true, an Expense record (category MANTENIMIENTO) is created atomically with this log")
    private boolean createExpense;

    @Schema(description = "Optional subcategory for the auto-created expense", example = "Service oficial")
    @Size(max = 50, message = "La subcategoría no puede superar los 50 caracteres")
    private String expenseSubcategory;
}
