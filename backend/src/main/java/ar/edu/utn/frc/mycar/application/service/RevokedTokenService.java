package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.RevokedToken;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.repository.RevokedTokenRepository;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RevokedTokenService {

    private final RevokedTokenRepository repository;
    private final JwtService jwtService;

    /**
     * Adds the token to the blacklist. Idempotent — calling it twice with the same token is safe.
     */
    @Transactional
    public void revokeToken(String token, User user) {
        String jti = jwtService.extractJti(token);
        if (repository.existsByTokenJti(jti)) {
            return;
        }
        RevokedToken revoked = RevokedToken.builder()
                .tokenJti(jti)
                .user(user)
                .revokedAt(LocalDateTime.now())
                .expiresAt(jwtService.extractExpiration(token))
                .build();
        repository.save(revoked);
    }

    /** Returns {@code true} if the given jti is in the blacklist. */
    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        return repository.existsByTokenJti(jti);
    }

    /** Removes expired entries from the blacklist — they can no longer be used anyway. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpired() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
