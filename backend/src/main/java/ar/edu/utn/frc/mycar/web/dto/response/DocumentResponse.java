package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.DocumentStatus;
import ar.edu.utn.frc.mycar.domain.enums.DocumentType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DocumentResponse(
        Long id,
        Long vehicleId,
        DocumentType type,
        String referenceNumber,
        LocalDate issueDate,
        LocalDate expiryDate,
        String notes,
        DocumentStatus status,
        boolean hasFile,
        String originalFileName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
