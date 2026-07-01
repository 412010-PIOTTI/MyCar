package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.DocumentService;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.DocumentStatus;
import ar.edu.utn.frc.mycar.domain.enums.DocumentType;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.response.CreateDocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentSummaryResponse;
import ar.edu.utn.frc.mycar.web.exception.DocumentFileNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.DocumentNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.InvalidFileException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;
    @MockitoBean DocumentService documentService;

    static final String USER_EMAIL = "ana@example.com";
    static final Long VEHICLE_ID = 1L;
    static final Long DOC_ID = 10L;

    static final DocumentResponse DOC_STUB = new DocumentResponse(
            DOC_ID, VEHICLE_ID, DocumentType.ITV, "NRO-001",
            LocalDate.of(2024, 1, 1), LocalDate.now().plusDays(60),
            null, DocumentStatus.VIGENTE, false, null,
            LocalDateTime.of(2025, 1, 1, 0, 0), null);

    String authHeader;

    @BeforeEach
    void setUp() {
        User stub = User.builder().id(1L).name("Ana Pérez").email(USER_EMAIL).role(Role.USER).build();
        authHeader = "Bearer " + jwtService.generateToken(stub);
    }

    // ── POST /api/vehicles/{vehicleId}/documents ──────────────────────────────

    @Test
    void create_validRequest_returns201WithDocument() throws Exception {
        when(documentService.create(eq(USER_EMAIL), eq(VEHICLE_ID), any()))
                .thenReturn(new CreateDocumentResponse(DOC_STUB, null));

        mockMvc.perform(post("/api/vehicles/{vehicleId}/documents", VEHICLE_ID)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ITV",
                                  "referenceNumber": "NRO-001",
                                  "issueDate": "2024-01-01",
                                  "expiryDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.document.id").value(DOC_ID))
                .andExpect(jsonPath("$.document.type").value("ITV"))
                .andExpect(jsonPath("$.warning").doesNotExist());
    }

    @Test
    void create_replacesExisting_returns201WithWarning() throws Exception {
        when(documentService.create(eq(USER_EMAIL), eq(VEHICLE_ID), any()))
                .thenReturn(new CreateDocumentResponse(DOC_STUB, "Se reemplazó el documento anterior de tipo ITV."));

        mockMvc.perform(post("/api/vehicles/{vehicleId}/documents", VEHICLE_ID)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ITV",
                                  "referenceNumber": "NRO-002"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.warning").value("Se reemplazó el documento anterior de tipo ITV."));
    }

    @Test
    void create_missingType_returns400() throws Exception {
        mockMvc.perform(post("/api/vehicles/{vehicleId}/documents", VEHICLE_ID)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.type").exists());
    }

    @Test
    void create_vehicleNotFound_returns404() throws Exception {
        when(documentService.create(eq(USER_EMAIL), eq(VEHICLE_ID), any()))
                .thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        mockMvc.perform(post("/api/vehicles/{vehicleId}/documents", VEHICLE_ID)
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "ITV"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/vehicles/{vehicleId}/documents", VEHICLE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "ITV"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/vehicles/{vehicleId}/documents ───────────────────────────────

    @Test
    void getAll_returnsListOf200() throws Exception {
        when(documentService.getAll(USER_EMAIL, VEHICLE_ID)).thenReturn(List.of(DOC_STUB));

        mockMvc.perform(get("/api/vehicles/{vehicleId}/documents", VEHICLE_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(DOC_ID))
                .andExpect(jsonPath("$[0].type").value("ITV"));
    }

    @Test
    void getAll_vehicleNotFound_returns404() throws Exception {
        when(documentService.getAll(USER_EMAIL, VEHICLE_ID))
                .thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        mockMvc.perform(get("/api/vehicles/{vehicleId}/documents", VEHICLE_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/vehicles/{vehicleId}/documents/{documentId} ─────────────────

    @Test
    void getById_existingDoc_returns200() throws Exception {
        when(documentService.getById(USER_EMAIL, VEHICLE_ID, DOC_ID)).thenReturn(DOC_STUB);

        mockMvc.perform(get("/api/vehicles/{vehicleId}/documents/{documentId}", VEHICLE_ID, DOC_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DOC_ID))
                .andExpect(jsonPath("$.status").value("VIGENTE"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(documentService.getById(USER_EMAIL, VEHICLE_ID, DOC_ID))
                .thenThrow(new DocumentNotFoundException(DOC_ID));

        mockMvc.perform(get("/api/vehicles/{vehicleId}/documents/{documentId}", VEHICLE_ID, DOC_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/vehicles/{vehicleId}/documents/summary ──────────────────────

    @Test
    void getSummary_returns200WithSummary() throws Exception {
        DocumentSummaryResponse summary = new DocumentSummaryResponse(4, 3, 1, 0, 0, 75,
                new DocumentSummaryResponse.NextExpiring(DocumentType.ITV, "ITV", LocalDate.now().plusDays(14), 14));
        when(documentService.getSummary(USER_EMAIL, VEHICLE_ID)).thenReturn(summary);

        mockMvc.perform(get("/api/vehicles/{vehicleId}/documents/summary", VEHICLE_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.compliancePercentage").value(75))
                .andExpect(jsonPath("$.nextExpiring.type").value("ITV"))
                .andExpect(jsonPath("$.nextExpiring.daysLeft").value(14));
    }

    // ── DELETE /api/vehicles/{vehicleId}/documents/{documentId} ──────────────

    @Test
    void delete_existingDoc_returns204() throws Exception {
        doNothing().when(documentService).delete(USER_EMAIL, VEHICLE_ID, DOC_ID);

        mockMvc.perform(delete("/api/vehicles/{vehicleId}/documents/{documentId}", VEHICLE_ID, DOC_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new DocumentNotFoundException(DOC_ID))
                .when(documentService).delete(USER_EMAIL, VEHICLE_ID, DOC_ID);

        mockMvc.perform(delete("/api/vehicles/{vehicleId}/documents/{documentId}", VEHICLE_ID, DOC_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/vehicles/{vehicleId}/documents/{documentId}/file ──────────

    @Test
    void uploadFile_validPdf_returns200WithDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "poliza.pdf", "application/pdf", "content".getBytes());
        DocumentResponse withFile = new DocumentResponse(
                DOC_ID, VEHICLE_ID, DocumentType.ITV, "NRO-001",
                LocalDate.of(2024, 1, 1), LocalDate.now().plusDays(60),
                null, DocumentStatus.VIGENTE, true, "poliza.pdf",
                LocalDateTime.of(2025, 1, 1, 0, 0), null);
        when(documentService.uploadFile(eq(USER_EMAIL), eq(VEHICLE_ID), eq(DOC_ID), any())).thenReturn(withFile);

        mockMvc.perform(multipart("/api/vehicles/{vehicleId}/documents/{documentId}/file", VEHICLE_ID, DOC_ID)
                        .file(file)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasFile").value(true))
                .andExpect(jsonPath("$.originalFileName").value("poliza.pdf"));
    }

    @Test
    void uploadFile_invalidType_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "content".getBytes());
        when(documentService.uploadFile(eq(USER_EMAIL), eq(VEHICLE_ID), eq(DOC_ID), any()))
                .thenThrow(new InvalidFileException("Solo se permiten archivos PDF."));

        mockMvc.perform(multipart("/api/vehicles/{vehicleId}/documents/{documentId}/file", VEHICLE_ID, DOC_ID)
                        .file(file)
                        .header("Authorization", authHeader))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFile_documentNotFound_returns404() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "poliza.pdf", "application/pdf", "content".getBytes());
        when(documentService.uploadFile(eq(USER_EMAIL), eq(VEHICLE_ID), eq(DOC_ID), any()))
                .thenThrow(new DocumentNotFoundException(DOC_ID));

        mockMvc.perform(multipart("/api/vehicles/{vehicleId}/documents/{documentId}/file", VEHICLE_ID, DOC_ID)
                        .file(file)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadFile_noAuth_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "poliza.pdf", "application/pdf", "content".getBytes());

        mockMvc.perform(multipart("/api/vehicles/{vehicleId}/documents/{documentId}/file", VEHICLE_ID, DOC_ID)
                        .file(file))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/vehicles/{vehicleId}/documents/{documentId}/file ────────────

    @Test
    void getFile_existingFile_returns200WithPdf() throws Exception {
        DocumentService.StoredFile stored = new DocumentService.StoredFile(
                new ByteArrayResource("content".getBytes()), "poliza.pdf");
        when(documentService.getFile(USER_EMAIL, VEHICLE_ID, DOC_ID)).thenReturn(stored);

        mockMvc.perform(get("/api/vehicles/{vehicleId}/documents/{documentId}/file", VEHICLE_ID, DOC_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"poliza.pdf\""));
    }

    @Test
    void getFile_noFile_returns404() throws Exception {
        when(documentService.getFile(USER_EMAIL, VEHICLE_ID, DOC_ID))
                .thenThrow(new DocumentFileNotFoundException(DOC_ID));

        mockMvc.perform(get("/api/vehicles/{vehicleId}/documents/{documentId}/file", VEHICLE_ID, DOC_ID)
                        .header("Authorization", authHeader))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFile_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/vehicles/{vehicleId}/documents/{documentId}/file", VEHICLE_ID, DOC_ID))
                .andExpect(status().isUnauthorized());
    }
}
