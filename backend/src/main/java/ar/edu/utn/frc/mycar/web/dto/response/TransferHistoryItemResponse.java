package ar.edu.utn.frc.mycar.web.dto.response;

import ar.edu.utn.frc.mycar.domain.enums.TransferStatus;

import java.time.LocalDateTime;

public record TransferHistoryItemResponse(
        Long tokenId,
        Long vehicleId,
        String vehiclePlate,
        String vehicleBrand,
        String vehicleModel,
        String buyerName,
        TransferStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        LocalDateTime completedAt
) {}
