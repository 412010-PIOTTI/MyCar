package ar.edu.utn.frc.mycar.web.dto.response;

public record TransferConfirmResponse(
        Long vehicleId,
        String vehiclePlate,
        String message
) {}
