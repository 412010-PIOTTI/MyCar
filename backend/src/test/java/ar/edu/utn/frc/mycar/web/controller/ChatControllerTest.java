package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.ChatService;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.response.ChatResponse;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for ChatController. Uses @SpringBootTest so the full Spring Security filter
 * chain runs with the real JWT filter, matching production behaviour.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @MockitoBean ChatService chatService;

    static final String USER_EMAIL = "ana@example.com";

    String authHeader;

    @BeforeEach
    void setUp() {
        User stub = User.builder().id(1L).name("Ana Pérez").email(USER_EMAIL).role(Role.USER).build();
        authHeader = "Bearer " + jwtService.generateToken(stub);
    }

    @Test
    void chat_validRequest_returns200WithReply() throws Exception {
        when(chatService.ask(eq(USER_EMAIL), any()))
                .thenReturn(new ChatResponse("Cada 10.000 km.", true, 1L));

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 1,
                                  "message": "¿Cuándo toca el cambio de aceite?"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").value("Cada 10.000 km."))
                .andExpect(jsonPath("$.manualGrounded").value(true))
                .andExpect(jsonPath("$.vehicleId").value(1));
    }

    @Test
    void chat_blankMessage_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 1,
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void chat_missingVehicleId_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "hola"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.vehicleId").exists());
    }

    @Test
    void chat_vehicleNotFoundOrNotOwned_returns404() throws Exception {
        when(chatService.ask(eq(USER_EMAIL), any())).thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 99,
                                  "message": "hola"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(
                        "No se encontró un vehículo con id 99 asociado a tu cuenta."));
    }

    @Test
    void chat_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 1,
                                  "message": "hola"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }
}
