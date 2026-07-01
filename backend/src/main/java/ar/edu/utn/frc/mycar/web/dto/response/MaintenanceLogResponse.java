package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.MaintenanceSystem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MaintenanceLogResponse(
        Long id,
        Long vehicleId,
        MaintenanceSystem system,
        LocalDate date,
        Integer kmAtMaintenance,
        String workshop,
        String description,
        BigDecimal cost,
        Integer nextServiceKm,
        LocalDate nextServiceDate,
        Long expenseId,
        LocalDateTime createdAt
) {}
