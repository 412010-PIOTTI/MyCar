package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.DocumentStatus;
import ar.edu.utn.frc.mycar.domain.enums.DocumentType;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.enums.UrgencyLevel;
import ar.edu.utn.frc.mycar.infrastructure.exchangerate.ExchangeRateService;
import ar.edu.utn.frc.mycar.web.dto.response.AlertResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DashboardResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.ExchangeRateResponse;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseSummaryResponse;
import ar.edu.utn.frc.mycar.web.dto.response.MaintenanceLogResponse;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleDashboardSummary;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock VehicleService vehicleService;
    @Mock AlertService alertService;
    @Mock DocumentService documentService;
    @Mock MaintenanceLogService maintenanceLogService;
    @Mock ExpenseService expenseService;
    @Mock UserService userService;
    @Mock ExchangeRateService exchangeRateService;

    @InjectMocks DashboardService dashboardService;

    private static final String OWNER_EMAIL = "ana@example.com";
    private static final Long VEHICLE_ID = 1L;

    private VehicleResponse vehicle;

    @BeforeEach
    void setUp() {
        vehicle = new VehicleResponse(VEHICLE_ID, "AB123CD", "Volkswagen", "Gol", 2019, "Blanco",
                46812, LocalDateTime.of(2025, 1, 1, 0, 0));
    }

    private AlertResponse buildAlert(UrgencyLevel urgency) {
        return new AlertResponse(1L, VEHICLE_ID, "Vencimiento de Seguro", AlertType.DATE,
                LocalDate.now(), null, 30, urgency, true, LocalDateTime.now());
    }

    private DocumentResponse buildDocument(Long id, DocumentStatus status, LocalDate expiryDate) {
        return new DocumentResponse(id, VEHICLE_ID, DocumentType.SEGURO, "123", LocalDate.now(),
                expiryDate, null, status, false, null, LocalDateTime.now(), null);
    }

    private MaintenanceLogResponse buildLog(LocalDate date) {
        return new MaintenanceLogResponse(1L, VEHICLE_ID, ar.edu.utn.frc.mycar.domain.enums.MaintenanceSystem.MOTOR,
                date, 45200, "Taller Central", "Cambio de aceite", new BigDecimal("50000"), 55000, null, null,
                LocalDateTime.now());
    }

    @Test
    void assemblesVehicleSummaryFromExistingServices() {
        when(vehicleService.getAll(OWNER_EMAIL)).thenReturn(List.of(vehicle));
        List<AlertResponse> alerts = List.of(buildAlert(UrgencyLevel.URGENTE));
        when(alertService.getAll(OWNER_EMAIL, VEHICLE_ID)).thenReturn(alerts);

        DocumentResponse vencido = buildDocument(1L, DocumentStatus.VENCIDO, LocalDate.now().minusDays(5));
        DocumentResponse vigente = buildDocument(2L, DocumentStatus.VIGENTE, LocalDate.now().plusDays(200));
        DocumentResponse porVencer = buildDocument(3L, DocumentStatus.POR_VENCER, LocalDate.now().plusDays(10));
        when(documentService.getAll(OWNER_EMAIL, VEHICLE_ID)).thenReturn(List.of(vigente, vencido, porVencer));

        MaintenanceLogResponse lastLog = buildLog(LocalDate.of(2023, 10, 15));
        when(maintenanceLogService.getAll(OWNER_EMAIL, VEHICLE_ID)).thenReturn(List.of(lastLog));

        when(expenseService.getSummaryForOwner(eq(OWNER_EMAIL), anyInt(), anyInt()))
                .thenReturn(new ExpenseSummaryResponse(new BigDecimal("142500"), BigDecimal.ZERO, null, Map.of()));
        when(expenseService.getYearTotalForOwner(eq(OWNER_EMAIL), anyInt()))
                .thenReturn(new BigDecimal("980000"));

        User user = User.builder().id(1L).name("Ana").email(OWNER_EMAIL).role(Role.USER).build();
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(user);

        DashboardResponse response = dashboardService.getDashboard(OWNER_EMAIL);

        assertThat(response.vehicles()).hasSize(1);
        VehicleDashboardSummary summary = response.vehicles().get(0);
        assertThat(summary.vehicleId()).isEqualTo(VEHICLE_ID);
        assertThat(summary.currentKm()).isEqualTo(46812);
        assertThat(summary.activeAlerts()).isEqualTo(alerts);
        assertThat(summary.lastService()).isEqualTo(lastLog);
        // Documents ordered VENCIDO > POR_VENCER > VIGENTE
        assertThat(summary.documents()).extracting(DocumentResponse::id).containsExactly(1L, 3L, 2L);

        assertThat(response.totalExpensesMonth()).isEqualByComparingTo("142500");
        assertThat(response.totalExpensesYear()).isEqualByComparingTo("980000");
        assertThat(response.exchangeRate()).isNull();
        verifyNoInteractions(exchangeRateService);
    }

    @Test
    void includesExchangeRateOnlyForConcesionario() {
        when(vehicleService.getAll(OWNER_EMAIL)).thenReturn(List.of());
        when(expenseService.getSummaryForOwner(eq(OWNER_EMAIL), anyInt(), anyInt()))
                .thenReturn(new ExpenseSummaryResponse(BigDecimal.ZERO, BigDecimal.ZERO, null, Map.of()));
        when(expenseService.getYearTotalForOwner(eq(OWNER_EMAIL), anyInt())).thenReturn(BigDecimal.ZERO);

        User concesionario = User.builder().id(2L).name("Concesionaria SA").email(OWNER_EMAIL)
                .role(Role.CONCESIONARIO).build();
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(concesionario);

        ExchangeRateResponse rate = new ExchangeRateResponse("USD", "ARS", new BigDecimal("1234.56"), LocalDate.now());
        when(exchangeRateService.getTodayRate("USD", "ARS")).thenReturn(Optional.of(rate));

        DashboardResponse response = dashboardService.getDashboard(OWNER_EMAIL);

        assertThat(response.exchangeRate()).isEqualTo(rate);
    }
}
