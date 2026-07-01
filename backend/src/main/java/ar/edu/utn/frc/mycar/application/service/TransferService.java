package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.MaintenanceLog;
import ar.edu.utn.frc.mycar.domain.entity.TransferLog;
import ar.edu.utn.frc.mycar.domain.entity.TransferToken;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.TransferStatus;
import ar.edu.utn.frc.mycar.domain.repository.MaintenanceLogRepository;
import ar.edu.utn.frc.mycar.domain.repository.TransferLogRepository;
import ar.edu.utn.frc.mycar.domain.repository.TransferTokenRepository;
import ar.edu.utn.frc.mycar.web.dto.response.MaintenanceLogResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferConfirmResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferGenerateResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferHistoryItemResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferPreviewResponse;
import ar.edu.utn.frc.mycar.web.exception.CannotTransferToSelfException;
import ar.edu.utn.frc.mycar.web.exception.TransferTokenInvalidException;
import ar.edu.utn.frc.mycar.web.exception.TransferTokenNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Generates and redeems QR-based vehicle ownership transfer tokens.
 * A token is valid for 48h and single-use; scanning it lets the prospective
 * buyer preview the vehicle's maintenance history before accepting the transfer.
 */
@Service
@RequiredArgsConstructor
public class TransferService {

    private static final int TOKEN_VALIDITY_HOURS = 48;
    private static final int QR_SIZE_PX = 300;

    private final TransferTokenRepository transferTokenRepository;
    private final TransferLogRepository transferLogRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final AlertService alertService;
    private final QrCodeService qrCodeService;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /** Generates a fresh transfer QR for the vehicle, invalidating any previously pending one. */
    @Transactional
    public TransferGenerateResponse generate(String ownerEmail, Long vehicleId) {
        Vehicle vehicle = vehicleService.getEntity(vehicleId, ownerEmail);
        User owner = userService.getEntity(ownerEmail);

        List<TransferToken> pending = transferTokenRepository.findByVehicleIdAndUsedFalse(vehicleId);
        pending.forEach(t -> t.setUsed(true));
        transferTokenRepository.saveAll(pending);

        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS);
        TransferToken transferToken = TransferToken.builder()
                .vehicle(vehicle)
                .generatedBy(owner)
                .token(UUID.randomUUID().toString())
                .expiresAt(expiresAt)
                .build();
        transferTokenRepository.save(transferToken);

        alertService.createAutoAlert(vehicle, owner,
                "Transferencia de titularidad pendiente de confirmación",
                AlertType.DATE, expiresAt.toLocalDate(), null);

        return toGenerateResponse(transferToken);
    }

    /** Returns the vehicle's active (non-expired, non-used) token, or {@code null} if none. Requires current ownership. */
    @Transactional(readOnly = true)
    public TransferGenerateResponse getActiveToken(String ownerEmail, Long vehicleId) {
        vehicleService.getEntity(vehicleId, ownerEmail);

        return transferTokenRepository.findByVehicleIdAndUsedFalse(vehicleId)
                .stream()
                .max(Comparator.comparing(TransferToken::getCreatedAt))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::toGenerateResponse)
                .orElse(null);
    }

    /**
     * Returns every transfer token the authenticated user has ever generated as seller, across all
     * their vehicles — including ones they no longer own after a completed transfer. Not
     * vehicle-scoped, so it stays visible even once ownership has moved on.
     */
    @Transactional(readOnly = true)
    public List<TransferHistoryItemResponse> getHistory(String ownerEmail) {
        return transferTokenRepository
                .findByGeneratedByEmailOrderByCreatedAtDesc(ownerEmail)
                .stream()
                .map(this::toHistoryItem)
                .toList();
    }

    /** Public, read-only preview shown to the prospective buyer after scanning the QR. */
    @Transactional(readOnly = true)
    public TransferPreviewResponse preview(String token) {
        TransferToken transferToken = getValidTokenOrThrow(token);
        Vehicle vehicle = transferToken.getVehicle();

        List<MaintenanceLogResponse> history = maintenanceLogRepository
                .findByVehicleIdOrderByDateDescIdDesc(vehicle.getId())
                .stream()
                .map(this::toMaintenanceResponse)
                .toList();

        return new TransferPreviewResponse(
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getCurrentKm(),
                transferToken.getGeneratedBy().getName(),
                transferToken.getExpiresAt(),
                history
        );
    }

    /** Confirms the transfer: reassigns ownership to the authenticated buyer and closes the token out. */
    @Transactional
    public TransferConfirmResponse confirm(String buyerEmail, String token) {
        TransferToken transferToken = getValidTokenOrThrow(token);
        Vehicle vehicle = transferToken.getVehicle();
        User seller = transferToken.getGeneratedBy();
        User buyer = userService.getEntity(buyerEmail);

        if (buyer.getId().equals(seller.getId())) {
            throw new CannotTransferToSelfException();
        }

        vehicle.setOwner(buyer);
        transferToken.setUsed(true);
        transferTokenRepository.save(transferToken);

        transferLogRepository.save(TransferLog.builder()
                .vehicle(vehicle)
                .fromOwner(seller)
                .toOwner(buyer)
                .token(token)
                .build());

        return new TransferConfirmResponse(
                vehicle.getId(),
                vehicle.getPlate(),
                "Transferencia confirmada. Ahora sos el propietario de este vehículo."
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private TransferToken getValidTokenOrThrow(String token) {
        TransferToken transferToken = transferTokenRepository.findByToken(token)
                .orElseThrow(TransferTokenNotFoundException::new);

        if (transferToken.isUsed()) {
            throw new TransferTokenInvalidException("Este código de transferencia ya fue utilizado.");
        }
        if (transferToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TransferTokenInvalidException("Este código de transferencia expiró.");
        }
        return transferToken;
    }

    private TransferGenerateResponse toGenerateResponse(TransferToken transferToken) {
        String transferUrl = frontendUrl + "/transfer/confirm?token=" + transferToken.getToken();
        String qrCodeBase64 = qrCodeService.generateBase64Png(transferUrl, QR_SIZE_PX);
        return new TransferGenerateResponse(
                transferToken.getToken(), transferToken.getExpiresAt(), transferUrl, qrCodeBase64);
    }

    private TransferHistoryItemResponse toHistoryItem(TransferToken transferToken) {
        TransferStatus status;
        String buyerName = null;
        LocalDateTime completedAt = null;

        if (transferToken.isUsed()) {
            TransferLog log = transferLogRepository.findByToken(transferToken.getToken()).orElse(null);
            if (log != null) {
                status = TransferStatus.COMPLETED;
                buyerName = log.getToOwner().getName();
                completedAt = log.getTransferredAt();
            } else {
                // Superseded by a newer token for the same vehicle before ever being confirmed.
                status = TransferStatus.EXPIRED;
            }
        } else if (transferToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            status = TransferStatus.EXPIRED;
        } else {
            status = TransferStatus.PENDING;
        }

        Vehicle vehicle = transferToken.getVehicle();
        return new TransferHistoryItemResponse(
                transferToken.getId(),
                vehicle.getId(),
                vehicle.getPlate(),
                vehicle.getBrand(),
                vehicle.getModel(),
                buyerName,
                status,
                transferToken.getCreatedAt(),
                transferToken.getExpiresAt(),
                completedAt
        );
    }

    private MaintenanceLogResponse toMaintenanceResponse(MaintenanceLog log) {
        return new MaintenanceLogResponse(
                log.getId(),
                log.getVehicle().getId(),
                log.getSystem(),
                log.getDate(),
                log.getKmAtMaintenance(),
                log.getWorkshop(),
                log.getDescription(),
                log.getCost(),
                log.getNextServiceKm(),
                log.getNextServiceDate(),
                log.getExpense() != null ? log.getExpense().getId() : null,
                log.getCreatedAt()
        );
    }
}
