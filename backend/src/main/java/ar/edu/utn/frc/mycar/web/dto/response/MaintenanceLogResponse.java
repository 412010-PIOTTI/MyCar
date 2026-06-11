package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.MaintenanceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MaintenanceLogResponse(
        Long id,
        Long vehicleId,
        MaintenanceType type,
        LocalDate date,
        Integer kmAtMaintenance,
        String description,
        BigDecimal cost,
        LocalDateTime createdAt
) {}
