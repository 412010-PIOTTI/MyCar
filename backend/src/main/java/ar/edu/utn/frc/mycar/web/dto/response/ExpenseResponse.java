package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        Long vehicleId,
        ExpenseCategory category,
        String subcategory,
        LocalDate date,
        BigDecimal amount,
        String description,
        Integer kmAtExpense,
        LocalDateTime createdAt
) {}
