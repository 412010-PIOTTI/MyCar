package ar.edu.utn.frc.mycar.domain.entity;

import ar.edu.utn.frc.mycar.domain.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Documentación vehicular con seguimiento de vencimientos.
 * El estado (VIGENTE / POR_VENCER / VENCIDO / SIN_CARGAR) se calcula
 * en tiempo de ejecución comparando expiry_date con la fecha actual.
 * No se persiste.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentType type;

    /** Número de póliza, número de ITV, etc. */
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(length = 300)
    private String notes;

    /** Ruta relativa (bajo app.storage.documents-dir) del PDF adjunto. Null si no se cargó archivo. */
    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
