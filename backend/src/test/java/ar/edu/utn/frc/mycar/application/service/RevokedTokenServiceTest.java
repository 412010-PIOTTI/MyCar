package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.RevokedToken;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.RevokedTokenRepository;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokedTokenServiceTest {

    @Mock RevokedTokenRepository repository;
    @Mock JwtService jwtService;

    @InjectMocks RevokedTokenService revokedTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).name("Ana Pérez").email("ana@example.com").role(Role.USER).build();
    }

    // ── revokeToken ───────────────────────────────────────────────────────────

    @Test
    void revokeToken_newToken_persistsToBlacklist() {
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);
        when(jwtService.extractJti("raw.token")).thenReturn("jti-abc");
        when(repository.existsByTokenJti("jti-abc")).thenReturn(false);
        when(jwtService.extractExpiration("raw.token")).thenReturn(expiry);

        revokedTokenService.revokeToken("raw.token", user);

        ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
        verify(repository).save(captor.capture());
        RevokedToken saved = captor.getValue();
        assertThat(saved.getTokenJti()).isEqualTo("jti-abc");
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getExpiresAt()).isEqualTo(expiry);
        assertThat(saved.getRevokedAt()).isNotNull();
    }

    @Test
    void revokeToken_alreadyRevoked_isIdempotent() {
        when(jwtService.extractJti("raw.token")).thenReturn("jti-abc");
        when(repository.existsByTokenJti("jti-abc")).thenReturn(true);

        revokedTokenService.revokeToken("raw.token", user);

        verify(repository, never()).save(any());
    }

    // ── isRevoked ─────────────────────────────────────────────────────────────

    @Test
    void isRevoked_blacklistedJti_returnsTrue() {
        when(repository.existsByTokenJti("jti-abc")).thenReturn(true);

        assertThat(revokedTokenService.isRevoked("jti-abc")).isTrue();
    }

    @Test
    void isRevoked_unknownJti_returnsFalse() {
        when(repository.existsByTokenJti("jti-xyz")).thenReturn(false);

        assertThat(revokedTokenService.isRevoked("jti-xyz")).isFalse();
    }

    // ── cleanExpired ──────────────────────────────────────────────────────────

    @Test
    void cleanExpired_callsDeleteByExpiresAtBefore() {
        revokedTokenService.cleanExpired();

        verify(repository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }
}
