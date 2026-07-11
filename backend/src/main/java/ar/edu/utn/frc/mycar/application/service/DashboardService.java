package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.DocumentStatus;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.infrastructure.exchangerate.ExchangeRateService;
import ar.edu.utn.frc.mycar.web.dto.response.DashboardResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.MaintenanceLogResponse;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleDashboardSummary;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Assembles the main-screen dashboard by composing the existing per-module services — no data access of its own. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int MAX_DOCUMENTS_PER_VEHICLE = 3;

    private final VehicleService vehicleService;
    private final AlertService alertService;
    private final DocumentService documentService;
    private final MaintenanceLogService maintenanceLogService;
    private final ExpenseService expenseService;
    private final UserService userService;
    private final ExchangeRateService exchangeRateService;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String ownerEmail) {
        List<VehicleDashboardSummary> vehicles = vehicleService.getAll(ownerEmail).stream()
                .map(v -> buildVehicleSummary(ownerEmail, v))
                .toList();

        LocalDate today = LocalDate.now();
        var monthSummary = expenseService.getSummaryForOwner(ownerEmail, today.getYear(), today.getMonthValue());
        var yearTotal = expenseService.getYearTotalForOwner(ownerEmail, today.getYear());

        User user = userService.getEntity(ownerEmail);
        var exchangeRate = user.getRole() == Role.CONCESIONARIO
                ? exchangeRateService.getTodayRate("USD", "ARS").orElse(null)
                : null;

        return new DashboardResponse(vehicles, monthSummary.totalCurrentMonth(), yearTotal, exchangeRate);
    }

    private VehicleDashboardSummary buildVehicleSummary(String ownerEmail, VehicleResponse vehicle) {
        List<DocumentResponse> keyDocuments = documentService.getAll(ownerEmail, vehicle.id()).stream()
                .sorted(Comparator.comparingInt((DocumentResponse d) -> statusPriority(d.status()))
                        .thenComparing(DocumentResponse::expiryDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(MAX_DOCUMENTS_PER_VEHICLE)
                .toList();

        MaintenanceLogResponse lastService = maintenanceLogService.getAll(ownerEmail, vehicle.id()).stream()
                .findFirst()
                .orElse(null);

        return new VehicleDashboardSummary(
                vehicle.id(),
                vehicle.plate(),
                vehicle.brand(),
                vehicle.model(),
                vehicle.currentKm(),
                alertService.getAll(ownerEmail, vehicle.id()),
                keyDocuments,
                lastService
        );
    }

    private int statusPriority(DocumentStatus status) {
        return switch (status) {
            case VENCIDO    -> 0;
            case POR_VENCER -> 1;
            case VIGENTE    -> 2;
            case SIN_FECHA  -> 3;
        };
    }
}
