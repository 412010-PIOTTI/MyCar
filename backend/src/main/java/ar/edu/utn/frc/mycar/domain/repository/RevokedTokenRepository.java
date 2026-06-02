package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    boolean existsByTokenJti(String tokenJti);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
