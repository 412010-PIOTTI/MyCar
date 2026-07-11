package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.TransferToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransferTokenRepository extends JpaRepository<TransferToken, Long> {

    Optional<TransferToken> findByToken(String token);

    /** Tokens aún no usados para un vehículo — usado para invalidarlos al regenerar el QR. */
    List<TransferToken> findByVehicleIdAndUsedFalse(Long vehicleId);

    /** Historial completo de códigos generados por el usuario, como vendedor, en cualquier vehículo. */
    List<TransferToken> findByGeneratedByEmailOrderByCreatedAtDesc(String generatedByEmail);
}
