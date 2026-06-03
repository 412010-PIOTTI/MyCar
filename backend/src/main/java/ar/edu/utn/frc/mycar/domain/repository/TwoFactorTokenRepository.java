package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.TwoFactorToken;
import ar.edu.utn.frc.mycar.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TwoFactorTokenRepository extends JpaRepository<TwoFactorToken, Long> {

    /** Returns the most recent active (not used, not expired, under attempt limit) token for the email. */
    @Query("""
            SELECT t FROM TwoFactorToken t
            WHERE t.user.email = :email
              AND t.used = false
              AND t.expiresAt > :now
              AND t.attempts < 5
            ORDER BY t.createdAt DESC
            """)
    Optional<TwoFactorToken> findActiveToken(@Param("email") String email, @Param("now") LocalDateTime now);

    /** Marks all pending tokens for a user as used (called on resend or cancel). */
    @Modifying
    @Query("UPDATE TwoFactorToken t SET t.used = true WHERE t.user = :user AND t.used = false")
    void invalidatePendingByUser(@Param("user") User user);

    /** Removes expired or already-used tokens — called by the scheduled cleanup. */
    @Modifying
    @Query("DELETE FROM TwoFactorToken t WHERE t.expiresAt < :now OR t.used = true")
    void deleteExpiredOrUsed(@Param("now") LocalDateTime now);
}
