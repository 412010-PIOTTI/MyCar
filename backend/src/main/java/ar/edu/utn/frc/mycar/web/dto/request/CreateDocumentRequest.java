package ar.edu.utn.frc.mycar.web.dto.request;

import ar.edu.utn.frc.mycar.domain.enums.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateDocumentRequest {

    @NotNull(message = "El tipo de documento es obligatorio.")
    private DocumentType type;

    private String referenceNumber;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private String notes;
}
