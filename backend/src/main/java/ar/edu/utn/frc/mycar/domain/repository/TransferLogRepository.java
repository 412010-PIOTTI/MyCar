package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.TransferLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferLogRepository extends JpaRepository<TransferLog, Long> {

    Optional<TransferLog> findByToken(String token);
}
