package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.UrgencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Alert associated with a vehicle")
public record AlertResponse(
        Long id,
        Long vehicleId,
        String title,
        AlertType alertType,
        LocalDate alertDate,
        Integer alertKm,
        Integer advanceDays,
        UrgencyLevel urgencyLevel,
        boolean active,
        LocalDateTime createdAt
) {}
