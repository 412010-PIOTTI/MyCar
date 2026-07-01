package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Alert;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.UrgencyLevel;
import ar.edu.utn.frc.mycar.domain.repository.AlertRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateAlertRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateAlertRequest;
import ar.edu.utn.frc.mycar.web.dto.response.AlertResponse;
import ar.edu.utn.frc.mycar.web.exception.AlertNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final VehicleService vehicleService;
    private final UserService userService;

    @Transactional
    public AlertResponse create(String ownerEmail, Long vehicleId, CreateAlertRequest request) {
        Vehicle vehicle = vehicleService.getEntity(vehicleId, ownerEmail);
        User user = userService.getEntity(ownerEmail);

        int advanceDays = request.getAdvanceDays() != null ? request.getAdvanceDays() : 30;
        UrgencyLevel urgency = computeUrgency(
                request.getAlertType(), request.getAlertDate(), request.getAlertKm(),
                advanceDays, vehicle.getCurrentKm());

        Alert alert = Alert.builder()
                .vehicle(vehicle)
                .user(user)
                .title(request.getTitle())
                .alertType(request.getAlertType())
                .alertDate(request.getAlertDate())
                .alertKm(request.getAlertKm())
                .advanceDays(advanceDays)
                .urgencyLevel(urgency)
                .build();

        return toResponse(alertRepository.save(alert), vehicle.getCurrentKm());
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAll(String ownerEmail, Long vehicleId) {
        Vehicle vehicle = vehicleService.getEntity(vehicleId, ownerEmail);
        int currentKm = vehicle.getCurrentKm();

        return alertRepository
                .findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByCreatedAtDesc(vehicleId, ownerEmail)
                .stream()
                .map(a -> toResponse(a, currentKm))
                .sorted((a, b) -> urgencyOrder(b.urgencyLevel()) - urgencyOrder(a.urgencyLevel()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertResponse getById(String ownerEmail, Long vehicleId, Long alertId) {
        Vehicle vehicle = vehicleService.getEntity(vehicleId, ownerEmail);
        Alert alert = alertRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(alertId, vehicleId, ownerEmail)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
        return toResponse(alert, vehicle.getCurrentKm());
    }

    @Transactional
    public AlertResponse update(String ownerEmail, Long vehicleId, Long alertId, UpdateAlertRequest request) {
        Vehicle vehicle = vehicleService.getEntity(vehicleId, ownerEmail);
        Alert alert = alertRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(alertId, vehicleId, ownerEmail)
                .orElseThrow(() -> new AlertNotFoundException(alertId));

        if (request.getTitle()      != null) alert.setTitle(request.getTitle());
        if (request.getAlertDate()  != null) alert.setAlertDate(request.getAlertDate());
        if (request.getAlertKm()    != null) alert.setAlertKm(request.getAlertKm());
        if (request.getAdvanceDays()!= null) alert.setAdvanceDays(request.getAdvanceDays());
        if (request.getActive()     != null) alert.setActive(request.getActive());

        alert.setUrgencyLevel(computeUrgency(
                alert.getAlertType(), alert.getAlertDate(), alert.getAlertKm(),
                alert.getAdvanceDays(), vehicle.getCurrentKm()));

        return toResponse(alertRepository.save(alert), vehicle.getCurrentKm());
    }

    @Transactional
    public void delete(String ownerEmail, Long vehicleId, Long alertId) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        Alert alert = alertRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(alertId, vehicleId, ownerEmail)
                .orElseThrow(() -> new AlertNotFoundException(alertId));
        alertRepository.delete(alert);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private AlertResponse toResponse(Alert alert, int currentKm) {
        UrgencyLevel liveUrgency = computeUrgency(
                alert.getAlertType(), alert.getAlertDate(), alert.getAlertKm(),
                alert.getAdvanceDays(), currentKm);
        return new AlertResponse(
                alert.getId(),
                alert.getVehicle().getId(),
                alert.getTitle(),
                alert.getAlertType(),
                alert.getAlertDate(),
                alert.getAlertKm(),
                alert.getAdvanceDays(),
                liveUrgency,
                alert.isActive(),
                alert.getCreatedAt()
        );
    }

    private UrgencyLevel computeUrgency(AlertType type, LocalDate alertDate, Integer alertKm,
                                         int advanceDays, int currentKm) {
        if (type == AlertType.DATE && alertDate != null) {
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), alertDate);
            if (daysUntil < 0)          return UrgencyLevel.URGENTE;
            if (daysUntil <= advanceDays) return UrgencyLevel.ADVERTENCIA;
            return UrgencyLevel.INFORMATIVA;
        }
        if (type == AlertType.KM && alertKm != null) {
            int kmLeft = alertKm - currentKm;
            if (kmLeft <= 0)    return UrgencyLevel.URGENTE;
            if (kmLeft <= 1000) return UrgencyLevel.ADVERTENCIA;
            return UrgencyLevel.INFORMATIVA;
        }
        return UrgencyLevel.INFORMATIVA;
    }

    private int urgencyOrder(UrgencyLevel level) {
        return switch (level) {
            case URGENTE     -> 2;
            case ADVERTENCIA -> 1;
            case INFORMATIVA -> 0;
        };
    }
}
