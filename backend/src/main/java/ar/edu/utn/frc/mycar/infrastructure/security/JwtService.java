package ar.edu.utn.frc.mycar.infrastructure.security;

import ar.edu.utn.frc.mycar.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/** Handles JWT creation. Token claims: {@code sub} = email, {@code id}, {@code role}. */
@Service
public class JwtService {

    /** HMAC-SHA key derived from {@code app.jwt.secret}. */
    private final SecretKey secretKey;

    /** Token validity in milliseconds, from {@code app.jwt.expiration-ms}. */
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /** Generates a signed JWT for the given user. Includes a unique {@code jti} claim for revocation. */
    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .claims(Map.of(
                        "id",   user.getId(),
                        "role", user.getRole().name()
                ))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(secretKey)
                .compact();
    }

    /** Extracts the email (subject) from a signed JWT. Throws if the token is invalid or expired. */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /** Extracts the {@code jti} (JWT ID) claim — used to look up the token in the blacklist. */
    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    /** Extracts the expiration date as {@link LocalDateTime} — stored in the blacklist for cleanup. */
    public LocalDateTime extractExpiration(String token) {
        Date exp = parseClaims(token).getExpiration();
        return exp.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /** Returns {@code true} if the token signature is valid and the token has not expired. */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
