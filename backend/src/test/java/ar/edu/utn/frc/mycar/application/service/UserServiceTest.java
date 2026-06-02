package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.web.dto.request.ChangePasswordRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateProfileRequest;
import ar.edu.utn.frc.mycar.web.dto.response.UserProfileResponse;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.PasswordMismatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock RevokedTokenService revokedTokenService;

    @InjectMocks UserService userService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(1L).name("Ana Pérez").email("ana@example.com")
                .passwordHash("$2a$hashed").role(Role.USER)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0)).build();
    }

    // ── getMe ─────────────────────────────────────────────────────────────────

    @Test
    void getMe_existingUser_returnsProfile() {
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));

        UserProfileResponse response = userService.getMe("ana@example.com");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Ana Pérez");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
    }

    @Test
    void getMe_unknownEmail_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMe("unknown@example.com"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    void updateProfile_validName_returnsUpdatedProfile() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Ana García");

        User updatedUser = User.builder()
                .id(1L).name("Ana García").email("ana@example.com")
                .passwordHash("$2a$hashed").role(Role.USER)
                .createdAt(activeUser.getCreatedAt()).build();

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserProfileResponse response = userService.updateProfile("ana@example.com", request);

        assertThat(response.name()).isEqualTo("Ana García");
        assertThat(response.email()).isEqualTo("ana@example.com");
        verify(userRepository).save(activeUser);
    }

    @Test
    void updateProfile_unknownEmail_throwsInvalidCredentialsException() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("Ana García");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile("unknown@example.com", request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, never()).save(any());
    }

    // ── changePassword ────────────────────────────────────────────────────────

    @Test
    void changePassword_correctCurrent_updatesHash() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("secret123");
        request.setNewPassword("newSecret456");

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret123", "$2a$hashed")).thenReturn(true);
        when(passwordEncoder.encode("newSecret456")).thenReturn("$2a$newHashed");
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        userService.changePassword("ana@example.com", request);

        assertThat(activeUser.getPasswordHash()).isEqualTo("$2a$newHashed");
        verify(userRepository).save(activeUser);
    }

    @Test
    void changePassword_wrongCurrent_throwsPasswordMismatchException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newSecret456");

        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("ana@example.com", request))
                .isInstanceOf(PasswordMismatchException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_unknownEmail_throwsInvalidCredentialsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("secret123");
        request.setNewPassword("newSecret456");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.changePassword("unknown@example.com", request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(userRepository, never()).save(any());
    }

    // ── deleteAccount ─────────────────────────────────────────────────────────

    @Test
    void deleteAccount_correctPassword_softDeletesAndRevokesToken() {
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("secret123", "$2a$hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        userService.deleteAccount("ana@example.com", "secret123", "jwt.token.here");

        assertThat(activeUser.isActive()).isFalse();
        verify(userRepository).save(activeUser);
        verify(revokedTokenService).revokeToken("jwt.token.here", activeUser);
    }

    @Test
    void deleteAccount_wrongPassword_throwsPasswordMismatchException() {
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteAccount("ana@example.com", "wrongPassword", "jwt.token.here"))
                .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, never()).save(any());
        verify(revokedTokenService, never()).revokeToken(any(), any());
    }

    @Test
    void deleteAccount_unknownEmail_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount("unknown@example.com", "secret123", "jwt.token.here"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(revokedTokenService, never()).revokeToken(any(), any());
    }
}
