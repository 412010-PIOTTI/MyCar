package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /** Returns {@code true} if an active vehicle with the given plate already exists. */
    boolean existsByPlate(String plate);

    /** Returns all active vehicles owned by the user, ordered by creation date descending. */
    List<Vehicle> findByOwnerEmailAndActiveTrueOrderByCreatedAtDesc(String email);

    /** Returns the active vehicle with the given id only if it is owned by the user with the given email. */
    Optional<Vehicle> findByIdAndOwnerEmailAndActiveTrue(Long id, String ownerEmail);

    /**
     * Returns {@code true} if an active vehicle with the given plate exists and its id is
     * different from {@code excludedId}. Used during updates to allow a vehicle to "keep"
     * its own plate while still rejecting plates already taken by other vehicles.
     */
    boolean existsByPlateAndActiveTrueAndIdNot(String plate, Long excludedId);
}
