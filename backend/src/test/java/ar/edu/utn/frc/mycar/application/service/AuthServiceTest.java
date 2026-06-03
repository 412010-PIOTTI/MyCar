package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.request.LoginRequest;
import ar.edu.utn.frc.mycar.web.dto.request.RegisterRequest;
import ar.edu.utn.frc.mycar.web.dto.response.AuthResponse;
import ar.edu.utn.frc.mycar.web.dto.response.LoginResponse;
import ar.edu.utn.frc.mycar.web.exception.EmailAlreadyExistsException;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.UserInactiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock RevokedTokenService revokedTokenService;
    @Mock TwoFactorService twoFactorService;

    @InjectMocks AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User activeUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Ana Pérez");
        registerRequest.setEmail("ana@example.com");
        registerRequest.setPassword("secret123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("ana@example.com");
        loginRequest.setPassword("secret123");

        activeUser = User.builder()
                .id(1L).name("Ana Pérez").email("ana@example.com")
                .passwordHash("$2a$hashed").role(Role.USER).build();
    }

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    void register_success_returnsAuthResponse() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);
        when(jwtService.generateToken(activeUser)).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("ana@example.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(activeUser);
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyExistsException() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("ana@example.com");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_success_returnsLoginResponse() {
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret123", "$2a$hashed")).thenReturn(true);
        when(jwtService.generateToken(activeUser)).thenReturn("jwt.login.token");

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt.login.token");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("ana@example.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        assertThat(response.getRequires2FA()).isNull();
    }

    @Test
    void login_emailNotFound_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret123", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_inactiveUser_throwsUserInactiveException() {
        User inactiveUser = User.builder()
                .id(2L).name("Ana Pérez").email("ana@example.com")
                .passwordHash("$2a$hashed").role(Role.USER).active(false).build();
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(inactiveUser));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserInactiveException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    void logout_validToken_revokesToken() {
        when(jwtService.extractEmail("valid.token.here")).thenReturn("ana@example.com");
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));

        authService.logout("valid.token.here");

        verify(revokedTokenService).revokeToken("valid.token.here", activeUser);
    }

    @Test
    void logout_unknownEmail_throwsInvalidCredentialsException() {
        when(jwtService.extractEmail("valid.token.here")).thenReturn("ghost@example.com");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout("valid.token.here"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(revokedTokenService, never()).revokeToken(any(), any());
    }
}
