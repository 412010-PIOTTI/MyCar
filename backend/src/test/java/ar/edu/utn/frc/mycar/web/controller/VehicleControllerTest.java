package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.VehicleService;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.response.VehicleResponse;
import ar.edu.utn.frc.mycar.web.exception.DuplicatePlateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import java.time.Year;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for VehicleController. Uses @SpringBootTest so the full Spring Security
 * filter chain runs with the real JWT filter, matching production behaviour.
 */
@SpringBootTest
@AutoConfigureMockMvc
class VehicleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @MockitoBean VehicleService vehicleService;

    static final String USER_EMAIL = "ana@example.com";
    static final VehicleResponse VEHICLE_STUB = new VehicleResponse(
            1L, "AB123CD", "Toyota", "Corolla", 2020, "Blanco",
            35000, LocalDateTime.of(2025, 1, 1, 0, 0));

    String authHeader;

    @BeforeEach
    void setUp() {
        User stub = User.builder().id(1L).name("Ana Pérez").email(USER_EMAIL).role(Role.USER).build();
        authHeader = "Bearer " + jwtService.generateToken(stub);
    }

    // ── POST /api/vehicles ────────────────────────────────────────────────────

    @Test
    void register_validRequest_returns201WithVehicleResponse() throws Exception {
        when(vehicleService.register(eq(USER_EMAIL), any())).thenReturn(VEHICLE_STUB);

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020,
                                  "color": "Blanco",
                                  "initialKm": 35000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("AB123CD"))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"))
                .andExpect(jsonPath("$.year").value(2020))
                .andExpect(jsonPath("$.currentKm").value(35000));
    }

    @Test
    void register_colorOmitted_returns201() throws Exception {
        VehicleResponse noColor = new VehicleResponse(
                2L, "XY999ZZ", "Honda", "Civic", 2019, null, 0,
                LocalDateTime.of(2025, 1, 1, 0, 0));
        when(vehicleService.register(eq(USER_EMAIL), any())).thenReturn(noColor);

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "XY999ZZ",
                                  "brand": "Honda",
                                  "model": "Civic",
                                  "year": 2019,
                                  "initialKm": 0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void register_duplicatePlate_returns409() throws Exception {
        when(vehicleService.register(eq(USER_EMAIL), any()))
                .thenThrow(new DuplicatePlateException("AB123CD"));

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020,
                                  "initialKm": 35000
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("La patente 'AB123CD' ya está registrada en el sistema."));
    }

    @Test
    void register_blankPlate_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020,
                                  "initialKm": 35000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.plate").exists());
    }

    @Test
    void register_blankBrand_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "",
                                  "model": "Corolla",
                                  "year": 2020,
                                  "initialKm": 35000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.brand").exists());
    }

    @Test
    void register_blankModel_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "",
                                  "year": 2020,
                                  "initialKm": 35000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.model").exists());
    }

    @Test
    void register_nullYear_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "initialKm": 35000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.year").exists());
    }

    @Test
    void register_yearTooLow_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": 1800,
                                  "initialKm": 35000
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.year").exists());
    }

    @Test
    void register_yearIsCurrentYear_returns201() throws Exception {
        int currentYear = Year.now().getValue();
        when(vehicleService.register(eq(USER_EMAIL), any())).thenReturn(VEHICLE_STUB);

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": %d,
                                  "initialKm": 35000
                                }
                                """.formatted(currentYear)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_yearInFuture_returns400WithFieldError() throws Exception {
        int nextYear = Year.now().getValue() + 1;

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": %d,
                                  "initialKm": 35000
                                }
                                """.formatted(nextYear)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.year").value("El año no puede ser posterior al año actual"));
    }

    @Test
    void register_negativeKm_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020,
                                  "initialKm": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.initialKm").exists());
    }

    @Test
    void register_nullInitialKm_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.initialKm").exists());
    }

    @Test
    void register_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "plate": "AB123CD",
                                  "brand": "Toyota",
                                  "model": "Corolla",
                                  "year": 2020,
                                  "initialKm": 35000
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/vehicles ─────────────────────────────────────────────────────

    @Test
    void getAll_authenticated_returnsVehicleList() throws Exception {
        VehicleResponse second = new VehicleResponse(
                2L, "BB222BB", "Honda", "Civic", 2021, null,
                5000, LocalDateTime.of(2025, 6, 1, 0, 0));

        when(vehicleService.getAll(USER_EMAIL)).thenReturn(List.of(second, VEHICLE_STUB));

        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].plate").value("BB222BB"))
                .andExpect(jsonPath("$[1].id").value(1))
                .andExpect(jsonPath("$[1].plate").value("AB123CD"));
    }

    @Test
    void getAll_noVehicles_returns200WithEmptyArray() throws Exception {
        when(vehicleService.getAll(USER_EMAIL)).thenReturn(List.of());

        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());
    }
}
