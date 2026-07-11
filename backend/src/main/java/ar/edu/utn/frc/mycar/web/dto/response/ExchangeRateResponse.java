package ar.edu.utn.frc.mycar.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Day's exchange rate quote between two currencies (CONCESIONARIO dashboard widget)")
public record ExchangeRateResponse(

        @Schema(description = "Base currency ISO code", example = "USD")
        String baseCurrency,

        @Schema(description = "Target currency ISO code", example = "ARS")
        String targetCurrency,

        @Schema(description = "Conversion rate: 1 unit of base currency in target currency", example = "1234.56")
        BigDecimal rate,

        @Schema(description = "Date the quote was fetched")
        LocalDate date
) {}
