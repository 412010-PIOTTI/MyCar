package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.AuthService;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.web.dto.response.AuthResponse;
import ar.edu.utn.frc.mycar.web.exception.EmailAlreadyExistsException;
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

    static final String VALID_BODY = """
            {
              "name": "Ana Pérez",
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
                        .content(VALID_BODY))
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
                        .content(VALID_BODY))
                .andExpect(status().isConflict());
    }
}
