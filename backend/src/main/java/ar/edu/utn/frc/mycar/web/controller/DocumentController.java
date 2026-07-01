package ar.edu.utn.frc.mycar.web.controller;

import ar.edu.utn.frc.mycar.application.service.DocumentService;
import ar.edu.utn.frc.mycar.web.dto.request.CreateDocumentRequest;
import ar.edu.utn.frc.mycar.web.dto.response.CreateDocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentResponse;
import ar.edu.utn.frc.mycar.web.dto.response.DocumentSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Documents", description = "Register and manage vehicle documents, including PDF file upload/download")
@RestController
@RequestMapping("/api/vehicles/{vehicleId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(
            summary = "Register a document",
            description = "Creates a document for the vehicle. If an active document of the same type already exists it is soft-deleted and replaced; the response includes a 'warning' field when this happens.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document created.",
                    content = @Content(schema = @Schema(implementation = CreateDocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateDocumentResponse create(Authentication authentication,
                                         @PathVariable Long vehicleId,
                                         @RequestBody @Valid CreateDocumentRequest request) {
        return documentService.create(authentication.getName(), vehicleId, request);
    }

    @Operation(summary = "List active documents", description = "Returns all active documents for the vehicle ordered by type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned.",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public List<DocumentResponse> getAll(Authentication authentication,
                                          @PathVariable Long vehicleId) {
        return documentService.getAll(authentication.getName(), vehicleId);
    }

    @Operation(summary = "Get compliance summary", description = "Returns counts by status, compliance percentage, and next expiring document.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned.",
                    content = @Content(schema = @Schema(implementation = DocumentSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/summary")
    public DocumentSummaryResponse getSummary(Authentication authentication,
                                               @PathVariable Long vehicleId) {
        return documentService.getSummary(authentication.getName(), vehicleId);
    }

    @Operation(summary = "Get a document by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document found.",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Document or vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{documentId}")
    public DocumentResponse getById(Authentication authentication,
                                     @PathVariable Long vehicleId,
                                     @PathVariable Long documentId) {
        return documentService.getById(authentication.getName(), vehicleId, documentId);
    }

    @Operation(summary = "Upload a document's PDF file",
            description = "Stores the given PDF as the document's attached file, replacing any previous file.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File uploaded.",
                    content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file (not a PDF or too large).",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Document or vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/{documentId}/file")
    public DocumentResponse uploadFile(Authentication authentication,
                                        @PathVariable Long vehicleId,
                                        @PathVariable Long documentId,
                                        @RequestParam("file") MultipartFile file) {
        return documentService.uploadFile(authentication.getName(), vehicleId, documentId, file);
    }

    @Operation(summary = "Download/view a document's PDF file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File returned.",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Document, vehicle or file not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{documentId}/file")
    public ResponseEntity<Resource> getFile(Authentication authentication,
                                             @PathVariable Long vehicleId,
                                             @PathVariable Long documentId) {
        DocumentService.StoredFile file = documentService.getFile(authentication.getName(), vehicleId, documentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=\"" + file.fileName() + "\"")
                .body(file.resource());
    }

    @Operation(summary = "Delete a document", description = "Soft-deletes the document (sets active = false).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Document deleted."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Document or vehicle not found.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication,
                       @PathVariable Long vehicleId,
                       @PathVariable Long documentId) {
        documentService.delete(authentication.getName(), vehicleId, documentId);
    }
}
