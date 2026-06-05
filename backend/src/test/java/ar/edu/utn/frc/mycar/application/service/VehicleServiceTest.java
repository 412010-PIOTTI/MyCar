package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateVehicleRequest;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleResponse;
import ar.edu.utn.frc.mycar.web.exception.DuplicatePlateException;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock VehicleRepository vehicleRepository;
    @Mock UserRepository userRepository;

    @InjectMocks VehicleService vehicleService;

    private static final String OWNER_EMAIL = "ana@example.com";

    private User owner;
    private CreateVehicleRequest validRequest;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L).name("Ana Pérez").email(OWNER_EMAIL).role(Role.USER).build();

        validRequest = new CreateVehicleRequest();
        validRequest.setPlate("ab123cd");
        validRequest.setBrand("Toyota");
        validRequest.setModel("Corolla");
        validRequest.setYear(2020);
        validRequest.setColor("Blanco");
        validRequest.setInitialKm(35000);
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_newPlate_savesNormalisedPlateAndReturnsResponse() {
        Vehicle saved = Vehicle.builder()
                .id(1L).owner(owner)
                .plate("AB123CD").brand("Toyota").model("Corolla")
                .year(2020).color("Blanco").currentKm(35000)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        when(vehicleRepository.existsByPlate("AB123CD")).thenReturn(false);
        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(saved);

        VehicleResponse response = vehicleService.register(OWNER_EMAIL, validRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.plate()).isEqualTo("AB123CD");
        assertThat(response.brand()).isEqualTo("Toyota");
        assertThat(response.model()).isEqualTo("Corolla");
        assertThat(response.year()).isEqualTo(2020);
        assertThat(response.color()).isEqualTo("Blanco");
        assertThat(response.currentKm()).isEqualTo(35000);
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0));

        verify(vehicleRepository).existsByPlate("AB123CD");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void register_plateLowercase_isNormalisedToUppercaseBeforeCheck() {
        // Plate provided in lowercase; existsByPlate must be called with the upper-cased value
        when(vehicleRepository.existsByPlate("AB123CD")).thenReturn(false);
        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(
                Vehicle.builder().id(2L).plate("AB123CD").brand("Toyota")
                        .model("Corolla").year(2020).currentKm(0).owner(owner).build());

        vehicleService.register(OWNER_EMAIL, validRequest);

        verify(vehicleRepository).existsByPlate("AB123CD");
    }

    @Test
    void register_duplicatePlate_throwsDuplicatePlateException() {
        when(vehicleRepository.existsByPlate("AB123CD")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.register(OWNER_EMAIL, validRequest))
                .isInstanceOf(DuplicatePlateException.class)
                .hasMessageContaining("AB123CD");

        verify(userRepository, never()).findByEmail(any());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void register_ownerNotFound_throwsInvalidCredentialsException() {
        when(vehicleRepository.existsByPlate("AB123CD")).thenReturn(false);
        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.register(OWNER_EMAIL, validRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(vehicleRepository, never()).save(any());
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_existingVehicleOwnedByUser_returnsResponse() {
        Vehicle vehicle = Vehicle.builder()
                .id(1L).owner(owner)
                .plate("AB123CD").brand("Toyota").model("Corolla")
                .year(2020).color("Blanco").currentKm(35000)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

        when(vehicleRepository.findByIdAndOwnerEmail(1L, OWNER_EMAIL))
                .thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getById(OWNER_EMAIL, 1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.plate()).isEqualTo("AB123CD");
    }

    @Test
    void getById_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleRepository.findByIdAndOwnerEmail(99L, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById(OWNER_EMAIL, 99L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_vehicleBelongsToAnotherUser_throwsVehicleNotFoundException() {
        // findByIdAndOwnerEmail returns empty when the vehicle exists but owner doesn't match
        when(vehicleRepository.findByIdAndOwnerEmail(1L, "other@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById("other@example.com", 1L))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_ownerWithVehicles_returnsListOrderedByCreatedAtDesc() {
        LocalDateTime older = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime newer = LocalDateTime.of(2025, 6, 1, 0, 0);

        Vehicle v1 = Vehicle.builder().id(1L).owner(owner)
                .plate("AA111AA").brand("Toyota").model("Corolla").year(2020).currentKm(10000)
                .createdAt(older).build();
        Vehicle v2 = Vehicle.builder().id(2L).owner(owner)
                .plate("BB222BB").brand("Honda").model("Civic").year(2021).currentKm(5000)
                .createdAt(newer).build();

        when(vehicleRepository.findByOwnerEmailOrderByCreatedAtDesc(OWNER_EMAIL))
                .thenReturn(List.of(v2, v1));

        List<VehicleResponse> result = vehicleService.getAll(OWNER_EMAIL);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(2L);
        assertThat(result.get(0).plate()).isEqualTo("BB222BB");
        assertThat(result.get(1).id()).isEqualTo(1L);
        assertThat(result.get(1).plate()).isEqualTo("AA111AA");
    }

    @Test
    void getAll_ownerWithNoVehicles_returnsEmptyList() {
        when(vehicleRepository.findByOwnerEmailOrderByCreatedAtDesc(OWNER_EMAIL))
                .thenReturn(List.of());

        List<VehicleResponse> result = vehicleService.getAll(OWNER_EMAIL);

        assertThat(result).isEmpty();
    }
}
