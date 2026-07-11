package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Main-screen dashboard: status summary across all of the user's vehicles")
public record DashboardResponse(

        @Schema(description = "Per-vehicle status summaries, ordered like the vehicle list (newest first)")
        List<VehicleDashboardSummary> vehicles,

        @Schema(description = "Total expenses across all vehicles for the current month", example = "142500.00")
        BigDecimal totalExpensesMonth,

        @Schema(description = "Total expenses across all vehicles for the current year", example = "980000.00")
        BigDecimal totalExpensesYear,

        @Schema(description = "Day's exchange rate quote. Only populated for CONCESIONARIO users; null otherwise.",
                nullable = true)
        ExchangeRateResponse exchangeRate
) {}
