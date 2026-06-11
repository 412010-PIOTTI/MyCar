package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.util.Map;

public record ExpenseSummaryResponse(
        BigDecimal totalCurrentMonth,
        BigDecimal totalPreviousMonth,
        Double percentageChange,
        Map<ExpenseCategory, BigDecimal> byCategory
) {}
