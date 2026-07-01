package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Annual maintenance cost totals with year-over-year comparison")
public record MaintenanceAnnualCostResponse(
        @Schema(description = "The queried year", example = "2024")
        int year,

        @Schema(description = "Total maintenance cost for the queried year", example = "458200.00")
        BigDecimal total,

        @Schema(description = "Total maintenance cost for the previous year", example = "523000.00")
        BigDecimal previousYearTotal,

        @Schema(description = "Percentage change vs the previous year. Null when previous year total is zero.",
                nullable = true, example = "-12.4")
        Double changePercent
) {}
