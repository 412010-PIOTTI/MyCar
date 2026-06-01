package ar.edu.utn.frc.mycar.infrastructure.security;

import ar.edu.utn.frc.mycar.domain.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

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

    /** Generates a signed JWT for the given user. */
    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
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
}
