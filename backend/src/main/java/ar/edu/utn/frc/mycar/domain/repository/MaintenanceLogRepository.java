package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    List<MaintenanceLog> findByVehicleIdAndVehicleOwnerEmailOrderByDateDescIdDesc(Long vehicleId, String ownerEmail);

    Optional<MaintenanceLog> findByIdAndVehicleIdAndVehicleOwnerEmail(Long id, Long vehicleId, String ownerEmail);

    /** Used by the public transfer preview, where the caller is the prospective buyer, not the current owner. */
    List<MaintenanceLog> findByVehicleIdOrderByDateDescIdDesc(Long vehicleId);
}
