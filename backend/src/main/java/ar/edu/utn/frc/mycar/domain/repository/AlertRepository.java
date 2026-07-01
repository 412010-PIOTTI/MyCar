package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByCreatedAtDesc(
            Long vehicleId, String ownerEmail);

    Optional<Alert> findByIdAndVehicleIdAndVehicleOwnerEmail(Long id, Long vehicleId, String ownerEmail);

    @Query("SELECT a FROM Alert a JOIN FETCH a.vehicle JOIN FETCH a.user WHERE a.active = true")
    List<Alert> findAllActiveWithDetails();
}
