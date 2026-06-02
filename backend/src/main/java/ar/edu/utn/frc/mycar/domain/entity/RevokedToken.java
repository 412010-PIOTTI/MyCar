package ar.edu.utn.frc.mycar.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Stores the {@code jti} of JWTs that have been explicitly revoked (logout or account deletion).
 * The filter chain checks this table on every request so revoked tokens are rejected even before
 * their natural expiry. Expired entries are cleaned up hourly by {@link ar.edu.utn.frc.mycar.application.service.RevokedTokenService#cleanExpired()}.
 */
@Entity
@Table(name = "revoked_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The JWT ID claim — unique identifier of the revoked token. */
    @Column(name = "token_jti", nullable = false, unique = true, length = 255)
    private String tokenJti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    /** When the original JWT would have expired — used for scheduled cleanup. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
