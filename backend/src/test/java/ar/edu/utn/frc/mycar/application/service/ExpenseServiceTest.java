package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Expense;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;
import ar.edu.utn.frc.mycar.domain.enums.ExpenseStatus;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.ExpenseRepository;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateExpenseRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseResponse;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseSummaryResponse;
import ar.edu.utn.frc.mycar.web.dto.response.MonthlyTotalResponse;
import ar.edu.utn.frc.mycar.web.exception.ExpenseNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock ExpenseRepository expenseRepository;
    @Mock VehicleRepository vehicleRepository;
    @Mock UserService userService;

    @InjectMocks ExpenseService expenseService;

    private static final String OWNER_EMAIL = "ana@example.com";
    private static final Long VEHICLE_ID = 1L;
    private static final int CURRENT_KM = 30000;

    private User owner;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Ana").email(OWNER_EMAIL).role(Role.USER).build();
        vehicle = Vehicle.builder()
                .id(VEHICLE_ID).owner(owner)
                .plate("AB123CD").brand("Toyota").model("Corolla")
                .year(2020).currentKm(CURRENT_KM).active(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();
    }

    private CreateExpenseRequest buildRequest(Integer km) {
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setCategory(ExpenseCategory.OPERATIVO);
        req.setDate(LocalDate.now());
        req.setAmount(new BigDecimal("1500.00"));
        req.setKmAtExpense(km);
        return req;
    }

    private Expense buildExpense(Integer km) {
        return buildExpense(km, null);
    }

    private Expense buildExpense(Integer km, LocalDate expiryDate) {
        return Expense.builder()
                .id(10L).vehicle(vehicle).user(owner)
                .category(ExpenseCategory.OPERATIVO)
                .date(LocalDate.now())
                .amount(new BigDecimal("1500.00"))
                .kmAtExpense(km)
                .expiryDate(expiryDate)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── km update logic ───────────────────────────────────────────────────────

    @Test
    void create_withKmGreaterThanCurrent_updatesVehicleKm() {
        int newKm = CURRENT_KM + 5000;
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenReturn(buildExpense(newKm));

        expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(newKm));

        assertThat(vehicle.getCurrentKm()).isEqualTo(newKm);
    }

    @Test
    void create_withKmEqualToCurrent_doesNotUpdateVehicleKm() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenReturn(buildExpense(CURRENT_KM));

        expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(CURRENT_KM));

        assertThat(vehicle.getCurrentKm()).isEqualTo(CURRENT_KM);
    }

    @Test
    void create_withKmLessThanCurrent_doesNotUpdateVehicleKm() {
        int lowerKm = CURRENT_KM - 1000;
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenReturn(buildExpense(lowerKm));

        expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(lowerKm));

        assertThat(vehicle.getCurrentKm()).isEqualTo(CURRENT_KM);
    }

    @Test
    void create_withNullKm_doesNotUpdateVehicleKm() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenReturn(buildExpense(null));

        expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(null));

        assertThat(vehicle.getCurrentKm()).isEqualTo(CURRENT_KM);
    }

    @Test
    void create_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(35000)))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(expenseRepository, never()).save(any());
    }

    // ── status computation ────────────────────────────────────────────────────

    @Test
    void create_withExpiryDateInFarFuture_statusIsVigente() {
        LocalDate farFuture = LocalDate.now().plusDays(60);
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenReturn(buildExpense(null, farFuture));

        ExpenseResponse response = expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(null));

        assertThat(response.status()).isEqualTo(ExpenseStatus.VIGENTE);
    }

    @Test
    void create_withExpiryDateIn15Days_statusIsPorVencer() {
        LocalDate soon = LocalDate.now().plusDays(15);
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenReturn(buildExpense(null, soon));

        ExpenseResponse response = expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(null));

        assertThat(response.status()).isEqualTo(ExpenseStatus.POR_VENCER);
    }

    @Test
    void create_withPastExpiryDate_statusIsVencido() {
        LocalDate past = LocalDate.now().minusDays(5);
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenReturn(buildExpense(null, past));

        ExpenseResponse response = expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(null));

        assertThat(response.status()).isEqualTo(ExpenseStatus.VENCIDO);
    }

    @Test
    void create_withNoExpiryDate_statusIsNull() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(expenseRepository.save(any(Expense.class))).thenReturn(buildExpense(null, null));

        ExpenseResponse response = expenseService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(null));

        assertThat(response.status()).isNull();
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_noCategoryFilter_returnsAllExpenses() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(expenseRepository.findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(List.of(buildExpense(CURRENT_KM)));

        List<ExpenseResponse> result = expenseService.getAll(OWNER_EMAIL, VEHICLE_ID, null);

        assertThat(result).hasSize(1);
        verify(expenseRepository).findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(VEHICLE_ID, OWNER_EMAIL);
        verify(expenseRepository, never()).findByVehicleIdAndVehicleOwnerEmailAndCategoryOrderByDateDescIdDesc(any(), any(), any());
    }

    @Test
    void getAll_withCategoryFilter_delegatesToFilteredQuery() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(expenseRepository.findByVehicleIdAndVehicleOwnerEmailAndCategoryOrderByDateDescIdDesc(
                VEHICLE_ID, OWNER_EMAIL, ExpenseCategory.OPERATIVO))
                .thenReturn(List.of(buildExpense(CURRENT_KM)));

        List<ExpenseResponse> result = expenseService.getAll(OWNER_EMAIL, VEHICLE_ID, ExpenseCategory.OPERATIVO);

        assertThat(result).hasSize(1);
        verify(expenseRepository).findByVehicleIdAndVehicleOwnerEmailAndCategoryOrderByDateDescIdDesc(
                VEHICLE_ID, OWNER_EMAIL, ExpenseCategory.OPERATIVO);
    }

    @Test
    void getAll_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(false);

        assertThatThrownBy(() -> expenseService.getAll(OWNER_EMAIL, VEHICLE_ID, null))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getSummary ────────────────────────────────────────────────────────────

    @Test
    void getSummary_withPreviousMonthData_computesPercentageChange() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(expenseRepository.sumByVehicleAndYearMonth(VEHICLE_ID, OWNER_EMAIL, 2024, 6))
                .thenReturn(new BigDecimal("110000"));
        when(expenseRepository.sumByVehicleAndYearMonth(VEHICLE_ID, OWNER_EMAIL, 2024, 5))
                .thenReturn(new BigDecimal("100000"));
        when(expenseRepository.sumByCategoryForYearMonth(VEHICLE_ID, OWNER_EMAIL, 2024, 6))
                .thenReturn(List.of());

        ExpenseSummaryResponse summary = expenseService.getSummary(OWNER_EMAIL, VEHICLE_ID, 2024, 6);

        assertThat(summary.totalCurrentMonth()).isEqualByComparingTo("110000");
        assertThat(summary.percentageChange()).isEqualTo(10.0);
    }

    @Test
    void getSummary_withNoPreviousMonth_percentageChangeIsNull() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(expenseRepository.sumByVehicleAndYearMonth(VEHICLE_ID, OWNER_EMAIL, 2024, 1))
                .thenReturn(new BigDecimal("50000"));
        when(expenseRepository.sumByVehicleAndYearMonth(VEHICLE_ID, OWNER_EMAIL, 2023, 12))
                .thenReturn(BigDecimal.ZERO);
        when(expenseRepository.sumByCategoryForYearMonth(VEHICLE_ID, OWNER_EMAIL, 2024, 1))
                .thenReturn(List.of());

        ExpenseSummaryResponse summary = expenseService.getSummary(OWNER_EMAIL, VEHICLE_ID, 2024, 1);

        assertThat(summary.percentageChange()).isNull();
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_existingExpense_returnsResponse() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(expenseRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(10L, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(buildExpense(CURRENT_KM)));

        ExpenseResponse response = expenseService.getById(OWNER_EMAIL, VEHICLE_ID, 10L);

        assertThat(response.id()).isEqualTo(10L);
    }

    @Test
    void getById_expenseNotFound_throwsExpenseNotFoundException() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(expenseRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(99L, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getById(OWNER_EMAIL, VEHICLE_ID, 99L))
                .isInstanceOf(ExpenseNotFoundException.class);
    }
}
