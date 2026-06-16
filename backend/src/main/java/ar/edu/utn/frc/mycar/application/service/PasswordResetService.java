package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.PasswordResetToken;
import ar.edu.utn.frc.mycar.domain.repository.PasswordResetTokenRepository;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.infrastructure.email.EmailService;
import ar.edu.utn.frc.mycar.web.exception.InvalidResetTokenException;
import ar.edu.utn.frc.mycar.web.exception.ResetTokenExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final int TOKEN_EXPIRY_HOURS = 1;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Initiates the password reset flow for the given email.
     * Always responds successfully — does not reveal whether the email exists.
     */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            tokenRepository.invalidatePendingByUser(user);

            String rawToken = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(rawToken)
                    .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                    .build();
            tokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/auth/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(email, resetLink);
        });
    }

    /**
     * Resets the user's password using a valid reset token.
     * Throws {@link InvalidResetTokenException} if the token does not exist or is already used.
     * Throws {@link ResetTokenExpiredException} if the token has expired.
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(rawToken)
                .orElseThrow(InvalidResetTokenException::new);

        if (token.isUsed()) {
            throw new InvalidResetTokenException();
        }
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResetTokenExpiredException();
        }

        token.getUser().setPasswordHash(passwordEncoder.encode(newPassword));
        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Password reset completed for user id={}", token.getUser().getId());
    }

    /** Removes expired or already-used tokens every hour alongside other token cleanup jobs. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpiredTokens() {
        tokenRepository.deleteExpiredOrUsed(LocalDateTime.now());
    }
}
