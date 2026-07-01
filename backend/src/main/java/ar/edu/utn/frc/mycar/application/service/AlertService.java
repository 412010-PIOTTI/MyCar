package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Alert;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.UrgencyLevel;
import ar.edu.utn.frc.mycar.domain.repository.AlertRepository;
import ar.edu.utn.frc.mycar.infrastructure.email.EmailService;
import ar.edu.utn.frc.mycar.web.dto.request.CreateAlertRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateAlertRequest;
import ar.edu.utn.frc.mycar.web.dto.response.AlertResponse;
import ar.edu.utn.frc.mycar.web.exception.AlertNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final EmailService emailService;

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

    // ── Scheduler ─────────────────────────────────────────────────────────────

    /** Runs every day at 8 AM — checks URGENTE alerts and sends one email per trigger. */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void checkAndNotifyUrgentAlerts() {
        List<Alert> activeAlerts = alertRepository.findAllActiveWithDetails();
        int sent = 0;
        int reset = 0;

        for (Alert alert : activeAlerts) {
            int currentKm = alert.getVehicle().getCurrentKm();
            UrgencyLevel urgency = computeUrgency(
                    alert.getAlertType(), alert.getAlertDate(), alert.getAlertKm(),
                    alert.getAdvanceDays(), currentKm);

            if (urgency == UrgencyLevel.URGENTE && !alert.isNotified()) {
                try {
                    emailService.sendAlertNotification(
                            alert.getUser().getEmail(),
                            alert.getUser().getName(),
                            alert.getTitle(),
                            buildVehicleDesc(alert),
                            buildAlertDetail(alert, currentKm));
                    alert.setNotified(true);
                    alert.setNotifiedAt(LocalDateTime.now());
                    alertRepository.save(alert);
                    sent++;
                } catch (Exception e) {
                    log.error("Error al notificar alerta {}: {}", alert.getId(), e.getMessage());
                }
            } else if (urgency != UrgencyLevel.URGENTE && alert.isNotified()) {
                // Reset so user gets notified again if the alert becomes urgent in the future
                alert.setNotified(false);
                alert.setNotifiedAt(null);
                alertRepository.save(alert);
                reset++;
            }
        }
        log.info("Scheduler alertas: {} enviadas, {} reseteadas", sent, reset);
    }

    private String buildVehicleDesc(Alert alert) {
        Vehicle v = alert.getVehicle();
        return v.getBrand() + " " + v.getModel() + " (" + v.getPlate() + ")";
    }

    private String buildAlertDetail(Alert alert, int currentKm) {
        if (alert.getAlertType() == AlertType.DATE && alert.getAlertDate() != null) {
            return "📅 Venció el: " + alert.getAlertDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        if (alert.getAlertType() == AlertType.KM && alert.getAlertKm() != null) {
            return "⊙ KM actual: " + currentKm + " km  |  Límite: " + alert.getAlertKm() + " km";
        }
        return "";
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

    @Transactional
    public void createAutoAlert(Vehicle vehicle, User user, String title,
                                AlertType alertType, LocalDate alertDate, Integer alertKm) {
        int advanceDays = 30;
        UrgencyLevel urgency = computeUrgency(alertType, alertDate, alertKm, advanceDays, vehicle.getCurrentKm());
        Alert alert = Alert.builder()
                .vehicle(vehicle)
                .user(user)
                .title(title)
                .alertType(alertType)
                .alertDate(alertDate)
                .alertKm(alertKm)
                .advanceDays(advanceDays)
                .urgencyLevel(urgency)
                .build();
        alertRepository.save(alert);
        log.debug("Auto-alerta creada: {} ({}) para vehículo {}", title, alertType, vehicle.getId());
    }

    private int urgencyOrder(UrgencyLevel level) {
        return switch (level) {
            case URGENTE     -> 2;
            case ADVERTENCIA -> 1;
            case INFORMATIVA -> 0;
        };
    }
}
