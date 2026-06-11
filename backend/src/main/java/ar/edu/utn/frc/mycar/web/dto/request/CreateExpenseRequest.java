package ar.edu.utn.frc.mycar.web.dto.request;

import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Request body for POST /api/vehicles/{id}/expenses. */
@Schema(description = "Payload to register a new expense")
@Getter
@Setter
@NoArgsConstructor
public class CreateExpenseRequest {

    @Schema(description = "Expense category", example = "OPERATIVO", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La categoría es obligatoria")
    private ExpenseCategory category;

    @Schema(description = "Optional free-text subcategory", example = "COMBUSTIBLE")
    @Size(max = 50, message = "La subcategoría no puede superar los 50 caracteres")
    private String subcategory;

    @Schema(description = "Date of the expense", example = "2024-03-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha es obligatoria")
    @PastOrPresent(message = "La fecha no puede ser futura")
    private LocalDate date;

    @Schema(description = "Amount in local currency", example = "5000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El monto no puede tener más de 8 enteros y 2 decimales")
    private BigDecimal amount;

    @Schema(description = "Optional description", example = "Carga completa en YPF")
    @Size(max = 300, message = "La descripción no puede superar los 300 caracteres")
    private String description;

    @Schema(description = "Odometer reading at the time of the expense (optional)", example = "45230")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer kmAtExpense;
}
