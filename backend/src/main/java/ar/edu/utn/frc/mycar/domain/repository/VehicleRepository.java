package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /** Returns {@code true} if a vehicle with the given plate already exists. */
    boolean existsByPlate(String plate);

    /** Returns all vehicles owned by the user with the given email, ordered by creation date descending. */
    List<Vehicle> findByOwnerEmailOrderByCreatedAtDesc(String email);

    /** Returns the vehicle with the given id only if it is owned by the user with the given email. */
    Optional<Vehicle> findByIdAndOwnerEmail(Long id, String ownerEmail);
}
