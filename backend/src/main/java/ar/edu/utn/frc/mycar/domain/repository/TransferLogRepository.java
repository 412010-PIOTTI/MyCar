package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.TransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferLogRepository extends JpaRepository<TransferLog, Long> {

    Optional<TransferLog> findByToken(String token);

    /**
     * Whether this vehicle appears in the permanent transfer audit trail. If so, the vehicle
     * row itself cannot be hard-deleted (transfer_logs.vehicle_id has no cascade and the
     * history is intentionally immutable) — only its personal-data children can be purged.
     */
    boolean existsByVehicleId(Long vehicleId);
}
