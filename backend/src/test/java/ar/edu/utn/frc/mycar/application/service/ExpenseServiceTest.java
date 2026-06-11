package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Expense;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.ExpenseCategory;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.ExpenseRepository;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateExpenseRequest;
import ar.edu.utn.frc.mycar.web.dto.response.ExpenseResponse;
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
        return Expense.builder()
                .id(10L).vehicle(vehicle).user(owner)
                .category(ExpenseCategory.OPERATIVO)
                .date(LocalDate.now())
                .amount(new BigDecimal("1500.00"))
                .kmAtExpense(km)
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

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_existingVehicle_returnsList() {
        Expense e = buildExpense(CURRENT_KM);
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(expenseRepository.findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(List.of(e));

        List<ExpenseResponse> result = expenseService.getAll(OWNER_EMAIL, VEHICLE_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAll_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(false);

        assertThatThrownBy(() -> expenseService.getAll(OWNER_EMAIL, VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_existingExpense_returnsResponse() {
        Expense e = buildExpense(CURRENT_KM);
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(expenseRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(10L, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(e));

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
