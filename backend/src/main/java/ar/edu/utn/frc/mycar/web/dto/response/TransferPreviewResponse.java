package ar.edu.utn.frc.mycar.web.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/** Public, read-only view shown to the prospective buyer after scanning the QR. */
public record TransferPreviewResponse(
        String vehiclePlate,
        String vehicleBrand,
        String vehicleModel,
        Integer vehicleYear,
        String vehicleColor,
        Integer currentKm,
        String sellerName,
        LocalDateTime expiresAt,
        List<MaintenanceLogResponse> maintenanceHistory
) {}
