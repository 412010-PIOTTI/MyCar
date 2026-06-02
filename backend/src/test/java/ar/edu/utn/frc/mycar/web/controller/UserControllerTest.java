package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.UserService;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.response.UserProfileResponse;
import ar.edu.utn.frc.mycar.web.exception.PasswordMismatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for UserController. Uses @SpringBootTest (full context + H2) so the real
 * Spring Security filter chain runs, including SecurityContextHolderAwareRequestFilter, which
 * is required for the Authentication parameter resolver to work in stateless JWT apps.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @MockitoBean UserService userService;

    static final String USER_EMAIL = "ana@example.com";
    static final UserProfileResponse PROFILE_STUB = new UserProfileResponse(
            1L, "Ana Pérez", USER_EMAIL, Role.USER,
            LocalDateTime.of(2025, 1, 1, 0, 0));

    String authHeader;

    @BeforeEach
    void setUp() {
        User stub = User.builder().id(1L).name("Ana Pérez").email(USER_EMAIL).role(Role.USER).build();
        authHeader = "Bearer " + jwtService.generateToken(stub);
    }

    // ── GET /api/users/me ─────────────────────────────────────────────────────

    @Test
    void getMe_authenticated_returnsProfile() throws Exception {
        when(userService.getMe(USER_EMAIL)).thenReturn(PROFILE_STUB);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ana Pérez"))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void getMe_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/users/me ─────────────────────────────────────────────────────

    @Test
    void updateMe_validName_returnsUpdatedProfile() throws Exception {
        UserProfileResponse updated = new UserProfileResponse(
                1L, "Ana García", USER_EMAIL, Role.USER,
                LocalDateTime.of(2025, 1, 1, 0, 0));
        when(userService.updateProfile(eq(USER_EMAIL), any())).thenReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ana García"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana García"));
    }

    @Test
    void updateMe_blankName_returns400() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void updateMe_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ana García"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ── PUT /api/users/me/password ────────────────────────────────────────────

    @Test
    void changePassword_validRequest_returns204() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "secret123", "newPassword": "newSecret456"}
                                """))
                .andExpect(status().isNoContent());

        verify(userService).changePassword(eq(USER_EMAIL), any());
    }

    @Test
    void changePassword_wrongCurrent_returns400() throws Exception {
        doThrow(new PasswordMismatchException())
                .when(userService).changePassword(eq(USER_EMAIL), any());

        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "wrong", "newPassword": "newSecret456"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_shortNewPassword_returns400() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "secret123", "newPassword": "short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    @Test
    void changePassword_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword": "secret123", "newPassword": "newSecret456"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
