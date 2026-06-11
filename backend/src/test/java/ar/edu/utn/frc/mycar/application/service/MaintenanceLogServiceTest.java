package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.MaintenanceLog;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.MaintenanceType;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.MaintenanceLogRepository;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateMaintenanceLogRequest;
import ar.edu.utn.frc.mycar.web.dto.response.MaintenanceLogResponse;
import ar.edu.utn.frc.mycar.web.exception.MaintenanceLogNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceLogServiceTest {

    @Mock MaintenanceLogRepository maintenanceLogRepository;
    @Mock VehicleRepository vehicleRepository;
    @Mock UserService userService;

    @InjectMocks MaintenanceLogService maintenanceLogService;

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

    private CreateMaintenanceLogRequest buildRequest(int km) {
        CreateMaintenanceLogRequest req = new CreateMaintenanceLogRequest();
        req.setType(MaintenanceType.ACEITE);
        req.setDate(LocalDate.now());
        req.setKmAtMaintenance(km);
        return req;
    }

    private MaintenanceLog buildLog(int km) {
        return MaintenanceLog.builder()
                .id(20L).vehicle(vehicle).user(owner)
                .type(MaintenanceType.ACEITE)
                .date(LocalDate.now())
                .kmAtMaintenance(km)
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
        when(maintenanceLogRepository.save(any(MaintenanceLog.class))).thenReturn(buildLog(newKm));

        maintenanceLogService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(newKm));

        assertThat(vehicle.getCurrentKm()).isEqualTo(newKm);
    }

    @Test
    void create_withKmEqualToCurrent_doesNotUpdateVehicleKm() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(maintenanceLogRepository.save(any(MaintenanceLog.class))).thenReturn(buildLog(CURRENT_KM));

        maintenanceLogService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(CURRENT_KM));

        assertThat(vehicle.getCurrentKm()).isEqualTo(CURRENT_KM);
    }

    @Test
    void create_withKmLessThanCurrent_doesNotUpdateVehicleKm() {
        int lowerKm = CURRENT_KM - 1000;
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(maintenanceLogRepository.save(any(MaintenanceLog.class))).thenReturn(buildLog(lowerKm));

        maintenanceLogService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(lowerKm));

        assertThat(vehicle.getCurrentKm()).isEqualTo(CURRENT_KM);
    }

    @Test
    void create_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceLogService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(35000)))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(maintenanceLogRepository, never()).save(any());
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_existingVehicle_returnsList() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(maintenanceLogRepository.findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(List.of(buildLog(CURRENT_KM)));

        List<MaintenanceLogResponse> result = maintenanceLogService.getAll(OWNER_EMAIL, VEHICLE_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAll_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(false);

        assertThatThrownBy(() -> maintenanceLogService.getAll(OWNER_EMAIL, VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_existingLog_returnsResponse() {
        MaintenanceLog log = buildLog(CURRENT_KM);
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(maintenanceLogRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(20L, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(log));

        MaintenanceLogResponse response = maintenanceLogService.getById(OWNER_EMAIL, VEHICLE_ID, 20L);

        assertThat(response.id()).isEqualTo(20L);
    }

    @Test
    void getById_logNotFound_throwsMaintenanceLogNotFoundException() {
        when(vehicleRepository.existsByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL)).thenReturn(true);
        when(maintenanceLogRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(99L, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceLogService.getById(OWNER_EMAIL, VEHICLE_ID, 99L))
                .isInstanceOf(MaintenanceLogNotFoundException.class);
    }
}
