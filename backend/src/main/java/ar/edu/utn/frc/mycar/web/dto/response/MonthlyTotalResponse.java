package ar.edu.utn.frc.mycar.web.dto.response;

import java.math.BigDecimal;

public record MonthlyTotalResponse(
        int month,
        BigDecimal total
) {}
