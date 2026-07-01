package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.Document;
import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.entity.Vehicle;
import ar.edu.utn.frc.mycar.domain.enums.AlertType;
import ar.edu.utn.frc.mycar.domain.enums.DocumentStatus;
import ar.edu.utn.frc.mycar.domain.enums.DocumentType;
import ar.edu.utn.frc.mycar.domain.repository.DocumentRepository;
import ar.edu.utn.frc.mycar.web.dto.request.CreateDocumentRequest;
import ar.edu.utn.frc.mycar.web.dto.response.CreateDocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentSummaryResponse;
import ar.edu.utn.frc.mycar.web.exception.DocumentFileNotFoundException;
import ar.edu.utn.frc.mycar.web.exception.DocumentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final VehicleService vehicleService;
    private final UserService userService;
    private final AlertService alertService;
    private final FileStorageService fileStorageService;

    private static final Map<DocumentType, String> TYPE_LABELS = Map.of(
            DocumentType.CEDULA_VERDE, "Cédula Verde",
            DocumentType.CEDULA_AZUL,  "Cédula Azul",
            DocumentType.ITV,          "ITV",
            DocumentType.SEGURO,       "Seguro",
            DocumentType.LICENCIA,     "Licencia",
            DocumentType.PATENTE,      "Patente",
            DocumentType.OTRO,         "Documento"
    );

    @Transactional
    public CreateDocumentResponse create(String ownerEmail, Long vehicleId, CreateDocumentRequest request) {
        Vehicle vehicle = vehicleService.getEntity(vehicleId, ownerEmail);

        Optional<Document> existing =
                documentRepository.findByVehicleIdAndTypeAndActiveTrue(vehicleId, request.getType());

        String warning = null;
        if (existing.isPresent()) {
            existing.get().setActive(false);
            documentRepository.save(existing.get());
            warning = "Se reemplazó el documento anterior de tipo " + request.getType().name() + ".";
        }

        Document document = Document.builder()
                .vehicle(vehicle)
                .type(request.getType())
                .referenceNumber(request.getReferenceNumber())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .notes(request.getNotes())
                .build();

        DocumentResponse response = toResponse(documentRepository.save(document));

        if (request.getExpiryDate() != null) {
            User user = userService.getEntity(ownerEmail);
            String title = "Vencimiento: " + TYPE_LABELS.getOrDefault(request.getType(), request.getType().name());
            alertService.createAutoAlert(vehicle, user, title, AlertType.DATE, request.getExpiryDate(), null);
        }

        return new CreateDocumentResponse(response, warning);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAll(String ownerEmail, Long vehicleId) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        return documentRepository
                .findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByTypeAsc(vehicleId, ownerEmail)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getById(String ownerEmail, Long vehicleId, Long documentId) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        Document doc = documentRepository
                .findByIdAndVehicleIdAndVehicleOwnerEmail(documentId, vehicleId, ownerEmail)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        return toResponse(doc);
    }

    @Transactional
    public DocumentResponse uploadFile(String ownerEmail, Long vehicleId, Long documentId, MultipartFile file) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        Document doc = documentRepository
                .findByIdAndVehicleIdAndVehicleOwnerEmail(documentId, vehicleId, ownerEmail)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        String relativePath = fileStorageService.store(vehicleId, file, doc.getFilePath());

        doc.setFilePath(relativePath);
        doc.setOriginalFileName(sanitizeFileName(file.getOriginalFilename()));
        doc.setFileSize(file.getSize());

        return toResponse(documentRepository.save(doc));
    }

    @Transactional(readOnly = true)
    public StoredFile getFile(String ownerEmail, Long vehicleId, Long documentId) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        Document doc = documentRepository
                .findByIdAndVehicleIdAndVehicleOwnerEmail(documentId, vehicleId, ownerEmail)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        if (doc.getFilePath() == null) {
            throw new DocumentFileNotFoundException(documentId);
        }

        Resource resource = fileStorageService.load(doc.getFilePath());
        return new StoredFile(resource, doc.getOriginalFileName());
    }

    public record StoredFile(Resource resource, String fileName) {}

    /** Strips path separators and control characters from a user-supplied file name before persisting/echoing it. */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "documento.pdf";
        String stripped = fileName.replaceAll("[\\\\/]", "_").replaceAll("[\\p{Cntrl}\"]", "");
        return stripped.isBlank() ? "documento.pdf" : stripped;
    }

    @Transactional
    public void delete(String ownerEmail, Long vehicleId, Long documentId) {
        vehicleService.getEntity(vehicleId, ownerEmail);
        Document doc = documentRepository
                .findByIdAndVehicleIdAndVehicleOwnerEmail(documentId, vehicleId, ownerEmail)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        doc.setActive(false);
        documentRepository.save(doc);
    }

    @Transactional(readOnly = true)
    public DocumentSummaryResponse getSummary(String ownerEmail, Long vehicleId) {
        vehicleService.getEntity(vehicleId, ownerEmail);

        List<Document> docs = documentRepository
                .findByVehicleIdAndVehicleOwnerEmailAndActiveTrueOrderByTypeAsc(vehicleId, ownerEmail);

        int vigente   = 0, porVencer = 0, vencido = 0, sinFecha = 0;
        Document nextExpiringDoc = null;
        long nextDaysLeft = Long.MAX_VALUE;

        for (Document doc : docs) {
            DocumentStatus status = computeStatus(doc.getExpiryDate());
            switch (status) {
                case VIGENTE    -> vigente++;
                case POR_VENCER -> {
                    porVencer++;
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), doc.getExpiryDate());
                    if (days < nextDaysLeft) { nextDaysLeft = days; nextExpiringDoc = doc; }
                }
                case VENCIDO    -> vencido++;
                case SIN_FECHA  -> sinFecha++;
            }
            if (status == DocumentStatus.VIGENTE && doc.getExpiryDate() != null) {
                long days = ChronoUnit.DAYS.between(LocalDate.now(), doc.getExpiryDate());
                if (days < nextDaysLeft) { nextDaysLeft = days; nextExpiringDoc = doc; }
            }
        }

        int total = docs.size();
        int compliance = total == 0 ? 100 : Math.round((vigente * 100f) / total);

        DocumentSummaryResponse.NextExpiring next = null;
        if (nextExpiringDoc != null) {
            next = new DocumentSummaryResponse.NextExpiring(
                    nextExpiringDoc.getType(),
                    TYPE_LABELS.getOrDefault(nextExpiringDoc.getType(), nextExpiringDoc.getType().name()),
                    nextExpiringDoc.getExpiryDate(),
                    nextDaysLeft
            );
        }

        return new DocumentSummaryResponse(total, vigente, porVencer, vencido, sinFecha, compliance, next);
    }

    private DocumentResponse toResponse(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getVehicle().getId(),
                doc.getType(),
                doc.getReferenceNumber(),
                doc.getIssueDate(),
                doc.getExpiryDate(),
                doc.getNotes(),
                computeStatus(doc.getExpiryDate()),
                doc.getFilePath() != null,
                doc.getOriginalFileName(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    private DocumentStatus computeStatus(LocalDate expiryDate) {
        if (expiryDate == null) return DocumentStatus.SIN_FECHA;
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        if (daysLeft < 0) return DocumentStatus.VENCIDO;
        if (daysLeft <= 30) return DocumentStatus.POR_VENCER;
        return DocumentStatus.VIGENTE;
    }
}
