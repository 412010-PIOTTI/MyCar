package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Document;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.DocumentStatus;
import ar.edu.utn.frc.mycar.domain.enums.DocumentType;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.DocumentRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateDocumentRequest;
import ar.edu.utn.frc.mycar.web.dto.response.CreateDocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentSummaryResponse;
import ar.edu.utn.frc.mycar.web.exception.DocumentFileNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.DocumentNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.VehicleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock DocumentRepository documentRepository;
    @Mock VehicleService vehicleService;
    @Mock UserService userService;
    @Mock AlertService alertService;
    @Mock FileStorageService fileStorageService;

    @InjectMocks DocumentService documentService;

    static final String OWNER_EMAIL = "ana@example.com";
    static final Long VEHICLE_ID = 1L;
    static final Long DOC_ID = 10L;

    User owner;
    Vehicle vehicle;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Ana").email(OWNER_EMAIL).role(Role.USER).build();
        vehicle = Vehicle.builder()
                .id(VEHICLE_ID).owner(owner)
                .plate("AB123CD").brand("Toyota").model("Corolla")
                .year(2020).currentKm(30000).active(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();
    }

    private CreateDocumentRequest buildRequest(DocumentType type) {
        CreateDocumentRequest req = new CreateDocumentRequest();
        req.setType(type);
        req.setReferenceNumber("NRO-001");
        req.setIssueDate(LocalDate.of(2024, 1, 1));
        req.setExpiryDate(LocalDate.now().plusDays(60));
        return req;
    }

    private Document buildDocument(Long id, DocumentType type, LocalDate expiryDate) {
        return Document.builder()
                .id(id).vehicle(vehicle).type(type)
                .referenceNumber("NRO-001")
                .issueDate(LocalDate.of(2024, 1, 1))
                .expiryDate(expiryDate)
                .active(true)
                .createdAt(LocalDateTime.of(2025, 1, 1, 0, 0))
                .build();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_noExisting_createsDocumentWithoutWarning() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndTypeAndActiveTrue(VEHICLE_ID, DocumentType.ITV))
                .thenReturn(Optional.empty());
        Document saved = buildDocument(DOC_ID, DocumentType.ITV, LocalDate.now().plusDays(60));
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        CreateDocumentResponse result = documentService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(DocumentType.ITV));

        assertThat(result.warning()).isNull();
        assertThat(result.document().type()).isEqualTo(DocumentType.ITV);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void create_existingActiveDocument_replacesAndSetsWarning() {
        Document existing = buildDocument(5L, DocumentType.ITV, LocalDate.now().plusDays(10));
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndTypeAndActiveTrue(VEHICLE_ID, DocumentType.ITV))
                .thenReturn(Optional.of(existing));
        Document saved = buildDocument(DOC_ID, DocumentType.ITV, LocalDate.now().plusDays(60));
        when(documentRepository.save(any(Document.class))).thenReturn(existing).thenReturn(saved);

        CreateDocumentResponse result = documentService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(DocumentType.ITV));

        assertThat(existing.isActive()).isFalse();
        assertThat(result.warning()).isNotNull().contains("ITV");
        verify(documentRepository, times(2)).save(any(Document.class));
    }

    @Test
    void create_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL))
                .thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        assertThatThrownBy(() -> documentService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(DocumentType.ITV)))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── auto-alert ────────────────────────────────────────────────────────────

    @Test
    void create_withExpiryDate_createsAutoAlert() {
        LocalDate expiry = LocalDate.now().plusDays(60);
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndTypeAndActiveTrue(VEHICLE_ID, DocumentType.ITV))
                .thenReturn(Optional.empty());
        when(userService.getEntity(OWNER_EMAIL)).thenReturn(owner);
        Document saved = buildDocument(DOC_ID, DocumentType.ITV, expiry);
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        documentService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(DocumentType.ITV));

        verify(alertService).createAutoAlert(
                eq(vehicle), eq(owner),
                eq("Vencimiento: ITV"),
                eq(AlertType.DATE), eq(expiry), isNull());
    }

    @Test
    void create_withoutExpiryDate_doesNotCreateAutoAlert() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndTypeAndActiveTrue(VEHICLE_ID, DocumentType.CEDULA_VERDE))
                .thenReturn(Optional.empty());
        Document saved = buildDocument(DOC_ID, DocumentType.CEDULA_VERDE, null);
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        CreateDocumentRequest req = buildRequest(DocumentType.CEDULA_VERDE);
        req.setExpiryDate(null);
        documentService.create(OWNER_EMAIL, VEHICLE_ID, req);

        verifyNoInteractions(alertService);
        verifyNoInteractions(userService);
    }

    // ── status calculation ────────────────────────────────────────────────────

    @Test
    void create_expiryFarAway_returnsVigente() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndTypeAndActiveTrue(VEHICLE_ID, DocumentType.SEGURO))
                .thenReturn(Optional.empty());
        Document saved = buildDocument(DOC_ID, DocumentType.SEGURO, LocalDate.now().plusDays(60));
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        CreateDocumentRequest req = buildRequest(DocumentType.SEGURO);
        CreateDocumentResponse result = documentService.create(OWNER_EMAIL, VEHICLE_ID, req);

        assertThat(result.document().status()).isEqualTo(DocumentStatus.VIGENTE);
    }

    @Test
    void create_expiryWithin30Days_returnsPorVencer() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndTypeAndActiveTrue(VEHICLE_ID, DocumentType.SEGURO))
                .thenReturn(Optional.empty());
        Document saved = buildDocument(DOC_ID, DocumentType.SEGURO, LocalDate.now().plusDays(15));
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        CreateDocumentRequest req = buildRequest(DocumentType.SEGURO);
        req.setExpiryDate(LocalDate.now().plusDays(15));
        CreateDocumentResponse result = documentService.create(OWNER_EMAIL, VEHICLE_ID, req);

        assertThat(result.document().status()).isEqualTo(DocumentStatus.POR_VENCER);
    }

    @Test
    void create_expiryInPast_returnsVencido() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndTypeAndActiveTrue(VEHICLE_ID, DocumentType.SEGURO))
                .thenReturn(Optional.empty());
        Document saved = buildDocument(DOC_ID, DocumentType.SEGURO, LocalDate.now().minusDays(1));
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        CreateDocumentResponse result = documentService.create(OWNER_EMAIL, VEHICLE_ID, buildRequest(DocumentType.SEGURO));

        assertThat(result.document().status()).isEqualTo(DocumentStatus.VENCIDO);
    }

    @Test
    void create_noExpiryDate_returnsSinFecha() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndTypeAndActiveTrue(VEHICLE_ID, DocumentType.CEDULA_VERDE))
                .thenReturn(Optional.empty());
        Document saved = buildDocument(DOC_ID, DocumentType.CEDULA_VERDE, null);
        when(documentRepository.save(any(Document.class))).thenReturn(saved);

        CreateDocumentRequest req = buildRequest(DocumentType.CEDULA_VERDE);
        req.setExpiryDate(null);
        CreateDocumentResponse result = documentService.create(OWNER_EMAIL, VEHICLE_ID, req);

        assertThat(result.document().status()).isEqualTo(DocumentStatus.SIN_FECHA);
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsActiveDocumentsForVehicle() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByTypeAsc(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(List.of(
                        buildDocument(1L, DocumentType.ITV, LocalDate.now().plusDays(60)),
                        buildDocument(2L, DocumentType.SEGURO, LocalDate.now().plusDays(90))
                ));

        List<DocumentResponse> result = documentService.getAll(OWNER_EMAIL, VEHICLE_ID);

        assertThat(result).hasSize(2);
    }

    @Test
    void getAll_vehicleNotFound_throwsVehicleNotFoundException() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL))
                .thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        assertThatThrownBy(() -> documentService.getAll(OWNER_EMAIL, VEHICLE_ID))
                .isInstanceOf(VehicleNotFoundException.class);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_existingDocument_returnsResponse() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(buildDocument(DOC_ID, DocumentType.ITV, LocalDate.now().plusDays(60))));

        DocumentResponse result = documentService.getById(OWNER_EMAIL, VEHICLE_ID, DOC_ID);

        assertThat(result.id()).isEqualTo(DOC_ID);
    }

    @Test
    void getById_notFound_throwsDocumentNotFoundException() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getById(OWNER_EMAIL, VEHICLE_ID, DOC_ID))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_existingDocument_setsActiveFalse() {
        Document doc = buildDocument(DOC_ID, DocumentType.ITV, LocalDate.now().plusDays(60));
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(doc));
        when(documentRepository.save(doc)).thenReturn(doc);

        documentService.delete(OWNER_EMAIL, VEHICLE_ID, DOC_ID);

        assertThat(doc.isActive()).isFalse();
        verify(documentRepository).save(doc);
    }

    @Test
    void delete_notFound_throwsDocumentNotFoundException() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.delete(OWNER_EMAIL, VEHICLE_ID, DOC_ID))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    // ── getSummary ────────────────────────────────────────────────────────────

    @Test
    void getSummary_mixedStatuses_returnsCorrectCounts() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByTypeAsc(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(List.of(
                        buildDocument(1L, DocumentType.CEDULA_VERDE, LocalDate.now().plusDays(60)),  // VIGENTE
                        buildDocument(2L, DocumentType.ITV,          LocalDate.now().plusDays(15)),  // POR_VENCER
                        buildDocument(3L, DocumentType.SEGURO,       LocalDate.now().minusDays(1)),  // VENCIDO
                        buildDocument(4L, DocumentType.LICENCIA,     null)                           // SIN_FECHA
                ));

        DocumentSummaryResponse result = documentService.getSummary(OWNER_EMAIL, VEHICLE_ID);

        assertThat(result.total()).isEqualTo(4);
        assertThat(result.vigente()).isEqualTo(1);
        assertThat(result.porVencer()).isEqualTo(1);
        assertThat(result.vencido()).isEqualTo(1);
        assertThat(result.sinFecha()).isEqualTo(1);
        assertThat(result.compliancePercentage()).isEqualTo(25);
    }

    @Test
    void getSummary_nextExpiring_isTheClosestExpiryDate() {
        LocalDate closer = LocalDate.now().plusDays(10);
        LocalDate farther = LocalDate.now().plusDays(20);
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByTypeAsc(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(List.of(
                        buildDocument(1L, DocumentType.SEGURO, farther),
                        buildDocument(2L, DocumentType.ITV,    closer)
                ));

        DocumentSummaryResponse result = documentService.getSummary(OWNER_EMAIL, VEHICLE_ID);

        assertThat(result.nextExpiring()).isNotNull();
        assertThat(result.nextExpiring().type()).isEqualTo(DocumentType.ITV);
        assertThat(result.nextExpiring().daysLeft()).isEqualTo(10);
    }

    @Test
    void getSummary_noDocuments_returns100Compliance() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByTypeAsc(VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(List.of());

        DocumentSummaryResponse result = documentService.getSummary(OWNER_EMAIL, VEHICLE_ID);

        assertThat(result.total()).isEqualTo(0);
        assertThat(result.compliancePercentage()).isEqualTo(100);
        assertThat(result.nextExpiring()).isNull();
    }

    // ── uploadFile ────────────────────────────────────────────────────────────

    @Test
    void uploadFile_existingDocument_storesFileAndUpdatesDocument() {
        Document doc = buildDocument(DOC_ID, DocumentType.ITV, LocalDate.now().plusDays(60));
        MockMultipartFile file = new MockMultipartFile("file", "poliza.pdf", "application/pdf", "content".getBytes());

        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(doc));
        when(fileStorageService.store(VEHICLE_ID, file, null)).thenReturn(VEHICLE_ID + "/uuid.pdf");
        when(documentRepository.save(doc)).thenReturn(doc);

        DocumentResponse result = documentService.uploadFile(OWNER_EMAIL, VEHICLE_ID, DOC_ID, file);

        assertThat(doc.getFilePath()).isEqualTo(VEHICLE_ID + "/uuid.pdf");
        assertThat(doc.getOriginalFileName()).isEqualTo("poliza.pdf");
        assertThat(doc.getFileSize()).isEqualTo(file.getSize());
        assertThat(result.hasFile()).isTrue();
        assertThat(result.originalFileName()).isEqualTo("poliza.pdf");
    }

    @Test
    void uploadFile_documentNotFound_throwsDocumentNotFoundException() {
        MockMultipartFile file = new MockMultipartFile("file", "poliza.pdf", "application/pdf", "content".getBytes());
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.uploadFile(OWNER_EMAIL, VEHICLE_ID, DOC_ID, file))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    // ── getFile ───────────────────────────────────────────────────────────────

    @Test
    void getFile_documentWithFile_returnsStoredFile() {
        Document doc = buildDocument(DOC_ID, DocumentType.ITV, LocalDate.now().plusDays(60));
        doc.setFilePath(VEHICLE_ID + "/uuid.pdf");
        doc.setOriginalFileName("poliza.pdf");

        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(doc));
        when(fileStorageService.load(doc.getFilePath())).thenReturn(new ByteArrayResource("content".getBytes()));

        DocumentService.StoredFile result = documentService.getFile(OWNER_EMAIL, VEHICLE_ID, DOC_ID);

        assertThat(result.fileName()).isEqualTo("poliza.pdf");
    }

    @Test
    void getFile_documentWithoutFile_throwsDocumentFileNotFoundException() {
        Document doc = buildDocument(DOC_ID, DocumentType.ITV, LocalDate.now().plusDays(60));
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.of(doc));

        assertThatThrownBy(() -> documentService.getFile(OWNER_EMAIL, VEHICLE_ID, DOC_ID))
                .isInstanceOf(DocumentFileNotFoundException.class);
    }

    @Test
    void getFile_documentNotFound_throwsDocumentNotFoundException() {
        when(vehicleService.getEntity(VEHICLE_ID, OWNER_EMAIL)).thenReturn(vehicle);
        when(documentRepository.findByIdAndVehicleIdAndVehicleOwnerEmail(DOC_ID, VEHICLE_ID, OWNER_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getFile(OWNER_EMAIL, VEHICLE_ID, DOC_ID))
                .isInstanceOf(DocumentNotFoundException.class);
    }
}
