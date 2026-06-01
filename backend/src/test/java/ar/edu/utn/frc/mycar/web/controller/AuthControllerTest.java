package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.AuthService;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.response.AuthResponse;
import ar.edu.utn.frc.mycar.web.exception.EmailAlreadyExistsException;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.UserInactiveException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;  // required by JwtAuthFilter in WebMvcTest context

    static final String VALID_REGISTER_BODY = """
            {
              "name": "Ana Pérez",
              "email": "ana@example.com",
              "password": "secret123"
            }
            """;

    static final String VALID_LOGIN_BODY = """
            {
              "email": "ana@example.com",
              "password": "secret123"
            }
            """;

    @Test
    void register_validRequest_returns201() throws Exception {
        AuthResponse stub = new AuthResponse("jwt.t.here", 1L, "Ana Pérez",
                "ana@example.com", Role.USER);
        when(authService.register(any())).thenReturn(stub);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt.t.here"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void register_blankName_returns400() throws Exception {
        String body = """
                {"name": "", "email": "ana@example.com", "password": "secret123"}
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        String body = """
                {"name": "Ana", "email": "not-an-email", "password": "secret123"}
                """;
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        when(authService.register(any()))
                .thenThrow(new EmailAlreadyExistsException("ana@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REGISTER_BODY))
                .andExpect(status().isConflict());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200() throws Exception {
        AuthResponse stub = new AuthResponse("jwt.login.here", 1L, "Ana Pérez",
                "ana@example.com", Role.USER);
        when(authService.login(any())).thenReturn(stub);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.login.here"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_LOGIN_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_inactiveUser_returns403() throws Exception {
        when(authService.login(any())).thenThrow(new UserInactiveException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_LOGIN_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_blankEmail_returns400() throws Exception {
        String body = """
                {"email": "", "password": "secret123"}
                """;
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }
}
