package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.VehicleRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateVehicleRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateVehicleRequest;
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
    @Mock UserService userService;

    @InjectMocks VehicleService vehicleService;

    private static final String OWNER_EMAIL = "ana@example.com";
    private static final Long VEHICLE_ID = 1L;

    private User owner;
    private Vehicle existingVehicle;
    private CreateVehicleRequest validRequest;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L).name("Ana Pérez").email(OWNER_EMAIL).role(Role.USER).build();

        existingVehicle = Vehicle.builder()
                .id(VEHICLE_ID).owner(owner)
                .plate("AB123CD").brand("Toyota").model("Corolla")
                .year(2020).color("Blanco").currentKm(35000)
                .active(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();

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
        when(vehicleRepository.existsByPlate("AB123CD")).thenReturn(false);
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(existingVehicle);

        VehicleResponse response = vehicleService.register(OWNER_EMAIL, validRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.plate()).isEqualTo("AB123CD");
        assertThat(response.brand()).isEqualTo("Toyota");
        assertThat(response.model()).isEqualTo("Corolla");
        assertThat(response.year()).isEqualTo(2020);
        assertThat(response.color()).isEqualTo("Blanco");
        assertThat(response.currentKm()).isEqualTo(35000);

        verify(vehicleRepository).existsByPlate("AB123CD");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void register_plateLowercase_isNormalisedToUppercaseBeforeCheck() {
        when(vehicleRepository.existsByPlate("AB123CD")).thenReturn(false);
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(existingVehicle);

        vehicleService.register(OWNER_EMAIL, validRequest);

        verify(vehicleRepository).existsByPlate("AB123CD");
    }

    @Test
    void register_duplicatePlate_throwsDuplicatePlateException() {
        when(vehicleRepository.existsByPlate("AB123CD")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.register(OWNER_EMAIL, validRequest))
                .isInstanceOf(DuplicatePlateException.class)
                .hasMessageContaining("AB123CD");

        verify(userService, never()).getEntity(any());
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void register_ownerNotFound_throwsInvalidCredentialsException() {
        when(vehicleRepository.existsByPlate("AB123CD")).thenReturn(false);
        when(userService.getEntity(OWNER_EMAIL)).thenThrow(new InvalidCredentialsException());

        assertThatThrownBy(() -> vehicleService.register(OWNER_EMAIL, validRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(vehicleRepository, never()).save(any());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_allFields_updatesAndReturnsResponse() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setPlate("xx999yy");
        request.setBrand("Honda");
        request.setModel("Civic");
        request.setYear(2022);
        request.setColor("Negro");

        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.existsByPlateAndActiveTrueAndIdNot("XX999YY", VEHICLE_ID))
                .thenReturn(false);

        VehicleResponse response = vehicleService.update(OWNER_EMAIL, VEHICLE_ID, request);

        assertThat(response.plate()).isEqualTo("XX999YY");
        assertThat(response.brand()).isEqualTo("Honda");
        assertThat(response.model()).isEqualTo("Civic");
        assertThat(response.year()).isEqualTo(2022);
        assertThat(response.color()).isEqualTo("Negro");
    }

    @Test
    void update_onlyBrand_leavesOtherFieldsUnchanged() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setBrand("Ford");

        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(existingVehicle));

        VehicleResponse response = vehicleService.update(OWNER_EMAIL, VEHICLE_ID, request);

        assertThat(response.brand()).isEqualTo("Ford");
        assertThat(response.plate()).isEqualTo("AB123CD");
        assertThat(response.model()).isEqualTo("Corolla");
        assertThat(response.year()).isEqualTo(2020);
        assertThat(response.color()).isEqualTo("Blanco");

        verify(vehicleRepository, never()).existsByPlateAndActiveTrueAndIdNot(any(), any());
    }

    @Test
    void update_plateNormalisation_storesUppercase() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setPlate("xx999yy");

        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.existsByPlateAndActiveTrueAndIdNot("XX999YY", VEHICLE_ID))
                .thenReturn(false);

        VehicleResponse response = vehicleService.update(OWNER_EMAIL, VEHICLE_ID, request);

        assertThat(response.plate()).isEqualTo("XX999YY");
        verify(vehicleRepository).existsByPlateAndActiveTrueAndIdNot("XX999YY", VEHICLE_ID);
    }

    @Test
    void update_sameOwnPlate_doesNotThrowDuplicatePlateException() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setPlate("AB123CD");

        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(existingVehicle));
        // IdNot excludes this vehicle's own plate → no conflict
        when(vehicleRepository.existsByPlateAndActiveTrueAndIdNot("AB123CD", VEHICLE_ID))
                .thenReturn(false);

        VehicleResponse response = vehicleService.update(OWNER_EMAIL, VEHICLE_ID, request);

        assertThat(response.plate()).isEqualTo("AB123CD");
    }

    @Test
    void update_duplicatePlateFromAnotherVehicle_throwsDuplicatePlateException() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setPlate("TAKEN01");

        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.existsByPlateAndActiveTrueAndIdNot("TAKEN01", VEHICLE_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> vehicleService.update(OWNER_EMAIL, VEHICLE_ID, request))
                .isInstanceOf(DuplicatePlateException.class)
                .hasMessageContaining("TAKEN01");
    }

    @Test
    void update_emptyColor_clearsColor() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setColor("");

        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(existingVehicle));

        VehicleResponse response = vehicleService.update(OWNER_EMAIL, VEHICLE_ID, request);

        assertThat(response.color()).isNull();
    }

    @Test
    void update_vehicleNotFound_throwsVehicleNotFoundException() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setBrand("Ford");

        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(99L, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.update(OWNER_EMAIL, 99L, request))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_vehicleOfAnotherUser_throwsVehicleNotFoundException() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setBrand("Ford");

        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, "other@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.update("other@example.com", VEHICLE_ID, request))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_existingActiveOwnedVehicle_setsActiveFalse() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(existingVehicle));

        vehicleService.delete(OWNER_EMAIL, VEHICLE_ID);

        assertThat(existingVehicle.isActive()).isFalse();
    }

    @Test
    void delete_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(99L, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.delete(OWNER_EMAIL, 99L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_vehicleOfAnotherUser_throwsVehicleNotFoundException() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, "other@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.delete("other@example.com", VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    @Test
    void delete_alreadyDeletedVehicle_throwsVehicleNotFoundException() {
        // findByIdAndOwnerEmailAndActiveTrue returns empty for inactive vehicles
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.delete(OWNER_EMAIL, VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_existingActiveVehicleOwnedByUser_returnsResponse() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(existingVehicle));

        VehicleResponse response = vehicleService.getById(OWNER_EMAIL, VEHICLE_ID);

        assertThat(response.id()).isEqualTo(VEHICLE_ID);
        assertThat(response.plate()).isEqualTo("AB123CD");
    }

    @Test
    void getById_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(99L, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById(OWNER_EMAIL, 99L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getById_vehicleBelongsToAnotherUser_throwsVehicleNotFoundException() {
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, "other@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById("other@example.com", VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    @Test
    void getById_deletedVehicle_throwsVehicleNotFoundException() {
        // active=false → query returns empty
        when(vehicleRepository.findByIdAndOwnerEmailAndActiveTrue(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById(OWNER_EMAIL, VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_ownerWithVehicles_returnsListOrderedByCreatedAtDesc() {
        LocalDateTime older = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime newer = LocalDateTime.of(2025, 6, 1, 0, 0);

        Vehicle v1 = Vehicle.builder().id(1L).owner(owner)
                .plate("AA111AA").brand("Toyota").model("Corolla").year(2020).currentKm(10000)
                .active(true).createdAt(older).build();
        Vehicle v2 = Vehicle.builder().id(2L).owner(owner)
                .plate("BB222BB").brand("Honda").model("Civic").year(2021).currentKm(5000)
                .active(true).createdAt(newer).build();

        when(vehicleRepository.findByOwnerEmailAndActiveTrueOrderByCreatedAtDesc(OWNER_EMAIL))
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
        when(vehicleRepository.findByOwnerEmailAndActiveTrueOrderByCreatedAtDesc(OWNER_EMAIL))
                .thenReturn(List.of());

        List<VehicleResponse> result = vehicleService.getAll(OWNER_EMAIL);

        assertThat(result).isEmpty();
    }

    @Test
    void getAll_deletedVehiclesAreExcluded() {
        // Repository only returns active vehicles — this test verifies the service relies on that
        when(vehicleRepository.findByOwnerEmailAndActiveTrueOrderByCreatedAtDesc(OWNER_EMAIL))
                .thenReturn(List.of(existingVehicle));

        List<VehicleResponse> result = vehicleService.getAll(OWNER_EMAIL);

        assertThat(result).hasSize(1);
        verify(vehicleRepository).findByOwnerEmailAndActiveTrueOrderByCreatedAtDesc(OWNER_EMAIL);
    }
}
