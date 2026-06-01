package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.request.RegisterRequest;
import ar.edu.utn.frc.mycar.web.dto.response.AuthResponse;
import ar.edu.utn.frc.mycar.web.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @InjectMocks AuthService authService;

    private RegisterRequest request;

    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setName("Ana Pérez");
        request.setEmail("ana@example.com");
        request.setPassword("secret123");
    }

    @Test
    void register_success_returnsAuthResponse() {
        User saved = User.builder()
                .id(1L).name("Ana Pérez").email("ana@example.com")
                .passwordHash("$2a$hashed").role(Role.USER).build();

        when(userRepository.existsByEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(saved)).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("ana@example.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(saved);
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyExistsException() {
        when(userRepository.existsByEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("ana@example.com");

        verify(userRepository, never()).save(any());
        verify(jwtService, never()).generateToken(any());
    }
}
