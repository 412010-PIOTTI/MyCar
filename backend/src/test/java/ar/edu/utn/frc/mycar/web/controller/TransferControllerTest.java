package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.TransferService;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.enums.TransferStatus;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.response.TransferConfirmResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferGenerateResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferHistoryItemResponse;
import ar.edu.utn.frc.mycar.web.dto.response.TransferPreviewResponse;
import ar.edu.utn.frc.mycar.web.exception.CannotTransferToSelfException;
import ar.edu.utn.frc.mycar.web.exception.TransferTokenInvalidException;
import ar.edu.utn.frc.mycar.web.exception.TransferTokenNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransferControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @MockitoBean TransferService transferService;

    static final String OWNER_EMAIL = "seller@example.com";
    static final Long VEHICLE_ID = 1L;
    static final String TOKEN = "8f14e45f-ceea-4e94-b7f0-3d3a7b2f6d1a";

    static final TransferGenerateResponse GENERATE_STUB = new TransferGenerateResponse(
            TOKEN, LocalDateTime.now().plusHours(48),
            "http://localhost:4200/transfer/confirm?token=" + TOKEN,
            "data:image/png;base64,AAAA");

    String authHeader;

    @BeforeEach
    void setUp() {
        User stub = User.builder().id(1L).name("Seller").email(OWNER_EMAIL).role(Role.USER).build();
        authHeader = "Bearer " + jwtService.generateToken(stub);
    }

    // ── POST /api/vehicles/{vehicleId}/transfer/generate ─────────────────────

    @Test
    void generate_validRequest_returns201WithQr() throws Exception {
        when(transferService.generate(OWNER_EMAIL, VEHICLE_ID)).thenReturn(GENERATE_STUB);

        mockMvc.perform(post("/api/vehicles/{vehicleId}/transfer/generate", VEHICLE_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.qrCodeBase64").value("data:image/png;base64,AAAA"));
    }

    @Test
    void generate_vehicleNotFound_returns404() throws Exception {
        when(transferService.generate(OWNER_EMAIL, VEHICLE_ID)).thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        mockMvc.perform(post("/api/vehicles/{vehicleId}/transfer/generate", VEHICLE_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void generate_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/vehicles/{vehicleId}/transfer/generate", VEHICLE_ID))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/vehicles/{vehicleId}/transfer/active ────────────────────────

    @Test
    void getActiveToken_returns200WithToken() throws Exception {
        when(transferService.getActiveToken(OWNER_EMAIL, VEHICLE_ID)).thenReturn(GENERATE_STUB);

        mockMvc.perform(get("/api/vehicles/{vehicleId}/transfer/active", VEHICLE_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN));
    }

    @Test
    void getActiveToken_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/vehicles/{vehicleId}/transfer/active", VEHICLE_ID))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/vehicles/transfer/history ───────────────────────────────────

    @Test
    void getHistory_returns200WithItems() throws Exception {
        TransferHistoryItemResponse historyItem = new TransferHistoryItemResponse(
                10L, VEHICLE_ID, "AB123CD", "Toyota", "Corolla", null,
                TransferStatus.PENDING, LocalDateTime.now(), LocalDateTime.now().plusHours(48), null);
        when(transferService.getHistory(OWNER_EMAIL)).thenReturn(List.of(historyItem));

        mockMvc.perform(get("/api/vehicles/transfer/history")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getHistory_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/vehicles/transfer/history"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/vehicles/transfer/{token}/preview ───────────────────────────

    @Test
    void preview_validToken_returns200WithoutAuth() throws Exception {
        TransferPreviewResponse preview = new TransferPreviewResponse(
                "AB123CD", "Toyota", "Corolla", 2020, "Blanco", 30000,
                "Seller", LocalDateTime.now().plusHours(48), List.of());
        when(transferService.preview(TOKEN)).thenReturn(preview);

        mockMvc.perform(get("/api/vehicles/transfer/{token}/preview", TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehiclePlate").value("AB123CD"))
                .andExpect(jsonPath("$.sellerName").value("Seller"));
    }

    @Test
    void preview_tokenNotFound_returns404() throws Exception {
        when(transferService.preview(TOKEN)).thenThrow(new TransferTokenNotFoundException());

        mockMvc.perform(get("/api/vehicles/transfer/{token}/preview", TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void preview_expiredToken_returns410() throws Exception {
        when(transferService.preview(TOKEN)).thenThrow(new TransferTokenInvalidException("Este código de transferencia expiró."));

        mockMvc.perform(get("/api/vehicles/transfer/{token}/preview", TOKEN))
                .andExpect(status().isGone());
    }

    // ── POST /api/vehicles/transfer/{token}/confirm ──────────────────────────

    @Test
    void confirm_validToken_returns200() throws Exception {
        when(transferService.confirm(eq(OWNER_EMAIL), eq(TOKEN)))
                .thenReturn(new TransferConfirmResponse(VEHICLE_ID, "AB123CD", "Transferencia confirmada."));

        mockMvc.perform(post("/api/vehicles/transfer/{token}/confirm", TOKEN)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(VEHICLE_ID))
                .andExpect(jsonPath("$.vehiclePlate").value("AB123CD"));
    }

    @Test
    void confirm_selfTransfer_returns400() throws Exception {
        when(transferService.confirm(eq(OWNER_EMAIL), eq(TOKEN))).thenThrow(new CannotTransferToSelfException());

        mockMvc.perform(post("/api/vehicles/transfer/{token}/confirm", TOKEN)
                        .header("Authorization", authHeader))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirm_expiredToken_returns410() throws Exception {
        when(transferService.confirm(eq(OWNER_EMAIL), eq(TOKEN)))
                .thenThrow(new TransferTokenInvalidException("Este código de transferencia expiró."));

        mockMvc.perform(post("/api/vehicles/transfer/{token}/confirm", TOKEN)
                        .header("Authorization", authHeader))
                .andExpect(status().isGone());
    }

    @Test
    void confirm_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/vehicles/transfer/{token}/confirm", TOKEN))
                .andExpect(status().isUnauthorized());
    }
}
