package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.TwoFactorToken;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.repository.TwoFactorTokenRepository;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.infrastructure.email.EmailService;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.response.AuthResponse;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.TwoFactorVerificationException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int CODE_EXPIRY_MINUTES = 10;

    private final TwoFactorTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a 6-digit code, invalidates any previous pending token for the user,
     * persists the new token, and sends it by email.
     */
    @Transactional
    public void generateAndSend(User user) {
        tokenRepository.invalidatePendingByUser(user);

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        TwoFactorToken token = TwoFactorToken.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .build();
        tokenRepository.save(token);

        emailService.send2FACode(user.getEmail(), code);
    }

    /**
     * Validates the code for the given email.
     * Returns a full {@link AuthResponse} with JWT on success.
     * Throws {@link TwoFactorVerificationException} for any failure (wrong code, expired, max attempts).
     */
    @Transactional
    public AuthResponse verify(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        TwoFactorToken token = tokenRepository.findActiveToken(email, LocalDateTime.now())
                .orElseThrow(() -> new TwoFactorVerificationException(
                        "El código expiró o no existe. Solicitá uno nuevo."));

        if (!token.getCode().equals(code)) {
            token.setAttempts(token.getAttempts() + 1);
            if (token.getAttempts() >= MAX_ATTEMPTS) {
                token.setUsed(true);
                tokenRepository.save(token);
                throw new TwoFactorVerificationException(
                        "Número máximo de intentos alcanzado. Solicitá un nuevo código.");
            }
            int remaining = MAX_ATTEMPTS - token.getAttempts();
            tokenRepository.save(token);
            throw new TwoFactorVerificationException(
                    "Código incorrecto. Intentos restantes: " + remaining);
        }

        token.setUsed(true);
        tokenRepository.save(token);

        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    /**
     * Regenerates and resends the code. Safe to call multiple times — each call invalidates the previous token.
     */
    @Transactional
    public void resend(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.isTwoFactorEnabled() || !user.isActive()) {
            throw new InvalidCredentialsException();
        }
        generateAndSend(user);
    }

    /** Invalidates any pending 2FA token for the given email (e.g., user cancels the flow). */
    @Transactional
    public void cancel(String email) {
        userRepository.findByEmail(email)
                .ifPresent(tokenRepository::invalidatePendingByUser);
    }

    /** Removes expired and used tokens every hour alongside the JWT blacklist cleanup. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpiredTokens() {
        tokenRepository.deleteExpiredOrUsed(LocalDateTime.now());
    }

    /** Returns the masked form of an email: first char + *** + @domain. */
    public static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        return email.charAt(0) + "***" + email.substring(at);
    }
}
