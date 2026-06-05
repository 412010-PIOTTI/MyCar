package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /** Returns {@code true} if a vehicle with the given plate already exists. */
    boolean existsByPlate(String plate);
}
